package org.red.minecraft.uw.core.item.attribute.equipment;

import org.red.minecraft.uw.core.item.U_ItemType;

/**
 * 장신구 아이템 (목걸이, 반지). (구조 결정 T19-7)
 * 갑옷(ALL_DEFENSE), 무기(ALL_DAMAGE)와 달리 고유한 고정 스탯이 없다 — attribute만 가진다.
 */
public interface AccessoryItem extends EquipmentItem {

    @Override
    default U_ItemType getType() {
        return U_ItemType.ACCESSORY;
    }
}
