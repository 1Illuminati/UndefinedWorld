package org.red.minecraft.undefinedworld.item.stat.equipment;

import org.red.library.data.DataMap;
import org.red.minecraft.undefinedworld.attribute.stat.Stat;
import org.red.minecraft.undefinedworld.item.U_ItemConfig;
import org.red.minecraft.undefinedworld.item.U_ItemGrade;
import org.red.minecraft.undefinedworld.item.U_ItemType;

public class WeaponItem extends EquipmentItem {
    private WeaponType weaponType;

    public WeaponItem(String code, U_ItemConfig config, WeaponType weaponType) {
        super(code, config);
        this.weaponType = weaponType;
    }

    public WeaponType getWeaponType() {
        return this.weaponType;
    }

    public void setWeaponType(WeaponType type) {
        this.weaponType = type;
    }

    @Override
    public boolean isWearable() {
        return false;
    }

    public enum WeaponType {
        SWORD("검"),
        DAGGER("단검"),
        WAND("지팡이"),
        BOW("활"),
        GUN("총"),
        SHILED("방패"),
        SCYTHE("낫"),
        SHOVEL("삽"),
        PICKAXE("곡괭이"),
        AXE("도끼"),
        SPEAR("창");

        public final String krName;
        WeaponType(String krName) {
            this.krName = krName;
        }
    }

    @Override
    public String EquipmentTypeKRName() {
        return this.getWeaponType().krName;
    }

    @Override
    public DataMap serialize() {
        DataMap dataMap = super.serialize();
        dataMap.put("weaponType", this.getWeaponType().name());
        
        return dataMap;
    }

    public static WeaponItem deserialize(DataMap map) {
        U_ItemConfig config = new U_ItemConfig(U_ItemType.valueOf(map.getString("type")), U_ItemGrade.valueOf(map.getString("grade")), 
                            map.getString("display"),  map.getList("description"),  map.getBoolean("upgrade"));

        WeaponItem result = new WeaponItem(map.getString("code"), config, WeaponType.valueOf(map.getString("weaponType")));
        DataMap stat = map.getDataMap("stat");
        stat.keySet().forEach(name -> result.setStatValue(Stat.getStatByStr(name), stat.getInt(name)));

        return result;
    }
}
