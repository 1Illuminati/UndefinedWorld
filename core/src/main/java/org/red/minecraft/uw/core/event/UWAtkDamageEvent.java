package org.red.minecraft.uw.core.event;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.ImmutableMap;
import org.bukkit.damage.DamageSource;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;
import org.red.minecraft.dellarte.library.CommediaDellarte;
import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.dellarte.library.entity.A_LivingEntity;

import java.util.EnumMap;

public class UWAtkDamageEvent extends EntityDamageByEntityEvent implements UWEvent {

    public UWAtkDamageEvent(@NotNull A_Entity damager, @NotNull A_LivingEntity damagee, @NotNull DamageSource damageSource, double damage, boolean critical) {
        super(damager.getEntity(), damagee.getEntity(), DamageCause.CUSTOM, damageSource, new EnumMap<>(ImmutableMap.of(DamageModifier.BASE, damage)), new EnumMap<DamageModifier, Function<? super Double, Double>>(ImmutableMap.of(DamageModifier.BASE, Functions.constant(-0.0))), critical);
    }

    public A_Entity attacker() {
        return CommediaDellarte.getAEntity(this.getDamager());
    }

    public A_LivingEntity defender() {
        return CommediaDellarte.getAEntity(this.getEntity()).getALivingEntity();
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return super.getHandlers();
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return EntityDamageByEntityEvent.getHandlerList();
    }

    @Override
    public String getLoggerMessage() {
        return "";
    }
}
