package org.red.minecraft.uw.core.combat.damage;

import org.jetbrains.annotations.Nullable;
import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.dellarte.library.entity.A_LivingEntity;
import org.red.minecraft.dellarte.library.entity.A_Player;
import org.red.minecraft.uw.core.UndefinedWorldCorePlugin;
import org.red.minecraft.uw.core.combat.ElementalType;

public class DamageCTX {
    private final DamageSource damageSource;
    private final double originDamage;               // 초기 데미지 (불변, 참조용)
    private DamageType type;
    private ElementalType elementalType;
    private double damage;        // 흐르며 변하는 현재값
    private double scale;   // 최종 배율 (곱연산 누적)
    private boolean isCritical;

    // 모디파이어 간 공유 데이터 (관통량, 속성 플래그 등)
    public DamageCTX(DamageSource source, DamageType damageType, ElementalType elementalType, double originDamage, double scale, boolean isCritical) {
        this.damageSource = source;
        this.originDamage = originDamage;
        this.type = damageType;
        this.elementalType = elementalType;
        this.damage = originDamage;
        this.scale = scale;
        this.isCritical = isCritical;
    }

    public DamageCTX(DamageSource source, DamageType damageType, ElementalType elementalType, double originDamage, boolean isCritical) {
        this(source, damageType, elementalType, originDamage, 1.0, isCritical);
    }

    public DamageCTX(DamageSource source, DamageType damageType, ElementalType elementalType, double originDamage) {
        this(source, damageType, elementalType, originDamage, 1.0, false);
    }

    public boolean hasAttacker()  { return damageSource.hasAttacker(); }
    public @Nullable A_Entity attacker() { return damageSource.getAttacker(); }
    public A_LivingEntity defender() { return damageSource.getDefender(); }
    public double originDamage()  { return originDamage; }

    public DamageSource source() { return damageSource; }
    public double damage()         { return damage; }
    public void addFlat(double v)  { damage += v; }
    public void multiply(double m) { damage *= m; }
    public void setDamage(double v){ damage = Math.max(0, v); }
    public void mulScale(double s) { scale *= s; }

    /**
     * 물리/마법 공격 배율.
     *
     * PhysicalAtkModifier / MagicAtkModifier 만 이 값을 읽는다.
     * 즉 <b>공격자가 있고 DamageType 이 PHYSICAL 또는 MAGIC 일 때만</b> 데미지에 반영되며,
     * 그 외 타입(독/화상/연쇄/COST 등)과 공격자 없는 데미지에서는 무시된다. (확정 사항, CombatManager javadoc 참조)
     */
    public double scale() { return scale; }
    public DamageType type() { return type; }
    public void setType(DamageType type) { this.type = type; }
    public ElementalType elementalType() { return elementalType; }
    public void setElementalType(ElementalType elementalType) { this.elementalType = elementalType; }

    public boolean isCritical() { return isCritical; }
    public void setCritical(boolean v) { isCritical = v; }

    /**
     * 최종 데미지 산출 (부작용 없음 — 여러 번 호출해도 같은 값, ctx 상태를 바꾸지 않는다).
     * canDeath=false 타입은 방어자 체력 1 미만으로 떨어지지 않도록 상한을 둔다.
     * 비정상값(NaN/Infinity)은 체력에 그대로 적용되면 엔티티 체력이 복구 불가 상태가 되므로 0으로 차단하고 로그를 남긴다.
     */
    public double finalDamage() {
        double result = damage;
        if (!type.canDeath) result = Math.min(result, damageSource.getDefender().getHealth() - 1);

        if (!Double.isFinite(result)) {
            UndefinedWorldCorePlugin.sendLog("finalDamage 비정상값(NaN/Infinity) 차단 → 0 처리: " + this);
            return 0;
        }

        return Math.max(0, result);
    }

    public DamageCTX fixedData() {
        return new FixDamageCTX(this);
    }

    /**
     * 현재 상태 그대로의 복제본. 진행 중인 damage 값도 함께 옮긴다.
     * (originDamage 로만 복원하면 "복사"라는 이름과 달리 계산 중이던 값이 조용히 되돌아간다)
     */
    public DamageCTX copy() {
        DamageCTX copied = new DamageCTX(damageSource, type, elementalType, originDamage, scale, isCritical);
        copied.setDamage(damage);
        return copied;
    }

    @Override
    public String toString() {
        return "DamageCTX{" +
                "type=" + type +
                ", elementalType=" + elementalType +
                ", originDamage=" + originDamage +
                ", damage=" + damage +
                ", scale=" + scale +
                ", isCritical=" + isCritical +
                ", attacker=" + (!hasAttacker() ? "null" : attacker() instanceof A_Player ? attacker().getName() : attacker().getUniqueIdStr()) +
                ", defender=" + (defender() instanceof A_Player ? defender().getName() : defender().getUniqueIdStr()) +
                '}';
    }

    private static final class FixDamageCTX extends DamageCTX {
        private final double finalDamage;
        public FixDamageCTX(DamageCTX ctx) {
            super(ctx.damageSource, ctx.type, ctx.elementalType, ctx.originDamage, ctx.scale, ctx.isCritical);
            super.setDamage(ctx.damage);
            this.finalDamage = ctx.finalDamage();
        }

        // @Override 를 붙여둔다: 상위 시그니처가 바뀌면 봉인이 조용히 풀려(오버라이드가 아니게 되어)
        // 확정된 CTX 가 다시 변경 가능해지는데, 컴파일 에러로 즉시 드러나게 하기 위함이다.
        @Override public FixDamageCTX copy() { return new FixDamageCTX(this); }
        @Override public void addFlat(double v)  { throw new UnsupportedOperationException(); }
        @Override public void multiply(double m) { throw new UnsupportedOperationException(); }
        @Override public void setDamage(double v){ throw new UnsupportedOperationException(); }
        @Override public void mulScale(double s) { throw new UnsupportedOperationException(); }
        @Override public void setType(DamageType type) { throw new UnsupportedOperationException(); }
        @Override public void setElementalType(ElementalType elementalType) { throw new UnsupportedOperationException(); }
        @Override public void setCritical(boolean v) { throw new UnsupportedOperationException(); }
        @Override public double finalDamage() { return finalDamage; }
    }
}
