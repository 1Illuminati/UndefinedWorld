package org.red.minecraft.uw.core.skill.effect.modifier;

import org.red.minecraft.uw.core.skill.CTXType;
import org.red.minecraft.uw.core.skill.SkillCTX;
import org.red.minecraft.uw.core.skill.effect.Effect;
import org.red.minecraft.uw.core.skill.effect.EffectResult;
import org.red.minecraft.uw.core.skill.effect.EffectType;

import java.util.concurrent.CompletableFuture;

/**
 * 시간 배율({@link CTXType#TIME})을 곱하는 수정자 기어.
 *
 * <p>TIME 은 기본값 1.0 의 <b>배율</b> CTX 다. 따라서 이 수정자는 기존 배율에 <b>곱한다</b>.
 *
 * <p>소비처: {@code BuffEffect}(버프 지속시간 배율).
 */
public class TimeMultiplyEffect implements Effect {

    private final double multiply;

    /**
     * @throws IllegalArgumentException 배율이 유한하지 않을 때.
     *         (BuffEffect 는 비유한 배율을 무시하고 원래 값을 쓰지만, 조용히 무시되면
     *          기어가 왜 안 먹는지 알 수 없으므로 로드 시점에서 막는다)
     */
    public TimeMultiplyEffect(double multiply) {
        if (!Double.isFinite(multiply))
            throw new IllegalArgumentException("TimeMultiplyEffect Error: multiply is not finite (" + multiply + ")");

        this.multiply = multiply;
    }

    @Override
    public CompletableFuture<EffectResult> execute(SkillCTX ctx) {
        double time = ctx.getCTX(CTXType.TIME, 1.0);
        ctx.setCTX(CTXType.TIME, time * this.multiply);
        return CompletableFuture.completedFuture(EffectResult.SUCCESS);
    }

    @Override
    public EffectType[] getEffectTypes() {
        return new EffectType[]{EffectType.MODIFIER};
    }
}
