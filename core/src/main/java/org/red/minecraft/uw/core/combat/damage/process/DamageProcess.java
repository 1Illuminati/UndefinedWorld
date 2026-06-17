package org.red.minecraft.uw.core.combat.damage.process;

import org.bukkit.Bukkit;
import org.bukkit.damage.DamageSource;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.red.minecraft.dellarte.library.entity.A_LivingEntity;
import org.red.minecraft.uw.core.combat.CombatManager;
import org.red.minecraft.uw.core.combat.ElementalType;
import org.red.minecraft.uw.core.combat.damage.DamageCTX;
import org.red.minecraft.uw.core.combat.damage.DamageType;
import org.red.minecraft.uw.core.combat.damage.modify.DamageModifierBus;
import org.red.minecraft.uw.core.event.UWDamageEvent;

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
        EntityDamageEvent event = this.createEvent(resultCTX);
        setEvent(event);
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
