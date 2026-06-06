package org.red.minecraft.uw.core.skill.gear;

import org.red.minecraft.uw.core.skill.condition.Condition;
import org.red.minecraft.uw.core.skill.cost.Cost;
import org.red.minecraft.uw.core.skill.effect.Effect;

import java.util.List;

/**
 * 플레이어가 아닌 엔티티 전용 기어
 * 몹 스킬로 제작할때 쿨타임, 조건 등의 부가적인 요소를 없이 사용할때 쓰는 클래스
 */
public record BlankGear(String id, Effect effect) implements Gear {
    @Override
    public List<Cost> getCosts() {
        return List.of();
    }

    @Override
    public Effect getEffect() {
        return this.effect;
    }

    @Override
    public List<Condition> getConditions() {
        return List.of();
    }

    @Override
    public int getCool() {
        return 0;
    }

    @Override
    public int getPower() {
        return 0;
    }

    @Override
    public String getID() {
        return this.id;
    }
}
