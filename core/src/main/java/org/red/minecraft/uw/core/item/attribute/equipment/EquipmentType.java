package org.red.minecraft.uw.core.item.attribute.equipment;

public enum EquipmentType {
    HELMET,
    CHESTPLATE,
    LEGGINGS,
    BOOTS,

    RING,
    NECKLACE,

    SWORD,
    HEAVY_SWORD,
    DAGGER,
    SPEAR,
    SHIELD,
    AXE,
    SCYTHE,
    STICK,
    KNUCKLE,
    HAMMER,

    STAFF,
    BOW,
    CROSSBOW,
    GUN,
    SNIPER;

    /** 갑옷류 (장비 GUI의 갑옷 슬롯 대상) */
    public boolean isArmor() {
        return this == HELMET || this == CHESTPLATE || this == LEGGINGS || this == BOOTS;
    }

    /** 장신구류 (장비 GUI의 장신구 슬롯 대상) */
    public boolean isAccessory() {
        return this == RING || this == NECKLACE;
    }

    /**
     * 무기류 (주손 스캔 대상)
     * 부정 조건(!isArmor() && !isAccessory())으로 두면 새 분류(벨트 등)를 추가할 때
     * 자동으로 무기로 잡히므로 무기 타입을 명시한다.
     */
    public boolean isWeapon() {
        return switch (this) {
            case SWORD, HEAVY_SWORD, DAGGER, SPEAR, SHIELD, AXE, SCYTHE, STICK, KNUCKLE, HAMMER,
                 STAFF, BOW, CROSSBOW, GUN, SNIPER -> true;
            case HELMET, CHESTPLATE, LEGGINGS, BOOTS, RING, NECKLACE -> false;
        };
    }
}
