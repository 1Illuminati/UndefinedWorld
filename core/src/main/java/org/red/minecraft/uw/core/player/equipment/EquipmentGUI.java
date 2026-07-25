package org.red.minecraft.uw.core.player.equipment;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.red.minecraft.dellarte.library.entity.A_Player;

/**
 * 장비 전용 GUI. (구조 결정 T19-2)
 * 마인크래프트 장비창을 사용하지 않는 완전 별도 GUI — 여기 올려두면 감지해서 EQUIPMENT 컨테이너를 재계산한다.
 * 장착해도 플레이어 갑옷창에는 추가되지 않는다.
 *
 * 저장/재계산 트리거는 EquipmentGUIListener가 담당한다.
 */
public class EquipmentGUI implements InventoryHolder {

    private static final int SIZE = 27;

    private final A_Player player;
    private final Inventory inventory;

    public EquipmentGUI(A_Player player) {
        this.player = player;
        this.inventory = Bukkit.createInventory(this, SIZE, Component.text("장비")); //todo GUI 타이틀/디자인 사용자 확정 필요

        ItemStack filler = createFiller();
        for (int i = 0; i < SIZE; i++) {
            inventory.setItem(i, filler);
        }

        // 장비 슬롯은 저장된 장비(없으면 빈칸)로 세팅
        for (EquipSlot slot : EquipSlot.values()) {
            inventory.setItem(slot.guiIndex, EquipmentManager.getEquipment(player, slot));
        }
    }

    public void open() {
        player.openInventory(inventory);
    }

    /** GUI의 장비 슬롯 내용을 저장하고 EQUIPMENT 컨테이너를 재계산한다 */
    public void saveAndApply() {
        for (EquipSlot slot : EquipSlot.values()) {
            EquipmentManager.setEquipment(player, slot, inventory.getItem(slot.guiIndex));
        }
        EquipmentManager.applyEquipmentAttributes(player);
    }

    public A_Player getPlayer() {
        return player;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    private ItemStack createFiller() {
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = filler.getItemMeta();
        meta.displayName(Component.empty());
        filler.setItemMeta(meta);
        return filler;
    }
}
