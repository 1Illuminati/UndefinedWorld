package org.red.minecraft.uw.core.event;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.ImmutableMap;
import org.bukkit.damage.DamageSource;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;
import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.dellarte.library.entity.A_LivingEntity;
import org.red.minecraft.dellarte.library.entity.A_Player;
import org.red.minecraft.uw.core.combat.damage.DamageInfo;

import java.util.EnumMap;

public class UWAtkDamageEvent extends EntityDamageByEntityEvent implements UWEvent {
    private final A_Entity atkEntity;
    private final A_LivingEntity defEntity;
    private final DamageInfo info;
    public UWAtkDamageEvent(@NotNull A_Entity atkEntity, @NotNull A_LivingEntity defEntity, DamageInfo info) {
        super(atkEntity.getEntity(), defEntity.getEntity(), EntityDamageEvent.DamageCause.CUSTOM,
                DamageSource.builder(org.bukkit.damage.DamageType.GENERIC).withDamageLocation(defEntity.getLocation()).build(),
                new EnumMap<>(ImmutableMap.of(DamageModifier.BASE, info.damage())),
                new EnumMap<DamageModifier, Function<? super Double, Double>>(ImmutableMap.of(DamageModifier.BASE, Functions.constant(-0.0))), info.isCritical());
        this.atkEntity = atkEntity;
        this.defEntity = defEntity;
        this.info = info;

    }

    public A_Entity getAtkEntity() {
        return this.atkEntity;
    }

    public A_LivingEntity getDefEntity() {
        return defEntity;
    }

    public DamageInfo getDamageInfo() {
        return info;
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
        String atkEntityStr = (atkEntity instanceof A_Player aPlayer) ? aPlayer.getName() : atkEntity.getUniqueIdStr();
        String defEntityStr = (defEntity instanceof A_Player aPlayer) ? aPlayer.getName() : defEntity.getUniqueIdStr();
        return String.format("%s-damaged by %s damage:%f, type:%s, critical:%b", defEntityStr, atkEntityStr, info.damage(), info.type(), info.isCritical());
    }
}
