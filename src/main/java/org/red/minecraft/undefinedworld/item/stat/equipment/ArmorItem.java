package org.red.minecraft.undefinedworld.item.stat.equipment;

import java.util.List;

import org.red.library.data.DataMap;
import org.red.minecraft.undefinedworld.attribute.stat.Stat;
import org.red.minecraft.undefinedworld.item.U_ItemConfig;
import org.red.minecraft.undefinedworld.item.U_ItemGrade;
import org.red.minecraft.undefinedworld.item.U_ItemType;

public class ArmorItem extends EquipmentItem {
    private ArmorType armorType;

    public ArmorItem(String code, U_ItemConfig config, ArmorType armorType) {
        super(code, config);
        this.armorType = armorType;
    }

    public ArmorType getArmorType() {
        return this.armorType;
    }

    public void setArmorType(ArmorType type) {
        this.armorType = type;
    }

    @Override
    public boolean isWearable() {
        return true;
    }
    
    public enum ArmorType {
        CHESTPLATE("흉갑"),
        HELMET("헬멧"),
        LEGGINGS("레깅스"),
        BOOTS("신발");
        public final String krName;
        ArmorType(String krName) {
            this.krName = krName;
        }
    }

    @Override
    public String EquipmentTypeKRName() {
        return this.getArmorType().krName;
    }

    @Override
    public DataMap serialize() {
        DataMap dataMap = super.serialize();
        dataMap.put("armorType", this.getArmorType().name());
        
        return dataMap;
    }

    public static ArmorItem deserialize(DataMap map) {
        U_ItemConfig config = new U_ItemConfig(U_ItemType.valueOf(map.getString("type")), U_ItemGrade.valueOf(map.getString("grade")), 
                            map.getString("display"),  map.getList("description"),  map.getBoolean("upgrade"));

        ArmorItem result = new ArmorItem(map.getString("code"), config, ArmorType.valueOf(map.getString("armorType")));
        DataMap stat = map.getDataMap("stat");
        stat.keySet().forEach(name -> result.setStatValue(Stat.getStatByStr(name), stat.getInt(name)));

        return result;
    }
}
