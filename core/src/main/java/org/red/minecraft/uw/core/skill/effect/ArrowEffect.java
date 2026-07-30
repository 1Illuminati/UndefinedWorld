package org.red.minecraft.uw.core.skill.effect;

import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.uw.core.UndefinedWorldCorePlugin;
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
            UndefinedWorldCorePlugin.sendLog("ArrowEffect: 시전자가 LivingEntity 가 아니라 발사 불가 caster:" + caster.getUniqueIdStr());
            return CompletableFuture.completedFuture(EffectResult.FAIL);
        }

        double finalSpeed = speed * (double) ctx.getCTX(CTXType.SPEED);
        int count = ctx.getCTX(CTXType.COUNT);

        // SPEED 수정자가 0/음수/비유한 값을 만들 수 있다. 그대로 속도 벡터로 쓰면
        // 화살이 제자리에 떨어지거나(0) 시전자 뒤로 날아가고(음수), NaN 이면 엔티티가 즉시 망가진다.
        if (!Double.isFinite(finalSpeed) || finalSpeed <= 0) {
            UndefinedWorldCorePlugin.sendLog("ArrowEffect: 속도가 유효하지 않아 발사 불가 speed=" + finalSpeed);
            return CompletableFuture.completedFuture(EffectResult.FAIL);
        }

        Vector direction = living.getLocation().getDirection().normalize();

        // 발사 개수 상한은 두지 않는다 (사용자 확정). 과도한 발사는 기어 파워/쿨타임/비용 등 밸런스로 관리한다.
        // 여기 Math.max(1, count)는 상한이 아니라 하한이다 — count가 0 이하여도 최소 1발은 나가야 이펙트가 성립한다.
        // 위의 speed 가드도 상한이 아니라 무효값(NaN/무한/0 이하) 방어다. 둘 다 상한으로 바꾸지 말 것.
        for (int i = 0; i < Math.max(1, count); i++) {
            // launchProjectile 이 벡터를 보관할 수 있으므로 발사마다 새 벡터를 넘긴다
            Arrow arrow = living.launchProjectile(Arrow.class, direction.clone().multiply(finalSpeed));
            arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED); // 스킬 화살은 회수 불가
        }

        return CompletableFuture.completedFuture(EffectResult.SUCCESS);
    }

    @Override
    public EffectType[] getEffectTypes() {
        return new EffectType[]{EffectType.ARROW};
    }
}
