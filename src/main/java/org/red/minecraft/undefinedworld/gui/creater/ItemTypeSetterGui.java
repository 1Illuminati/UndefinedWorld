package org.red.minecraft.undefinedworld.gui.creater;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.red.minecraft.dellarte.library.inventory.Button;
import org.red.minecraft.dellarte.library.inventory.CustomGui;
import org.red.minecraft.dellarte.library.item.ItemBuilder;
import org.red.minecraft.undefinedworld.item.U_Item;
import org.red.minecraft.undefinedworld.item.U_ItemType;

public class ItemTypeSetterGui extends CustomGui {
    public ItemTypeSetterGui(U_Item item, ItemStack itemStack) throws IllegalArgumentException {
        super(27, "TypeSetting - " + item.getItemCode());

        int index = 0;
        this.setItem(index++, ItemBuilder.createItem("§a재료§f로 설정", Material.LEATHER), new TypeChangeButton(item, itemStack, U_ItemType.RESOURCE));
        this.setItem(index++, ItemBuilder.createItem("§a광석§f으로 설정", Material.DIAMOND_ORE), new TypeChangeButton(item, itemStack, U_ItemType.ORE));
        this.setItem(index++, ItemBuilder.createItem("§a음식§f으로 설정", Material.APPLE), new TypeChangeButton(item, itemStack, U_ItemType.FOOD));
        this.setItem(index++, ItemBuilder.createItem("§a포션§f로 설정", Material.GLASS_BOTTLE), new TypeChangeButton(item, itemStack, U_ItemType.POTION));
        this.setItem(index++, ItemBuilder.createItem("§a방어구§f로 설정", Material.DIAMOND_CHESTPLATE), new TypeChangeButton(item, itemStack, U_ItemType.ARMOR));
        this.setItem(index++, ItemBuilder.createItem("§a무기§f로 설정", Material.DIAMOND_SWORD), new TypeChangeButton(item, itemStack, U_ItemType.WEAPON));
        this.setItem(index++, ItemBuilder.createItem("§a보조무기§f로 설정", Material.SHIELD), new TypeChangeButton(item, itemStack, U_ItemType.SUB_WEAPON));
        this.setItem(index++, ItemBuilder.createItem("§a장신구§f로 설정", Material.GOLDEN_HELMET), new TypeChangeButton(item, itemStack, U_ItemType.ACCESSORY));
        this.setItem(index++, ItemBuilder.createItem("§a주문서§f로 설정", Material.PAPER), new TypeChangeButton(item, itemStack, U_ItemType.SCROLL));
        this.setItem(index++, ItemBuilder.createItem("§a스페셜§f로 설정", Material.NETHER_STAR), new TypeChangeButton(item, itemStack, U_ItemType.SPECIAL));
        this.setItem(index++, ItemBuilder.createItem("§a스킬기어§f로 설정", Material.PAPER), new TypeChangeButton(item, itemStack, U_ItemType.SKILL_GEAR));
        this.setItem(index++, ItemBuilder.createItem("§a스킬§f로 설정", Material.PAPER), new TypeChangeButton(item, itemStack, U_ItemType.SKILL_ARTIFACT));
        this.setItem(index++, ItemBuilder.createItem("§a도구§f로 설정", Material.DIAMOND_PICKAXE), new TypeChangeButton(item, itemStack, U_ItemType.TOOL));

        this.setAllClickCancel(true);
    }

    private record TypeChangeButton(U_Item item, ItemStack itemStack, U_ItemType type) implements Button {

        @Override
        public void run(InventoryClickEvent event) {
            item.setType(type);
            event.getWhoClicked().openInventory(new ItemCreaterMain(item.getItemCode(), itemStack).getInventory());
        }
    }

}
