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
import org.red.minecraft.uw.core.event.UWEvent;

public class EntityDamageListener extends A_Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        UndefinedWorldCorePlugin.sendLog(event.getClass().getSimpleName());
        // UW 파이프라인이 발행한 이벤트를 다시 파이프라인에 넣으면 무한 재귀가 된다.
        // 개별 클래스가 아니라 UWEvent 마커로 판정해야 UW 이벤트가 추가돼도 누락되지 않는다.
        if (event.isCancelled() || event instanceof UWEvent || !(event.getEntity() instanceof LivingEntity livingEntity)) return;

        // 낙하/익사/화염 등 엔티티 공격이 아닌 환경 데미지는 UW 파이프라인 예외처리 — 바닐라 처리 유지 (발견이슈 확정)
        if (!(event instanceof EntityDamageByEntityEvent atkEvent)) return;

        event.setCancelled(true);
        A_LivingEntity defender = CommediaDellarte.getALivingEntity(livingEntity);

        if (atkEvent.getDamager() instanceof Projectile projectile) {
            this.onProjectileHit(event, defender, projectile, projectile.getShooter());
            return;
        }

        A_Entity attacker = CommediaDellarte.getAEntity(atkEvent.getDamager());
        CombatManager.damage(attacker, defender, DamageType.PHYSICAL, event.getDamage());
    }

    /**
     * 발사체 피격. 발사체 자체가 DirectEntity(realAttacker), 발사자가 CausingEntity(attacker)다.
     * 발사자가 엔티티가 아니면(발사기 등) 공격자 없는 데미지로 처리된다.
     * damageLocation 은 realAttacker 기준이라 기존과 동일하게 발사체 위치가 된다.
     */
    public void onProjectileHit(EntityDamageEvent event, A_LivingEntity defender, Projectile projectile, ProjectileSource shooter) {
        A_Entity attacker = (shooter instanceof Entity entity) ? CommediaDellarte.getAEntity(entity) : null;
        DamageSource source = new DamageSource(attacker, defender, CommediaDellarte.getAEntity(projectile));

        CombatManager.damage(new DamageAtkProcess(source, DamageType.PHYSICAL, ElementalType.NONE, event.getDamage(), 1, false));
    }

}
