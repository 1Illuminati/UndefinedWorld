package org.red.minecraft.uw.core.skill.condition;

import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.uw.core.UndefinedWorldCore;
import org.red.minecraft.uw.core.combat.buff.BuffType;
import org.red.minecraft.uw.core.skill.CTXType;
import org.red.minecraft.uw.core.skill.SkillCTX;

/**
 * 시전자의 버프 보유 여부 조건.
 * @param type 검사할 버프 종류
 * @param has true면 "보유 중일 때", false면 "미보유일 때" 통과
 */
public record BuffCondition(BuffType type, boolean has) implements Condition {

    @Override
    public boolean test(SkillCTX ctx) {
        A_Entity caster = ctx.getCTX(CTXType.CASTER);
        return UndefinedWorldCore.getBuffManager().hasBuff(caster, type) == has;
    }
}
