package org.red.minecraft.uw.core.player.equipment;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.red.minecraft.dellarte.library.event.listener.A_Listener;
import org.red.minecraft.uw.core.UndefinedWorldCorePlugin;

/**
 * 장비 GUI 상호작용 처리.
 * - 장비 슬롯 외 영역(필러) 클릭 차단
 * - 배치 검증: 슬롯의 acceptType과 일치하는 장비만 허용
 * - 검증 불가능한 이동(쉬프트클릭/숫자키/양손스왑/더블클릭/드래그) 차단
 * - 변경 발생 시 1틱 뒤 저장 + EQUIPMENT 컨테이너 재계산 ("올려두기만 하면 감지" 방식)
 */
public class EquipmentGUIListener extends A_Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof EquipmentGUI gui)) return;

        // 하단(플레이어 인벤) 클릭: 일반 조작은 허용, GUI로의 자동 이동은 검증 불가라 차단
        if (event.getClickedInventory() != event.getInventory()) {
            if (event.getClick().isShiftClick()) event.setCancelled(true);
            return;
        }

        // 검증을 우회하는 클릭 유형 차단
        ClickType click = event.getClick();
        if (click == ClickType.NUMBER_KEY || click == ClickType.SWAP_OFFHAND || click == ClickType.DOUBLE_CLICK) {
            event.setCancelled(true);
            return;
        }

        EquipSlot slot = EquipSlot.fromGuiIndex(event.getSlot());
        if (slot == null) { // 필러 영역
            event.setCancelled(true);
            return;
        }

        // 배치 검증: 커서에 아이템이 있으면 해당 슬롯에 맞는 장비인지 확인 (꺼내기는 항상 허용)
        ItemStack cursor = event.getCursor();
        if (!cursor.isEmpty() && !EquipmentManager.canEquip(slot, cursor)) {
            event.setCancelled(true);
            return;
        }

        // 클릭 처리 완료 후 1틱 뒤 저장 + 재계산
        Bukkit.getScheduler().runTask(UndefinedWorldCorePlugin.instance, gui::saveAndApply);
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (!(event.getInventory().getHolder() instanceof EquipmentGUI)) return;

        // 드래그 배치는 다중 슬롯 검증이 복잡하므로 GUI 영역에 걸치면 전부 차단 (MVP)
        int topSize = event.getInventory().getSize();
        boolean touchesTop = event.getRawSlots().stream().anyMatch(raw -> raw < topSize);
        if (touchesTop) event.setCancelled(true);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof EquipmentGUI gui)) return;
        gui.saveAndApply();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        WeaponScanTask.remove(event.getPlayer().getUniqueId());
    }
}
