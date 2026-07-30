package org.red.minecraft.uw.core.skill.effect.modifier;

import org.red.minecraft.uw.core.skill.CTXType;
import org.red.minecraft.uw.core.skill.SkillCTX;
import org.red.minecraft.uw.core.skill.effect.Effect;
import org.red.minecraft.uw.core.skill.effect.EffectResult;
import org.red.minecraft.uw.core.skill.effect.EffectType;

import java.util.concurrent.CompletableFuture;

/**
 * 렌더 배율({@link CTXType#RENDER})을 곱하는 수정자 기어.
 *
 * <p>RENDER 는 기본값 1.0 의 <b>배율</b> CTX 다. 따라서 이 수정자는 기존 배율에 <b>곱한다</b>.
 *
 * <p>⚠️ <b>현재 이 CTX 를 읽는 소비처가 없다 — 이 기어를 넣어도 실제 효과는 없다.</b>
 * (발사체 충돌 판정 크기는 {@link CTXType#SIZE} 를 쓴다)
 * §2.8 확정에 따라 미사용 CTX 자체는 제거하지 않으므로 수정자만 먼저 만들어 둔다.
 * 소비처가 생기면 이 주석을 지울 것.
 */
public class RenderMultiplyEffect implements Effect {

    private final double multiply;

    /** @throws IllegalArgumentException 배율이 유한하지 않을 때. */
    public RenderMultiplyEffect(double multiply) {
        if (!Double.isFinite(multiply))
            throw new IllegalArgumentException("RenderMultiplyEffect Error: multiply is not finite (" + multiply + ")");

        this.multiply = multiply;
    }

    @Override
    public CompletableFuture<EffectResult> execute(SkillCTX ctx) {
        double render = ctx.getCTX(CTXType.RENDER, 1.0);
        ctx.setCTX(CTXType.RENDER, render * this.multiply);
        return CompletableFuture.completedFuture(EffectResult.SUCCESS);
    }

    @Override
    public EffectType[] getEffectTypes() {
        return new EffectType[]{EffectType.MODIFIER};
    }
}
