package org.red.minecraft.uw.core.skill.effect.modifier;

import org.red.minecraft.uw.core.skill.CTXType;
import org.red.minecraft.uw.core.skill.SkillCTX;
import org.red.minecraft.uw.core.skill.effect.Effect;
import org.red.minecraft.uw.core.skill.effect.EffectResult;
import org.red.minecraft.uw.core.skill.effect.EffectType;

import java.util.concurrent.CompletableFuture;

/**
 * 속도 배율({@link CTXType#SPEED})을 곱하는 수정자 기어.
 *
 * <p>SPEED 는 기본값 1.0 의 <b>배율</b> CTX 다(소비처가 {@code base * CTX.SPEED} 로 곱한다).
 * 따라서 이 수정자는 기존 배율에 <b>곱한다</b> — 수정자 두 개(각 1.5)를 겹치면 2.25배가 된다.
 *
 * <p>소비처: {@code ProjectileEffect}, {@code SwordAuraEffect}, {@code ArrowEffect}.
 */
public class SpeedMultiplyEffect implements Effect {

    private final double multiply;

    /**
     * @throws IllegalArgumentException 배율이 유한하지 않을 때.
     *         NaN/무한 배율은 발사체 파라미터를 통째로 무효화시켜 원인에서 먼 곳에서 종료되므로
     *         기어 로드 시점(팩토리)에서 막는 편이 추적하기 쉽다. (SimpleModifierEffect 와 동일한 정책)
     */
    public SpeedMultiplyEffect(double multiply) {
        if (!Double.isFinite(multiply))
            throw new IllegalArgumentException("SpeedMultiplyEffect Error: multiply is not finite (" + multiply + ")");

        this.multiply = multiply;
    }

    @Override
    public CompletableFuture<EffectResult> execute(SkillCTX ctx) {
        // 명시적 기본값을 둔다 — CTXType.SPEED 의 기본값이 제거돼도 여기서 언박싱 NPE 가 나지 않는다.
        double speed = ctx.getCTX(CTXType.SPEED, 1.0);
        ctx.setCTX(CTXType.SPEED, speed * this.multiply);
        return CompletableFuture.completedFuture(EffectResult.SUCCESS);
    }

    @Override
    public EffectType[] getEffectTypes() {
        return new EffectType[]{EffectType.MODIFIER};
    }
}
