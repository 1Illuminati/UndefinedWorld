package org.red.minecraft.uw.core.combat.damage;

import org.jetbrains.annotations.Nullable;
import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.dellarte.library.entity.A_LivingEntity;

import java.util.Objects;

public class DamageCTX {

    private final @Nullable A_Entity attacker;  // 없을 수 있음
    private final A_LivingEntity defender;             // 항상 존재
    private final double originDamage;               // 초기 데미지 (불변, 참조용)

    private double damage;        // 흐르며 변하는 현재값
    private double scale = 1.0;   // 최종 배율 (곱연산 누적)

    // 모디파이어 간 공유 데이터 (관통량, 속성 플래그 등)
    public DamageCTX(@Nullable A_Entity attacker, A_LivingEntity defender, double originDamage) {
        this.attacker = attacker;
        this.defender = Objects.requireNonNull(defender);
        this.originDamage = originDamage;
        this.damage = originDamage;
    }

    public boolean hasAttacker()  { return attacker != null; }
    public @Nullable A_Entity attacker() { return attacker; }
    public A_LivingEntity defender() { return defender; }
    public double originDamage()  { return originDamage; }

    public double damage()         { return damage; }
    public void addFlat(double v)  { damage += v; }
    public void multiply(double m) { damage *= m; }
    public void setDamage(double v){ damage = Math.max(0, v); }
    public void mulScale(double s) { scale *= s; }

    public double finalDamage() { return Math.max(0, damage * scale); }
}
