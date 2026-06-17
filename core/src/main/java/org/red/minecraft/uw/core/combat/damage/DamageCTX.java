package org.red.minecraft.uw.core.combat.damage;

import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.Nullable;
import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.dellarte.library.entity.A_LivingEntity;
import org.red.minecraft.uw.core.combat.ElementalType;

import java.util.Objects;

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
    public double scale() { return scale; }
    public DamageType type() { return type; }
    public void setType(DamageType type) { this.type = type; }
    public ElementalType elementalType() { return elementalType; }
    public void setElementalType(ElementalType elementalType) { this.elementalType = elementalType; }

    public boolean isCritical() { return isCritical; }
    public void setCritical(boolean v) { isCritical = v; }

    public double finalDamage() {
        if (!type.canDeath) this.damage = Math.min(damage, damageSource.getDefender().getHealth() - 1);
        return Math.max(0, damage);
    }

    public DamageCTX fixedData() {
        return new FixDamageCTX(this);
    }

    public DamageCTX copy() {
        return new DamageCTX(damageSource, type, elementalType, originDamage, scale, isCritical);
    }

    private static final class FixDamageCTX extends DamageCTX {
        private final double finalDamage;
        public FixDamageCTX(DamageCTX ctx) {
            super(ctx.damageSource, ctx.type, ctx.elementalType, ctx.originDamage, ctx.scale, ctx.isCritical);
            super.setDamage(ctx.damage);
            this.finalDamage = ctx.finalDamage();
        }

        public FixDamageCTX copy() { return new FixDamageCTX(this); }
        public void addFlat(double v)  { throw new UnsupportedOperationException(); }
        public void multiply(double m) { throw new UnsupportedOperationException(); }
        public void setDamage(double v){ throw new UnsupportedOperationException(); }
        public void mulScale(double s) { throw new UnsupportedOperationException(); }
        public void setType(DamageType type) { throw new UnsupportedOperationException(); }
        public void setElementalType(ElementalType elementalType) { throw new UnsupportedOperationException(); }
        public void setCritical(boolean v) { throw new UnsupportedOperationException(); }
        public double finalDamage() { return finalDamage; }
    }
}
