package org.red.minecraft.uw.core.attribute;

public enum AttributeType {
    PHYSICS_DAMAGE("물리공격력"),
    PHYSICS_DAMAGE_DEFENSE("물리방어력"),
    PHYSICS_DAMAGE_RESISTANCE("물리저항력"),
    PHYSICS_DAMAGE_MULTIPLY("물리공격력 증폭"),
    PHYSICS_DAMAGE_REDUCE("물리공격력 감소"),
    PHYSICS_DAMAGE_MULTIPLY_REDUCE("물리공격력 증폭 감소"),
    PHYSICS_DAMAGE_DEFENSE_REDUCE("물리방어력 감소"),
    PHYSICS_DAMAGE_RESISTANCE_REDUCE("물리저항력 감소"),

    MAGIC_DAMAGE("마법공격력"),
    MAGIC_DAMAGE_DEFENSE("마법방어력"),
    MAGIC_DAMAGE_RESISTANCE("마법저항력"),
    MAGIC_DAMAGE_MULTIPLY("마법공격력 증폭"),
    MAGIC_DAMAGE_REDUCE("마법공격력 감소"),
    MAGIC_DAMAGE_MULTIPLY_REDUCE("마법공격력 증폭 감소"),
    MAGIC_DAMAGE_DEFENSE_REDUCE("마법방어력 감소"),
    MAGIC_DAMAGE_RESISTANCE_REDUCE("마법저항력 감소"),

    ELEMENT_DAMAGE("속성공격력"),
    ELEMENT_DAMAGE_DEFENSE("속성방어력"),
    ELEMENT_DAMAGE_RESISTANCE("속성저항력"),
    ELEMENT_DAMAGE_MULTIPLY("속성공격력 증폭"),
    ELEMENT_DAMAGE_REDUCE("속성공격력 감소"),
    ELEMENT_DAMAGE_RESISTANCE_REDUCE("속성방어력 감소"),

    // 정신은 보스 패턴용
    MENTAL_DAMAGE("정신공격력"),
    MENTAL_DAMAGE_DEFENSE("정신방어력"),
    MENTAL_DAMAGE_RESISTANCE("정신저항력"),
    MENTAL_DAMAGE_MULTIPLY("정신공격력 증폭"),
    MENTAL_DAMAGE_REDUCE("정신공격력 감소"),
    MENTAL_DAMAGE_RESISTANCE_REDUCE("정신방어력 감소"),

    TRUE_DAMAGE("고정공격력"),
    TRUE_DAMAGE_MULTIPLY("고정공격력 증폭"),
    HEALTH_TRUE_DAMAGE("체력비례 고정공격력"),

    HEALTH_MAX("최대 체력"),
    HEALTH_MULTIPLY("체력 증폭"),
    HEALTH_DIVIDE("체력 감소"),
    HEALTH_REGEN("체력 재생"),
    HEALTH_REGEN_REDUCE("체력 재생 감소"),
    HEALTH_USE_LESS("체력 소모량 감소"),
    HEALTH_USE_MORE("체력 소모량 증가"),
    MANA_MAX("최대 마나"),
    MANA_MULTIPLY("마나 증폭"),
    MANA_DIVIDE("마나 감소"),
    MANA_REGEN("마나 재생"),
    MANA_REGEN_REDUCE("마나 재생 감소"),
    MANA_USE_LESS("마나 소모량 감소"),
    MANA_USE_MORE("마나 소모량 증가"),
    STAMINA_MAX("최대 스테미나"),
    STAMINA_MULTIPLY("스테미나 증폭"),
    STAMINA_DIVIDE("스테미나 감소"),
    STAMINA_REGEN("스테미나 재생"),
    STAMINA_REGEN_REDUCE("스테미나 재생 감소"),
    STAMINA_USE_LESS("스테미나 소모량 감소"),
    STAMINA_USE_MORE("스테미나 소모량 증가"),
    MENTAL("정신력"),
    MENTAL_MAX("최대 정신력"),
    MENTAL_MULTIPLY("정신력 증폭"),
    MENTAL_DIVIDE("정신력 감소"),
    MENTAL_REGEN("정신력 재생"),
    MENTAL_REGEN_REDUCE("정신력 재생 감소"),
    MENTAL_USE_LESS("정신력 소모량 감소"),
    MENTAL_USE_MORE("정신력 소모량 증가"),

    ALL_DEFENSE("절대 방어력"),
    ALL_RESISTANCE("절대 저항력"),
    ALL_DAMAGE("절대 공격력"),
    ALL_DAMAGE_MULTIPLY("절대 공격력 증폭"),

