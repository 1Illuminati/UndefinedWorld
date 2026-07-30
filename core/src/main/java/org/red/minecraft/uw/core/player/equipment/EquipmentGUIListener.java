package org.red.minecraft.uw.core.player.equipment;

import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.red.minecraft.dellarte.library.event.listener.A_Listener;
import org.red.minecraft.uw.core.gui.U_Gui;

/**
 * 장비 GUI의 드래그/닫힘 처리.
 *
 * 클릭은 dellarte가 CustomGui.onClick으로 디스패치하므로 EquipmentGUI가 직접 처리한다.
 * 여기서 다루는 것은 CustomGui가 처리하지 않는 두 가지뿐이다:
 * - 드래그: 슬롯 검증을 우회하는 조작이라 상단이 걸리면 차단 (U_Gui 공통 정책)
 * - 닫힘: dellarte의 닫힘 디스패치는 A_Player가 IgnoreInvClose 상태면 onClose를 건너뛰므로
 *   그 경우 저장이 되지 않는다. U_Gui.handleClose()는 멱등이라 양쪽에서 호출돼도 안전하다.
 */
public class EquipmentGUIListener extends A_Listener {

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (!(event.getInventory().getHolder() instanceof EquipmentGUI)) return;

        U_Gui.blockTopDrag(event);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof EquipmentGUI gui)) return;

        gui.handleClose();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        // todo 무기 스캔 캐시 정리를 장비 GUI 리스너가 맡고 있다 — 담당 분리 여부는 사용자 확인 필요
        WeaponScanTask.remove(event.getPlayer().getUniqueId());
    }
}
