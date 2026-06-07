package org.red.minecraft.uw.mob;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.dellarte.library.entity.A_Player;
import org.red.minecraft.uw.core.attribute.AttributeHolder;
import org.red.minecraft.uw.core.attribute.AttributeManager;
import org.red.minecraft.uw.core.module.IMobModule;
import org.red.minecraft.uw.core.player.PlayerHelper;
import org.red.minecraft.uw.mob.attribute.MythicAttributeHolder;

public class MobModule implements IMobModule {
    @Override
    public @NotNull NamespacedKey getKey() {
        return null;
    }

    @Override
    public boolean isMythicMob(A_Entity entity) {
        return this.isMythicMob(entity.getEntity());
    }

    @Override
    public boolean isMythicMob(Entity entity) {
        return MythicBukkit.inst().getMobManager().isMythicMob(entity);
    }

    @Override
    public boolean isDamageableMob(A_Entity entity) {
        return this.isDamageableMob(entity.getEntity());
    }

    @Override
    public boolean isDamageableMob(Entity entity) {
        return entity instanceof LivingEntity;
    }

    @Override
    public AttributeHolder getAttributeHolder(A_Entity entity) {
        if (isMythicMob(entity)) return new MythicAttributeHolder(MythicBukkit.inst().getMobManager().getMythicMobInstance(entity.getEntity()));
        else if (entity instanceof A_Player player) return new PlayerHelper(player);
        return new AttributeManager(entity);
    }
}
