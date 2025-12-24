package org.red.minecraft.undefinedworld.item.stat.equipment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.red.library.data.DataMap;
import org.red.minecraft.undefinedworld.attribute.stat.Stat;
import org.red.minecraft.undefinedworld.item.U_Item;
import org.red.minecraft.undefinedworld.item.U_ItemConfig;
import org.red.minecraft.undefinedworld.item.U_ItemImpl;
import org.red.minecraft.undefinedworld.item.stat.StatItem;

import net.md_5.bungee.api.ChatColor;

public abstract class EquipmentItem extends U_ItemImpl implements StatItem {
    private final Map<Stat, Integer> map = new HashMap<>();

    public EquipmentItem(String code, U_ItemConfig config) {
        super(code, config);
    }

    @Override
    public int getStatValue(Stat type) {
        return map.getOrDefault(type, 0);
    }

    @Override
    public void setStatValue(Stat type, int value) {
        map.put(type, value);
    }

    @Override
    public boolean hasStatValue(Stat type) {
        return map.containsKey(type);
    }

    @Override
    public List<String> createLore() {
        List<String> list = new ArrayList<>();
        list.add("");
        list.add(ChatColor.of("#ffbb00") + "§lInfo");
        list.add(String.format(ChatColor.of("#ffbb00") + "§l>§r §7분류: §c%s - %s", this.getType().krName, this.EquipmentTypeKRName()));
        list.add(String.format(ChatColor.of("#ffbb00") + "§l>§r §7등급: §c%s", this.getGrade().krName));
        list.add("");
        list.add(ChatColor.of("#ffbb00") + "§lStat");
        map.entrySet().forEach(entry -> list.add(String.format(ChatColor.of("#ffbb00") + "§l>§r §7%s §f+%d", entry.getKey().krName, entry.getValue())));
        list.add("");
        list.add("");
        getDescription().forEach(str -> list.add(str));


        return list;
    }

    @Override
    public ItemStack setItem(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(getDisplayName());
        meta.setLore(createLore());
        meta.getPersistentDataContainer().set(U_Item.KEY, PersistentDataType.STRING, this.getItemCode());
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    @Override
    public DataMap serialize() {
        DataMap dataMap = super.serialize();
        DataMap statMap = new DataMap();
        map.entrySet().forEach(entry -> statMap.put(entry.getKey().krName, entry.getValue()));
        dataMap.put("stat", statMap);
        
        return dataMap;
    }

    public abstract boolean isWearable();

    public abstract String EquipmentTypeKRName();
}
