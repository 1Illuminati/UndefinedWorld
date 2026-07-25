package org.red.minecraft.uw.core.skill.craft;

import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.red.minecraft.dellarte.library.event.listener.A_Listener;

/**
 * 스킬 제작 GUI 상호작용 처리. (EquipmentGUIListener와 동일한 차단 정책)
 * - 기어 슬롯: Gear 아이템만 배치 허용
 * - 확인 버튼: 클릭 시 제작 시도
 * - 우회 이동(쉬프트/숫자키/스왑/더블클릭/드래그) 차단
 * - 닫을 때 미제작 상태면 배치 아이템 반환
 */
public class SkillCraftGUIListener extends A_Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof SkillCraftGUI gui)) return;

        // 하단(플레이어 인벤) 클릭: 일반 조작 허용.
        // 쉬프트클릭 이동은 기어 아이템만 허용 (GUI의 빈 칸은 기어 슬롯뿐이라 기어 슬롯으로만 들어간다)
        if (event.getClickedInventory() != event.getInventory()) {
            if (event.getClick().isShiftClick()) {
                ItemStack current = event.getCurrentItem();
                if (current == null || current.isEmpty() || !SkillCraftGUI.isGearItem(current))
                    event.setCancelled(true);
            }
            return;
        }

        ClickType click = event.getClick();
        if (click == ClickType.NUMBER_KEY || click == ClickType.SWAP_OFFHAND || click == ClickType.DOUBLE_CLICK) {
            event.setCancelled(true);
            return;
        }

        int index = event.getSlot();

        if (SkillCraftGUI.isConfirmSlot(index)) {
            event.setCancelled(true);
            gui.confirm();
            return;
        }

        if (!SkillCraftGUI.isGearSlot(index)) { // 필러 영역
            event.setCancelled(true);
            return;
        }

        // 기어 슬롯 배치 검증 (꺼내기는 항상 허용)
        ItemStack cursor = event.getCursor();
        if (!cursor.isEmpty() && !SkillCraftGUI.isGearItem(cursor)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (!(event.getInventory().getHolder() instanceof SkillCraftGUI)) return;

        int topSize = event.getInventory().getSize();
        boolean touchesTop = event.getRawSlots().stream().anyMatch(raw -> raw < topSize);
        if (touchesTop) event.setCancelled(true);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof SkillCraftGUI gui)) return;
        if (gui.isConfirmed()) return;

        gui.returnGearItems(); // 미제작 상태로 닫으면 배치 아이템 반환
    }
}
