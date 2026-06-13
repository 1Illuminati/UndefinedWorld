package org.red.minecraft.uw.mob.event;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.red.minecraft.dellarte.library.CommediaDellarte;
import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.dellarte.library.entity.A_LivingEntity;
import org.red.minecraft.dellarte.library.event.listener.A_Listener;
import org.red.minecraft.uw.core.UndefinedWorldCorePlugin;
import org.red.minecraft.uw.core.combat.CombatManager;
import org.red.minecraft.uw.core.combat.damage.DamageType;
import org.red.minecraft.uw.core.event.UWAtkDamageEvent;
import org.red.minecraft.uw.core.event.UWDamageEvent;

public class EntityDamageListener extends A_Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public  void onEntityDamage(EntityDamageEvent event) {
        UndefinedWorldCorePlugin.sendLog(event.getClass().getSimpleName());
        if (event.isCancelled() || event instanceof UWDamageEvent || event instanceof UWAtkDamageEvent) return;

        if (event instanceof EntityDamageByEntityEvent atkEvent && atkEvent.getEntity() instanceof LivingEntity livingEntity) {
            Entity atkEntity = atkEvent.getEntity();
            A_Entity atk = CommediaDellarte.getAEntity(atkEntity instanceof Projectile projectile ? projectile.getShooter() : atkEntity);
            A_LivingEntity def = CommediaDellarte.getALivingEntity(livingEntity);

            CombatManager.damage(atk, def, DamageType.PHYSICAL, 1, false);
            event.setCancelled(true);
        }
    }
}
