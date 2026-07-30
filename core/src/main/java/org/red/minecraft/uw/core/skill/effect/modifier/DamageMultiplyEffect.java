package org.red.minecraft.uw.core.skill.effect.modifier;

import org.red.minecraft.uw.core.skill.CTXType;
import org.red.minecraft.uw.core.skill.SkillCTX;
import org.red.minecraft.uw.core.skill.effect.Effect;
import org.red.minecraft.uw.core.skill.effect.EffectResult;
import org.red.minecraft.uw.core.skill.effect.EffectType;

import java.util.concurrent.CompletableFuture;

/**
 * 데미지 배율({@link CTXType#DAMAGE})을 곱하는 수정자 기어.
 *
 * <p>DAMAGE 는 기본값 1.0 의 <b>배율</b> CTX 다(소비처가 {@code base * CTX.DAMAGE} 로 곱한다).
 * 따라서 이 수정자는 기존 배율에 <b>곱한다</b>.
 *
 * <p>소비처: {@code DamageEffect}, {@code SwordAuraEffect}, {@code ThunderEffect},
 * 그리고 {@code HealEffect}(§2.10 확정 — 회복량도 DAMAGE 배율을 따른다).
 */
public class DamageMultiplyEffect implements Effect {

    private final double multiply;

    /**
     * @throws IllegalArgumentException 배율이 유한하지 않을 때.
     *         NaN 데미지는 체력 계산을 통째로 오염시키고 무한대는 즉사 판정이 된다.
     */
    public DamageMultiplyEffect(double multiply) {
        if (!Double.isFinite(multiply))
            throw new IllegalArgumentException("DamageMultiplyEffect Error: multiply is not finite (" + multiply + ")");

        this.multiply = multiply;
    }

    @Override
    public CompletableFuture<EffectResult> execute(SkillCTX ctx) {
        double damage = ctx.getCTX(CTXType.DAMAGE, 1.0);
        ctx.setCTX(CTXType.DAMAGE, damage * this.multiply);
        return CompletableFuture.completedFuture(EffectResult.SUCCESS);
    }

    @Override
    public EffectType[] getEffectTypes() {
        return new EffectType[]{EffectType.MODIFIER};
    }
}
