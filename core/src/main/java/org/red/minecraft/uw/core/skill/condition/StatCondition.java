package org.red.minecraft.uw.core.skill.condition;

import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.dellarte.library.entity.A_Player;
import org.red.minecraft.uw.core.attribute.stat.Stat;
import org.red.minecraft.uw.core.player.PlayerHelper;
import org.red.minecraft.uw.core.skill.CTXType;
import org.red.minecraft.uw.core.skill.SkillCTX;

/**
 * 시전자의 특정 Stat이 기준치 이상일 때 통과하는 조건.
 * Stat은 플레이어 전용이므로 플레이어가 아닌 시전자는 항상 실패한다.
 */
public class StatCondition implements Condition {

    private final Stat stat;
    private final int min;

    public StatCondition(Stat stat, int min) {
        this.stat = stat;
        this.min = min;
    }

    @Override
    public boolean test(SkillCTX ctx) {
        A_Entity caster = ctx.getCTX(CTXType.CASTER);
        if (!(caster instanceof A_Player player)) return false;

        return new PlayerHelper(player).getStatValue(stat) >= min;
    }
}
