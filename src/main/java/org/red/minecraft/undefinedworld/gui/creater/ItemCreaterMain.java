package org.red.minecraft.undefinedworld.gui.creater;

import java.util.List;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.red.minecraft.dellarte.library.inventory.CustomGui;
import org.red.minecraft.dellarte.library.item.ItemBuilder;
import org.red.minecraft.undefinedworld.UndefinedWorldPlugin;
import org.red.minecraft.undefinedworld.item.U_Item;
import org.red.minecraft.undefinedworld.item.U_ItemGrade;
import org.red.minecraft.undefinedworld.item.U_ItemImpl;
import org.red.minecraft.undefinedworld.item.U_ItemType;

public class ItemCreaterMain extends CustomGui {
    private final String code;
    private U_Item item;
    private ItemStack itemStack;

    public ItemCreaterMain(String code, ItemStack itemStack) throws IllegalArgumentException {
        super(54, "ItemCreater - " + code);
        this.code = code;
        this.item = UndefinedWorldPlugin.itemManager.has(code) ? UndefinedWorldPlugin.itemManager.get(code) : 
            new U_ItemImpl(code, U_ItemType.RESOURCE, U_ItemGrade.NORMAL, code, List.of("설명"), false) ;
        UndefinedWorldPlugin.itemManager.set(code, item);
        this.itemStack = itemStack;
        item.setItem(itemStack);
        
        this.fillItem(0, 53, ItemBuilder.createItem(" ", Material.GRAY_STAINED_GLASS_PANE), event -> event.setCancelled(true));
        this.setItem(13, itemStack);
        U_ItemType type = item.getType();

        this.setItem(11, ItemBuilder.createItem("§f타입 설정", Material.GOLDEN_APPLE, List.of("§f현재 타입 : " + type.krName, "", "§f클릭시 타입을 설정하는 창으로 이동합니다")),
            event -> event.getWhoClicked().openInventory(new ItemTypeSetterGui(item, itemStack).getInventory()));
        this.setItem(12, ItemBuilder.createItem("§f등급 설정", Material.NETHER_STAR, List.of("§f현재 등급 : " + item.getGrade().krName, "", "§f클릭시 등급을 설정하는 창으로 이동합니다")),
            event -> event.getWhoClicked().openInventory(new ItemGradeSetterGui(item, itemStack).getInventory()));
        this.setButton(13, event -> {
            event.getWhoClicked().getInventory().addItem(itemStack);
            event.getWhoClicked().closeInventory();
        });

        this.setAllClickCancel(true);
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        Bukkit.broadcastMessage("테스트");
    }
    
}
