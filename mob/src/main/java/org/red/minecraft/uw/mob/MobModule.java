package org.red.minecraft.uw.mob;

import io.lumine.mythic.bukkit.MythicBukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.dellarte.library.entity.A_Player;
import org.red.minecraft.uw.core.attribute.AttributeManager;
import org.red.minecraft.uw.core.module.IMobModule;
import org.red.minecraft.uw.core.player.PlayerHelper;
import org.red.minecraft.uw.mob.attribute.MythicAttributeManager;

public class MobModule implements IMobModule {
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
    public AttributeManager getAttributeHolder(A_Entity entity) {
        if (entity instanceof A_Player player) return new PlayerHelper(player);
        else if (isMythicMob(entity)) return new MythicAttributeManager(MythicBukkit.inst().getMobManager().getMythicMobInstance(entity.getEntity()));
        return new AttributeManager(entity);
    }
}
