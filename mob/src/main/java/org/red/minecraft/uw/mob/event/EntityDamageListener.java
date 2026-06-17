package org.red.minecraft.uw.mob.event;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.red.minecraft.dellarte.library.CommediaDellarte;
import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.dellarte.library.entity.A_LivingEntity;
import org.red.minecraft.dellarte.library.event.listener.A_Listener;
import org.red.minecraft.uw.core.UndefinedWorldCorePlugin;
import org.red.minecraft.uw.core.combat.CombatManager;
import org.red.minecraft.uw.core.combat.ElementalType;
import org.red.minecraft.uw.core.combat.damage.DamageSource;
import org.red.minecraft.uw.core.combat.damage.DamageType;
import org.red.minecraft.uw.core.combat.damage.process.DamageAtkProcess;
import org.red.minecraft.uw.core.event.UWAtkDamageEvent;
import org.red.minecraft.uw.core.event.UWDamageEvent;

public class EntityDamageListener extends A_Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        UndefinedWorldCorePlugin.sendLog(event.getClass().getSimpleName());
        if (event.isCancelled() || event instanceof UWDamageEvent || event instanceof UWAtkDamageEvent || !(event.getEntity() instanceof LivingEntity livingEntity)) return;

        event.setCancelled(true);
        A_LivingEntity defender = CommediaDellarte.getALivingEntity(livingEntity);
        if (event instanceof EntityDamageByEntityEvent atkEvent) {
            if (atkEvent.getDamager() instanceof Projectile projectile) {
                this.onProjectileHit(event, projectile, projectile.getShooter());
                return;
            }

            A_Entity attacker = CommediaDellarte.getAEntity(atkEvent.getDamager());
            CombatManager.damage(attacker, defender, DamageType.PHYSICAL, event.getDamage());
            return;
        }

        CombatManager.damage(defender, DamageType.PHYSICAL, event.getDamage());
    }

    public void onProjectileHit(EntityDamageEvent event, Projectile projectile, ProjectileSource shooter) {
        DamageSource source;
        if (shooter instanceof Entity entity) {
            source = new DamageSource(CommediaDellarte.getAEntity(entity), CommediaDellarte.getAEntity(event.getEntity()).getALivingEntity(),
                    projectile.getLocation());
        } else {
            source = new DamageSource(null, CommediaDellarte.getAEntity(event.getEntity()).getALivingEntity(),
                    projectile.getLocation());
        }

        CombatManager.damage(new DamageAtkProcess(source, DamageType.PHYSICAL, ElementalType.NONE, event.getDamage(), 1, false));
    }

}
