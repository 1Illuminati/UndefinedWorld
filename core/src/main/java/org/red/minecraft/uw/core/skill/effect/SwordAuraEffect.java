package org.red.minecraft.uw.core.skill.effect;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.dellarte.library.entity.A_LivingEntity;
import org.red.minecraft.uw.core.UndefinedWorldCore;
import org.red.minecraft.uw.core.attribute.AttributeType;
import org.red.minecraft.uw.core.combat.CombatManager;
import org.red.minecraft.uw.core.combat.ElementalType;
import org.red.minecraft.uw.core.combat.damage.DamageType;
import org.red.minecraft.uw.core.skill.CTXType;
import org.red.minecraft.uw.core.skill.SkillCTX;
import org.red.minecraft.uw.core.skill.projectile.ProjectileController;
import org.red.minecraft.uw.core.skill.projectile.ProjectileData;
import org.red.minecraft.uw.core.skill.projectile.ProjectileType;
import org.red.minecraft.uw.core.skill.target.faction.Faction;
import org.red.minecraft.uw.core.skill.target.faction.FactionType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 검기 이펙트 — 레드스톤(DUST) 파티클 궤적의 관통 검기를 전방으로 날린다.
 * 경로상의 적에게 물리 데미지(PHYSICS_DAMAGE × scale × CTX.DAMAGE)를 즉시 가하고,
 * 종료 시 적중 대상들을 LAST_TARGET_INFO에 저장해 후속 노드에 전달한다.
 *
 * SPEED/RANGE/SIZE CTX 배율이 각각 속도/사거리/판정크기에 적용된다.
 */
public class SwordAuraEffect implements Effect {

    /** 발사 높이 보정 — todo ProjectileEffect와 함께 눈높이 처리 확정 필요 */
    private static final double LAUNCH_HEIGHT = 1.2;

    private final double speed;
    private final double range;
    private final double size;
    private final double scale;

    public SwordAuraEffect(double speed, double range, double size, double scale) {
        this.speed = speed;
        this.range = range;
        this.size = size;
        this.scale = scale;
    }

    @Override
    public CompletableFuture<EffectResult> execute(SkillCTX ctx) {
        A_Entity caster = ctx.getCTX(CTXType.CASTER);

        double finalSpeed = speed * (double) ctx.getCTX(CTXType.SPEED);
        double finalRange = range * (double) ctx.getCTX(CTXType.RANGE);
        double finalSize = size * (double) ctx.getCTX(CTXType.SIZE);

        double baseDamage = UndefinedWorldCore.getAttributeManager(caster).getAttributeValue(AttributeType.PHYSICS_DAMAGE);
        double damage = baseDamage * scale * (double) ctx.getCTX(CTXType.DAMAGE);
        ElementalType elemental = ctx.getCTX(CTXType.ELEMENTAL);

        Location start = caster.getLocation().clone().add(0, LAUNCH_HEIGHT, 0);
        ProjectileData data = new ProjectileData(
                caster, start, caster.getLocation().getDirection(), finalSpeed, finalRange, finalSize, ProjectileType.PIERCE);

        CompletableFuture<EffectResult> future = new CompletableFuture<>();
        List<A_Entity> hits = new ArrayList<>();

        ProjectileController controller = new ProjectileController(
                data,
                Faction.predicate(caster, FactionType.ENEMY),
                hitData -> {
                    for (A_Entity target : hitData.entities()) {
                        A_LivingEntity living = target.getALivingEntity();
                        if (living == null || living.isDead()) continue;

                        hits.add(target);
                        CombatManager.damage(caster, living, DamageType.PHYSICAL, elemental, damage);
                    }
                },
                () -> {
                    if (hits.isEmpty()) {
                        future.complete(EffectResult.FAIL);
                        return;
                    }
                    ctx.setCTX(CTXType.LAST_TARGET_INFO, hits.toArray(new A_Entity[0]));
                    future.complete(EffectResult.SUCCESS);
                }
        );

        // 검기 시각효과: 레드스톤 파티클 궤적 (확정: 마크 레드스톤 파티클)
        Particle.DustOptions dust = new Particle.DustOptions(Color.RED, 1.5f);
        controller.setMoveVisual(loc -> {
            if (loc.getWorld() == null) return;
            loc.getWorld().spawnParticle(Particle.DUST, loc, 12, finalSize * 0.4, finalSize * 0.4, finalSize * 0.4, 0, dust);
        });

        controller.start();
        return future;
    }

    @Override
    public EffectType[] getEffectTypes() {
        return new EffectType[]{EffectType.MELEE, EffectType.TARGET};
    }
}
