package org.red.minecraft.uw.core.skill.condition;

import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.dellarte.library.entity.A_LivingEntity;
import org.red.minecraft.dellarte.library.entity.A_Player;
import org.red.minecraft.uw.core.player.PlayerHelper;
import org.red.minecraft.uw.core.skill.CTXType;
import org.red.minecraft.uw.core.skill.SkillCTX;
import org.red.minecraft.uw.core.skill.cost.CostType;

/**
 * 시전자의 자원(마나/스테미나/체력) 보유량이 기준치 이상일 때 통과하는 조건.
 * 비용(Cost) 지불과 별개로 "보유량 조건"만 판단한다.
 * 마나/스테미나는 플레이어 전용 자원이므로 플레이어가 아니면 실패, 체력은 리빙 엔티티 공통.
 */
public record ResourceCondition(CostType resource, double min) implements Condition {

    @Override
    public boolean test(SkillCTX ctx) {
        A_Entity caster = ctx.getCTX(CTXType.CASTER);

        return switch (resource) {
            case HEALTH -> {
                A_LivingEntity living = caster.getALivingEntity();
                yield living != null && living.getHealth() >= min;
            }
            case MANA -> caster instanceof A_Player player && new PlayerHelper(player).getMana() >= min;
            case STAMINA -> caster instanceof A_Player player && new PlayerHelper(player).getStamina() >= min;
            case NONE -> true; // 자원 조건 없음
        };
    }
}
