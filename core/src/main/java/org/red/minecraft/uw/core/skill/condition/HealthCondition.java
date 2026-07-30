package org.red.minecraft.uw.core.skill.condition;

import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.dellarte.library.entity.A_LivingEntity;
import org.red.minecraft.uw.core.skill.CTXType;
import org.red.minecraft.uw.core.skill.SkillCTX;

/**
 * 시전자의 체력 비율 조건.
 * @param ratio 기준 비율 (0.0 ~ 1.0)
 * @param above true면 "비율 이상", false면 "비율 이하"일 때 통과
 */
public record HealthCondition(double ratio, boolean above) implements Condition {

    @Override
    public boolean test(SkillCTX ctx) {
        A_Entity caster = ctx.getCTX(CTXType.CASTER);
        A_LivingEntity living = caster.getALivingEntity();
        if (living == null || living.isDead()) return false;
        if (living.getMaxHealth() <= 0) return false; // 0 나눗셈(NaN) 방지

        double current = living.getHealth() / living.getMaxHealth();
        return above ? current >= ratio : current <= ratio;
    }
}
