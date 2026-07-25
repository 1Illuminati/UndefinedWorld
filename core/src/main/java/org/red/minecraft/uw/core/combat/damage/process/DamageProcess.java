package org.red.minecraft.uw.core.combat.damage.process;

import org.bukkit.Bukkit;
import org.bukkit.damage.DamageSource;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.red.minecraft.dellarte.library.entity.A_LivingEntity;
import org.red.minecraft.uw.core.UndefinedWorldCorePlugin;
import org.red.minecraft.uw.core.combat.CombatManager;
import org.red.minecraft.uw.core.combat.ElementalType;
import org.red.minecraft.uw.core.combat.damage.DamageCTX;
import org.red.minecraft.uw.core.combat.damage.DamageType;
import org.red.minecraft.uw.core.combat.damage.modify.DamageModifierBus;
import org.red.minecraft.uw.core.event.UWDamageEvent;
import org.red.minecraft.uw.core.skill.CastingManager;

public class DamageProcess {
    private final DamageCTX originCTX;
    public DamageProcess(DamageCTX ctx) {
        this.originCTX = ctx;
    }

    public DamageProcess(A_LivingEntity defender, DamageType damageType, ElementalType elementalType, double originDamage, double scale, boolean isCritical) {
        org.red.minecraft.uw.core.combat.damage.DamageSource source = new org.red.minecraft.uw.core.combat.damage.DamageSource(defender);
        this(new DamageCTX(source, damageType, elementalType, originDamage, scale, isCritical));
    }

    public DamageProcess(org.red.minecraft.uw.core.combat.damage.DamageSource source, DamageType damageType, ElementalType elementalType, double originDamage, double scale, boolean isCritical) {
        this(new DamageCTX(source, damageType, elementalType, originDamage, scale, isCritical));
    }

    protected DamageCTX getOriginCTX() {
        return originCTX;
    }

    public void run() {
        DamageCTX resultCTX = DamageModifierBus.create(this.originCTX.copy()).flush();

        // 회피 판정 — 공격자가 있는 데미지만 대상 (todo 독/화상 등 타입별 회피 제외 여부 확정 필요)
        if (resultCTX.hasAttacker() && CombatManager.randomDodgeCheck(resultCTX.defender(), resultCTX.finalDamage())) {
            UndefinedWorldCorePlugin.sendLog("Dodge! " + resultCTX);
            return;
        }

        // 막기 판정 — 성공 시 데미지 완전 무효 (todo 회피/막기 판정 순서 확정 필요, 현재 회피 → 막기)
        if (resultCTX.hasAttacker() && CombatManager.randomBlockCheck(resultCTX.defender())) {
            UndefinedWorldCorePlugin.sendLog("Block! " + resultCTX);
            return;
        }

        EntityDamageEvent event = this.createEvent(resultCTX);
        setEvent(event);

        // 데미지 확정 이후 부수효과 (속성 디버프, 흡혈, 캐스팅 취소) — 이벤트 취소 시 미발동
        if (!event.isCancelled()) {
            ElementalPostProcessor.process(resultCTX, event);
            VamfirePostProcessor.process(resultCTX, event);

            // 캐스팅 취소 규칙 1: 상대 엔티티의 공격에 의한 데미지만, 디버프 데미지(독/화상)는 제외
            if (resultCTX.hasAttacker()
                    && resultCTX.type() != DamageType.POISON
                    && resultCTX.type() != DamageType.BURNING)
                CastingManager.onAttacked(resultCTX.defender());
        }

        UndefinedWorldCorePlugin.sendLog(resultCTX.toString());
    }

    protected void setEvent(EntityDamageEvent event) {
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) return;

        LivingEntity livingEntity = (LivingEntity) event.getEntity();
        livingEntity.setLastDamage(event.getDamage());
        livingEntity.setLastDamageCause(event);
        CombatManager.applyHitEffect(livingEntity, event.getDamageSource().getSourceLocation());
        livingEntity.setHealth(Math.max(livingEntity.getHealth() - event.getDamage(), 0));
    }

    protected EntityDamageEvent createEvent(DamageCTX ctx) {
        DamageSource source = ctx.source().getMinecraftDamageSource();
        return new UWDamageEvent(ctx.defender(), source, ctx.finalDamage());
    }
}
