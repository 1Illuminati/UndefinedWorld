package org.red.minecraft.uw.core.player.equipment;

import org.jetbrains.annotations.Nullable;
import org.red.minecraft.uw.core.item.attribute.equipment.EquipmentType;

/**
 * 장비 GUI의 슬롯 정의. (구조 결정 T19-1)
 * 갑옷: 헬멧/갑옷/레깅스/부츠, 장신구: 목걸이 1 + 반지 2.
 * 무기는 GUI 슬롯이 아닌 주손 주기 스캔으로 처리한다 (WeaponScanTask).
 */
public enum EquipSlot {
    HELMET(10, EquipmentType.HELMET, "투구"),
    CHESTPLATE(11, EquipmentType.CHESTPLATE, "갑옷"),
    LEGGINGS(12, EquipmentType.LEGGINGS, "레깅스"),
    BOOTS(13, EquipmentType.BOOTS, "부츠"),

    NECKLACE(15, EquipmentType.NECKLACE, "목걸이"),
    RING_1(16, EquipmentType.RING, "반지 1"),
    RING_2(17, EquipmentType.RING, "반지 2");

    /** GUI 인벤토리(27칸) 내 위치 */
    public final int guiIndex;
    /** 이 슬롯에 장착 가능한 장비 타입 */
    public final EquipmentType acceptType;
    /** GUI 안내/메시지용 이름 */
    public final String krName;

    EquipSlot(int guiIndex, EquipmentType acceptType, String krName) {
        this.guiIndex = guiIndex;
        this.acceptType = acceptType;
        this.krName = krName;
    }

    @Nullable
    public static EquipSlot fromGuiIndex(int index) {
        for (EquipSlot slot : values()) {
            if (slot.guiIndex == index) return slot;
        }
        return null;
    }
}
