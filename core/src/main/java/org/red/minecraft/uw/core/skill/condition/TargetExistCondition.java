package org.red.minecraft.uw.core.skill.condition;

import org.bukkit.entity.Entity;
import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.uw.core.skill.CTXType;
import org.red.minecraft.uw.core.skill.SkillCTX;
import org.red.minecraft.uw.core.skill.target.EntityTarget;
import org.red.minecraft.uw.core.skill.target.Target;
import org.red.minecraft.uw.core.skill.target.faction.Faction;
import org.red.minecraft.uw.core.skill.target.faction.FactionType;

/**
 * 시전자 주변 range 내에 지정 faction의 대상이 존재할 때 통과하는 조건.
 */
public record TargetExistCondition(double range, FactionType faction) implements Condition {

    /** faction 생략 시 ENEMY 기준 */
    public TargetExistCondition(double range) {
        this(range, FactionType.ENEMY);
    }

    @Override
    public boolean test(SkillCTX ctx) {
        A_Entity caster = ctx.getCTX(CTXType.CASTER);

        Entity[] found = new EntityTarget(caster.getLocation(), 1, range, Target.SearchType.RANGE_CIRCLE)
                .getTargets(Faction.predicate(caster, faction));

        return found.length > 0;
    }
}
