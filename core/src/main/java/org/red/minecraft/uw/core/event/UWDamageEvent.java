package org.red.minecraft.uw.core.event;

import org.bukkit.damage.DamageSource;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;
import org.red.minecraft.dellarte.library.entity.A_LivingEntity;
import org.red.minecraft.dellarte.library.entity.A_Player;
import org.red.minecraft.uw.core.combat.damage.DamageInfo;

public class UWDamageEvent extends EntityDamageEvent implements UWEvent {
    private final A_LivingEntity defEntity;
    private final DamageInfo info;
    public UWDamageEvent(@NotNull A_LivingEntity defEntity, DamageInfo info) {
        super(defEntity.getEntity(), EntityDamageEvent.DamageCause.CUSTOM,
                DamageSource.builder(org.bukkit.damage.DamageType.GENERIC).withDamageLocation(defEntity.getLocation()).build(), info.damage());
        this.defEntity = defEntity;
        this.info = info;
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
        return EntityDamageEvent.getHandlerList();
    }

    @Override
    public String getLoggerMessage() {
        String entityStr = (defEntity instanceof A_Player aPlayer) ? aPlayer.getName() : defEntity.getUniqueIdStr();
        return String.format("%s-damaged damage:%f, type:%s, critical:%b", entityStr, info.damage(), info.type(), info.isCritical());
    }
}
