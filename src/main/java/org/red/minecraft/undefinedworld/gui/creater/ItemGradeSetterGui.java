package org.red.minecraft.undefinedworld.gui.creater;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.red.minecraft.dellarte.library.inventory.Button;
import org.red.minecraft.dellarte.library.inventory.CustomGui;
import org.red.minecraft.dellarte.library.item.ItemBuilder;
import org.red.minecraft.undefinedworld.item.U_Item;
import org.red.minecraft.undefinedworld.item.U_ItemGrade;

public class ItemGradeSetterGui extends CustomGui {
    public ItemGradeSetterGui(U_Item item, ItemStack itemStack) throws IllegalArgumentException {
        super(27, "GradeSetting - " + item.getItemCode());

        int index = 0;
        this.setItem(index++, ItemBuilder.createItem("§f노말로 설정", Material.WHITE_STAINED_GLASS_PANE), new GradeChangeButton(item, itemStack, U_ItemGrade.NORMAL));
        this.setItem(index++, ItemBuilder.createItem("§9레어§f로 설정", Material.BLUE_STAINED_GLASS_PANE), new GradeChangeButton(item, itemStack, U_ItemGrade.RARE));
        this.setItem(index++, ItemBuilder.createItem("§5에픽§f로 설정", Material.PURPLE_STAINED_GLASS_PANE), new GradeChangeButton(item, itemStack, U_ItemGrade.EPIC));
        this.setItem(index++, ItemBuilder.createItem("§e유니크§f로 설정", Material.YELLOW_STAINED_GLASS_PANE), new GradeChangeButton(item, itemStack, U_ItemGrade.UNIQUE));
        this.setItem(index++, ItemBuilder.createItem("§6전설§f로 설정", Material.ORANGE_STAINED_GLASS_PANE), new GradeChangeButton(item, itemStack, U_ItemGrade.LEGEND));
        this.setItem(index++, ItemBuilder.createItem("§4고대§f로 설정", Material.RED_STAINED_GLASS_PANE), new GradeChangeButton(item, itemStack, U_ItemGrade.ACIENT));
        this.setItem(index++, ItemBuilder.createItem("§d신화§f로 설정", Material.MAGENTA_STAINED_GLASS_PANE), new GradeChangeButton(item, itemStack, U_ItemGrade.MYTHIC));
        this.setItem(index++, ItemBuilder.createItem("§7시작§f로 설정", Material.LIGHT_GRAY_STAINED_GLASS_PANE), new GradeChangeButton(item, itemStack, U_ItemGrade.BEGIN));
        this.setItem(index++, ItemBuilder.createItem("§b별§f로 설정", Material.LIGHT_BLUE_STAINED_GLASS_PANE), new GradeChangeButton(item, itemStack, U_ItemGrade.STAR));
        this.setItem(index++, ItemBuilder.createItem("§0무§f로 설정", Material.BLACK_STAINED_GLASS_PANE), new GradeChangeButton(item, itemStack, U_ItemGrade.NULL));
        this.setItem(index, ItemBuilder.createItem("§8끝§f로 설정", Material.GRAY_STAINED_GLASS_PANE), new GradeChangeButton(item, itemStack, U_ItemGrade.END));

        this.setAllClickCancel(true);
    }

    private record GradeChangeButton(U_Item item, ItemStack itemStack, U_ItemGrade grade) implements Button {

        @Override
        public void run(InventoryClickEvent event) {
            item.setGrade(grade);
            event.getWhoClicked().openInventory(new ItemCreaterMain(item.getItemCode(), itemStack).getInventory());
        }
    }
}
