package org.red.minecraft.uw.core.combat.damage;

/**
 * 데미지 유형별 일부 특징또한 여기서 설정
 * 크리티컬이 터지는 데미지유형인가?
 * 흡혈 효과가 작동하는 데미지 유형인가?
 * 고정데미지 효과가 작동하는 데미지 유형인가?
 * 플레이어 체력을 1 미만으로 떨어지게 가능한가? (이건 독때문에)
 */
public enum DamageType {
    MAGIC(),
    PHYSICAL(),
    BURNING(false, false, false, true),
    POISON(false, false, false, false),
    CHAIN_LIGHTING(true, false, true, true),
    FREEZE(true, false, true, true),
    REFLECT(true, false, false, true),
    COST(false, false, false, true);

    public final boolean isCritical;
    public final boolean isVamfire;
    public final boolean isTrueDamage;
    public final boolean canDeath;

    DamageType(boolean isCritical, boolean isVamfire, boolean isTrueDamage, boolean canDeath) {
        this.isCritical = isCritical;
        this.isVamfire = isVamfire;
        this.isTrueDamage = isTrueDamage;
        this.canDeath = canDeath;
    }
    DamageType() {
        this(true, true, true, true);
    }
}
