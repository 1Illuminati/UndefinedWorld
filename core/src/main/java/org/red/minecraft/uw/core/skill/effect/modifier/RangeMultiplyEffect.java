package org.red.minecraft.uw.core.skill.effect.modifier;

import org.red.minecraft.uw.core.skill.CTXType;
import org.red.minecraft.uw.core.skill.SkillCTX;
import org.red.minecraft.uw.core.skill.effect.Effect;
import org.red.minecraft.uw.core.skill.effect.EffectResult;
import org.red.minecraft.uw.core.skill.effect.EffectType;

import java.util.concurrent.CompletableFuture;

/**
 * 사거리/범위 배율({@link CTXType#RANGE})을 곱하는 수정자 기어.
 *
 * <p>RANGE 는 기본값 1.0 의 <b>배율</b> CTX 다(소비처가 {@code base * CTX.RANGE} 로 곱한다).
 * 따라서 이 수정자는 기존 배율에 <b>곱한다</b>.
 *
 * <p>소비처: {@code ProjectileEffect}(사거리), {@code SwordAuraEffect}(사거리), {@code TargetEffect}(탐색 반경).
 * <p><b>탐색 반경 배율은 이 CTX 하나로 통일한다</b>(사용자 확정). {@link CTXType#SEARCH_RANGE} 는 배율이 아니라
 * 반경 <b>절대값</b>을 덮어쓰는 용도다 — 둘의 역할을 바꾸거나 배율 CTX 를 또 만들지 말 것.
 */
public class RangeMultiplyEffect implements Effect {

    private final double multiply;

    /**
     * @throws IllegalArgumentException 배율이 유한하지 않을 때.
     *         무한/NaN 사거리는 탐색 박스를 월드 전체로 만들거나 발사체 종료 조건을 무너뜨린다.
     */
    public RangeMultiplyEffect(double multiply) {
        if (!Double.isFinite(multiply))
            throw new IllegalArgumentException("RangeMultiplyEffect Error: multiply is not finite (" + multiply + ")");

        this.multiply = multiply;
    }

    @Override
    public CompletableFuture<EffectResult> execute(SkillCTX ctx) {
        double range = ctx.getCTX(CTXType.RANGE, 1.0);
        ctx.setCTX(CTXType.RANGE, range * this.multiply);
        return CompletableFuture.completedFuture(EffectResult.SUCCESS);
    }

    @Override
    public EffectType[] getEffectTypes() {
        return new EffectType[]{EffectType.MODIFIER};
    }
}
