package org.red.minecraft.uw.enchant.gui;

import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.red.minecraft.dellarte.library.event.listener.A_Listener;
import org.red.minecraft.uw.core.gui.U_Gui;

/**
 * 인챈트 GUI의 드래그/닫힘 처리.
 *
 * CustomGui는 클릭/닫힘/열림만 자동 처리하고 드래그는 처리하지 않으므로,
 * 드래그를 통한 검증 우회를 막기 위해 별도로 차단한다 (U_Gui 공통 정책).
 *
 * 닫힘도 함께 처리한다 — dellarte의 CustomGui 닫힘 디스패치는 A_Player의 IgnoreInvClose 상태면
 * onClose 호출을 건너뛰므로, 그 경우 올려둔 아이템이 반환되지 않고 사라진다.
 * U_Gui.handleClose()는 멱등이라 양쪽에서 호출돼도 중복 반환되지 않는다.
 */
public class EnchantGUIListener extends A_Listener {

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (!(event.getInventory().getHolder() instanceof EnchantGUI)) return;

        U_Gui.blockTopDrag(event);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof EnchantGUI gui)) return;

        gui.handleClose();
    }
}
