package org.red.minecraft.uw.core.item.attribute.equipment;

import org.red.minecraft.uw.core.item.U_ItemType;

/**
 * 무기 아이템. (구조 결정 T19-6)
 * 갑옷의 getDefense처럼 무기는 기본적인 고유 공격값을 가지며 ALL_DAMAGE에 대응한다.
 * (YAML에서 attributes와 다른 별도 키로 정의)
 */
public interface WeaponItem extends EquipmentItem {
    double getDamage();

    @Override
    default U_ItemType getType() {
        return U_ItemType.WEAPON;
    }
}
