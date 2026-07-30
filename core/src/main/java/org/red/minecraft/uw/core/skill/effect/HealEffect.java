package org.red.minecraft.uw.core.skill.effect;

import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.dellarte.library.entity.A_LivingEntity;
import org.red.minecraft.uw.core.UndefinedWorldCorePlugin;
import org.red.minecraft.uw.core.skill.CTXType;
import org.red.minecraft.uw.core.skill.SkillCTX;

import java.util.concurrent.CompletableFuture;

/**
 * 힐 이펙트 — 자신(self=true) 또는 LAST_TARGET_INFO 대상의 체력을 회복한다.
 * 대상이 없거나 실제로 한 대상도 회복시키지 못하면 FAIL.
 *
 * <p>회복량 = 기어 YAML amount × {@link CTXType#DAMAGE} <b>(배율)</b>.
 * §2.10 확정: <b>회복량은 DAMAGE CTX 의 영향을 받는다.</b> 회복 전용 배율 CTX 는 두지 않는다.
 *
 * todo HEALING_REDUCE(치유 감소) 속성 반영 여부 확정 필요
 */
public class HealEffect implements Effect {

    private final double amount;
    private final boolean self;

    public HealEffect(double amount, boolean self) {
        this.amount = amount;
        this.self = self;
    }

    @Override
    public CompletableFuture<EffectResult> execute(SkillCTX ctx) {
        A_Entity caster = ctx.getCTX(CTXType.CASTER);

        A_Entity[] targets;
        if (self) {
            targets = new A_Entity[]{caster};
        } else {
            targets = ctx.hasCTX(CTXType.LAST_TARGET_INFO) ? ctx.getCTX(CTXType.LAST_TARGET_INFO) : null;
            if (targets == null || targets.length == 0) {
                UndefinedWorldCorePlugin.sendLog("HealEffect: LAST_TARGET_INFO 가 비어 있어 실패 (앞선 타겟 기어 확인 필요)");
                return CompletableFuture.completedFuture(EffectResult.FAIL);
            }
        }

        double healAmount = resolveAmount(ctx);

        int applied = 0;
        for (A_Entity target : targets) {
            if (target == null) continue;

            A_LivingEntity living = target.getALivingEntity();
            if (living == null || living.isDead()) continue;

            // setHealth 는 [0, maxHealth] 를 벗어나면 IllegalArgumentException 을 던진다.
            // amount 는 기어 YAML 값이라 음수/과대값이 들어올 수 있으므로 상·하한을 모두 잡는다.
            // (예외가 나가면 남은 대상이 통째로 회복되지 않고 체인도 끊긴다)
            try {
                living.setHealth(Math.clamp(living.getHealth() + healAmount, 0.0, living.getMaxHealth()));
                applied++;
            } catch (RuntimeException exception) {
                UndefinedWorldCorePlugin.sendLog("HealEffect 대상 처리 실패 target:" + target.getUniqueIdStr() + " - " + exception);
            }
        }

        // 대상은 있었으나 전부 무효라 아무것도 회복시키지 못한 경우도 실패로 본다 (다른 이펙트와 동일 기준)
        if (applied == 0) {
            UndefinedWorldCorePlugin.sendLog("HealEffect: 유효한 대상이 없어 실패 (self=" + self + ", targets=" + targets.length + ")");
            return CompletableFuture.completedFuture(EffectResult.FAIL);
        }

        return CompletableFuture.completedFuture(EffectResult.SUCCESS);
    }

    /**
     * 회복량 = 기어 YAML amount × CTX.DAMAGE <b>(배율)</b>. (§2.10 확정)
     * <p>CTXType.DAMAGE 의 기본값이 1.0 이라 수정자 기어가 없으면 amount 그대로다.
     * <p>비유한 값(수정자가 NaN/무한을 만든 경우)은 배율을 무시하고 원래 값을 쓴다.
     * NaN 을 그대로 쓰면 setHealth 가 대상마다 예외를 던져 회복이 통째로 실패한다.
     * (BuffEffect.resolveDuration 과 동일한 관용구)
     */
    private double resolveAmount(SkillCTX ctx) {
        double scaled = amount * ctx.getCTX(CTXType.DAMAGE, 1.0);
        if (!Double.isFinite(scaled)) {
            UndefinedWorldCorePlugin.sendLog("HealEffect: DAMAGE 배율이 유효하지 않아 무시 (amount=" + amount + ")");
            return amount;
        }
        return scaled;
    }

    @Override
    public EffectType[] getEffectTypes() {
        return new EffectType[]{EffectType.BUFF};
    }
}
