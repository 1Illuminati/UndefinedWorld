package org.red.minecraft.uw.core.skill.effect.modifier;

import org.red.minecraft.uw.core.skill.CTXType;
import org.red.minecraft.uw.core.skill.SkillCTX;
import org.red.minecraft.uw.core.skill.effect.Effect;
import org.red.minecraft.uw.core.skill.effect.EffectResult;
import org.red.minecraft.uw.core.skill.effect.EffectType;

import java.util.concurrent.CompletableFuture;

/**
 * 크기 배율({@link CTXType#SIZE})을 곱하는 수정자 기어.
 *
 * <p>SIZE 는 {@code double.class} CTX 이므로 <b>곱셈</b>이다 (수정자 연산 규칙은
 * {@link SimpleModifierEffect} 클래스 주석 참고).
 * <p><b>구 {@code SizeIncreaseEffect}(덧셈, 팩토리 id {@code size_increase})를 대체한다.</b>
 * 연산이 덧셈에서 곱셈으로 바뀌었으므로 같은 YAML 값이라도 결과가 다르다.
 *
 * <p>소비처: {@code ProjectileEffect}(충돌 감지 반경), {@code SwordAuraEffect}(판정 크기).
 */
public class SizeMultiplyEffect implements Effect {

    private final double multiply;

    /**
     * @throws IllegalArgumentException 배율이 유한하지 않을 때.
     *         NaN/무한 크기는 파티클 확산값과 충돌 판정 박스를 동시에 망가뜨린다.
     */
    public SizeMultiplyEffect(double multiply) {
        if (!Double.isFinite(multiply))
            throw new IllegalArgumentException("SizeMultiplyEffect Error: multiply is not finite (" + multiply + ")");

        this.multiply = multiply;
    }

    @Override
    public CompletableFuture<EffectResult> execute(SkillCTX ctx) {
        double size = ctx.getCTX(CTXType.SIZE, 1.0);
        ctx.setCTX(CTXType.SIZE, size * this.multiply);
        return CompletableFuture.completedFuture(EffectResult.SUCCESS);
    }

    @Override
    public EffectType[] getEffectTypes() {
        return new EffectType[]{EffectType.MODIFIER};
    }
}
