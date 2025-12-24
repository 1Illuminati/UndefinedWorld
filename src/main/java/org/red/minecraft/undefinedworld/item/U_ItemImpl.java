package org.red.minecraft.undefinedworld.item;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.red.library.data.DataMap;

import net.md_5.bungee.api.ChatColor;


public class U_ItemImpl implements U_Item {

    private final String itemCode;
    private U_ItemType type;
    private U_ItemGrade grade;
    private String displayName;
    private List<String> description;
    private boolean isUpgradeable;

    public U_ItemImpl(
            String itemCode,
            U_ItemType type,
            U_ItemGrade grade,
            String displayName,
            List<String> description,
            boolean isUpgradeable
    ) {
        this.itemCode = itemCode;
        this.type = type;
        this.grade = grade;
        this.displayName = displayName;
        this.description = description;
        this.isUpgradeable = isUpgradeable;
    }

    public U_ItemImpl(String itemCode, U_ItemConfig config) {
        this(itemCode, config.type(), config.grade(), config.displayName(), config.description(), config.isUpgradable());
    }

    @Override
    public String getItemCode() {
        return itemCode;
    }

    @Override
    public U_ItemType getType() {
        return type;
    }

    @Override
    public void setType(U_ItemType type) {
        this.type = type;
    }

    @Override
    public U_ItemGrade getGrade() {
        return grade;
    }

    @Override
    public void setGrade(U_ItemGrade grade) {
        this.grade = grade;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public List<String> getDescription() {
        return description;
    }

    @Override
    public void setDescription(List<String> description) {
        this.description = description;
    }

    @Override
    public boolean isUpgradeAble() {
        return isUpgradeable;
    }

    @Override
    public void setUpgradeAble(boolean able) {
        this.isUpgradeable = able;
    }

    @Override
    public List<String> createLore() {
        List<String> list = new ArrayList<>();
        list.add("");
        list.add(ChatColor.of("#ffbb00") + "§lInfo");
        list.add(String.format(ChatColor.of("#ffbb00") + "§l>§r §7분류: §c%s", this.getType().krName));
        list.add(String.format(ChatColor.of("#ffbb00") + "§l>§r §7등급: §c%s", this.getGrade().krName));
        list.add("");
        list.add("");

        description.forEach(list::add);
        return list;
    }

    @Override
    public ItemStack setItem(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(displayName);
        meta.setLore(createLore());
        meta.getPersistentDataContainer().set(U_Item.KEY, PersistentDataType.STRING, itemCode);
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    @Override
    public DataMap serialize() {
        DataMap map = new DataMap();
        map.put("code", this.itemCode);
        map.put("type", this.type.name());
        map.put("grade", this.grade.name());
        map.put("display", this.displayName);
        map.put("description", this.description);
        map.put("upgrade", this.isUpgradeable);
        return map;
    }

    public static U_ItemImpl deserialize(DataMap map) {
        return new U_ItemImpl(
                map.getString("code"),
                U_ItemType.valueOf(map.getString("type")),
                U_ItemGrade.valueOf(map.getString("grade")),
                map.getString("display"),
                map.getList("description"),
                map.getBoolean("upgrade")
        );
    }
}
