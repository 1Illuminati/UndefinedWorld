package org.red.minecraft.uw.core.item.attribute.equipment;

import org.red.minecraft.uw.core.item.U_ItemType;

public interface ArmorItem extends EquipmentItem {
    double getDefense();

    @Override
    default U_ItemType getType() {
        return U_ItemType.ARMOR;
    }
}
