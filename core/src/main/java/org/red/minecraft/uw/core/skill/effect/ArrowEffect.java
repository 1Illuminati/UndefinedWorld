package org.red.minecraft.uw.core.skill.effect;

import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.uw.core.skill.CTXType;
import org.red.minecraft.uw.core.skill.SkillCTX;

import java.util.concurrent.CompletableFuture;

/**
 * 화살 발사 이펙트 — 실제 화살 엔티티를 시전자 시선 방향으로 발사한다.
 * 적중 데미지는 바닐라 발사체 경로(EntityDamageListener의 projectile 처리)로 UW 파이프라인에 합류한다.
 * 속도에는 CTX.SPEED 배율, 발수에는 CTX.COUNT가 적용된다.
 */
public class ArrowEffect implements Effect {

    private final double speed;

    public ArrowEffect(double speed) {
        this.speed = speed;
    }

    @Override
    public CompletableFuture<EffectResult> execute(SkillCTX ctx) {
        A_Entity caster = ctx.getCTX(CTXType.CASTER);
        if (!(caster.getEntity() instanceof LivingEntity living)) {
            return CompletableFuture.completedFuture(EffectResult.FAIL);
        }

        double finalSpeed = speed * (double) ctx.getCTX(CTXType.SPEED);
        int count = ctx.getCTX(CTXType.COUNT);

        for (int i = 0; i < Math.max(1, count); i++) {
            Vector velocity = living.getLocation().getDirection().normalize().multiply(finalSpeed);
            Arrow arrow = living.launchProjectile(Arrow.class, velocity);
            arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED); // 스킬 화살은 회수 불가
        }

        return CompletableFuture.completedFuture(EffectResult.SUCCESS);
    }

    @Override
    public EffectType[] getEffectTypes() {
        return new EffectType[]{EffectType.ARROW};
    }
}