    FIRE_DAMAGE("화염공격력"),
    FIRE_DAMAGE_DEFENSE("화염방어력"),
    FIRE_DAMAGE_RESISTANCE("화염저항력"),
    FIRE_DAMAGE_MULTIPLY("화염공격력 증폭"),
    FIRE_DAMAGE_REDUCE("화염공격력 감소"),
    FIRE_DAMAGE_RESISTANCE_REDUCE("화염방어력 감소"),

    WATER_DAMAGE("물공격력"),
    WATER_DAMAGE_DEFENSE("물방어력"),
    WATER_DAMAGE_RESISTANCE("물저항력"),
    WATER_DAMAGE_MULTIPLY("물공격력 증폭"),
    WATER_DAMAGE_REDUCE("물공격력 감소"),
    WATER_DAMAGE_RESISTANCE_REDUCE("물방어력 감소"),

    WIND_DAMAGE("바람공격력"),
    WIND_DAMAGE_DEFENSE("바람방어력"),
    WIND_DAMAGE_RESISTANCE("바람저항력"),
    WIND_DAMAGE_MULTIPLY("바람공격력 증폭"),
    WIND_DAMAGE_REDUCE("바람공격력 감소"),
    WIND_DAMAGE_RESISTANCE_REDUCE("바람방어력 감소"),

    EARTH_DAMAGE("대지공격력"),
    EARTH_DAMAGE_DEFENSE("대지방어력"),
    EARTH_DAMAGE_RESISTANCE("대지저항력"),
    EARTH_DAMAGE_MULTIPLY("대지공격력 증폭"),
    EARTH_DAMAGE_REDUCE("대지공격력 감소"),
    EARTH_DAMAGE_RESISTANCE_REDUCE("대지방어력 감소"),

    ICE_DAMAGE("얼음공격력"),
    ICE_DAMAGE_DEFENSE("얼음방어력"),
    ICE_DAMAGE_RESISTANCE("얼음저항력"),
    ICE_DAMAGE_MULTIPLY("얼음공격력 증폭"),
    ICE_DAMAGE_REDUCE("얼음공격력 감소"),
    ICE_DAMAGE_RESISTANCE_REDUCE("얼음방어력 감소"),

    THUNDER_DAMAGE("번개공격력"),
    THUNDER_DAMAGE_DEFENSE("번개방어력"),
    THUNDER_DAMAGE_RESISTANCE("번개저항력"),
    THUNDER_DAMAGE_MULTIPLY("번개공격력 증폭"),
    THUNDER_DAMAGE_REDUCE("번개공격력 감소"),
    THUNDER_DAMAGE_RESISTANCE_REDUCE("번개방어력 감소"),

    VAMFIRE("흡혈"),
    VAMFIRE_MULTIPLY("흡혈 증폭"),
    VAMFIRE_RESISTANCE("흡혈저항"),

    HEALING_REDUCE("치유 감소"),

    DEBUFF_WEAK("디버프 취약"),
    DEBUFF_RESISTANCE("디버프 저항"),

    CRITICAL_CHANCE("치명타 확률"),
    CRITICAL_CHANCE_MULTIPLY("치명타 확률 증폭"),
    CRITICAL_CHANCE_DIVIDE("치명타 확률 감소"),
    CRITICAL_DAMAGE("치명타 공격력"),
    CRITICAL_RESISTANCE("치명타 저항력"),
    CRITICAL_DAMAGE_MULTIPLY("치명타 공격력 증폭"),
    CRITICAL_DAMAGE_REDUCE("치명타 공격력 감소"),
    CRITICAL_DAMAGE_DEFENSE("치명타 공격력 방어력"),
    CRITICAL_DAMAGE_RESISTANCE("치명타 공격력 저항력"),
    CRITICAL_DAMAGE_MULTIPLY_REDUCE("치명타 공격력 증폭"),
    CRITICAL_DAMAGE_DEFENSE_REDUCE("치명타 공격력 방어력"),
    CRITICAL_DAMAGE_RESISTANCE_REDUCE("치명타 공격력 저항력"),

    DODGE("회피"),
    DODGE_MULTIPLY("회피 증폭"),
    DODGE_DIVIDE("회피 감소"),

    BLOCK("막기"),
    BLOCK_MULTIPLY("막기 증폭"),
    BLOCK_DIVIDE("막기 감소"),

    SPEED("이속"),
    SPEED_MULTIPLY("이속 증폭"),
    SPEED_DIVIDE("이속 감소");

    public final String krName;
    AttributeType(String krName) {
        this.krName = krName;
    }

    public static AttributeType byName(String name) {
        try {
            return AttributeType.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
