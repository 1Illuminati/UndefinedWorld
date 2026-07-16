package org.red.minecraft.uw.core.skill.gear;

import org.red.minecraft.uw.core.skill.condition.Condition;
import org.red.minecraft.uw.core.skill.cost.Cost;
import org.red.minecraft.uw.core.skill.effect.Effect;
import org.red.minecraft.uw.core.util.HasID;

import java.util.List;

/**
 * 실질 스킬의 한 파츠를 담당하는 구성
 * 기본적으로 5가지로 구성되어있다
 * Effect: 파츠의 효과
 * Condition: 해당 파츠를 사용하기 위한 조건
 * Cost: 비용(마나, 체력, 스테미나 등)
 * Cool: 쿨타임
 * Power: 무게 (자치하는 공간의 크기)
 *
 * 기어를 합쳐 하나의 스킬을 만드며 하나의 스킬은 총 9 초과의 파워를 가질수 없다
 * 완성된 스킬의 실질 쿨타임 계산은 Power의 총합 * 2 + 각 기어들의 쿨타임의 합 이다
 */
public interface Gear extends HasID {
    List<Cost> getCosts();
    Effect getEffect();
    List<Condition> getConditions();
    int getCool();
    int getPower();
    int getCastingTime();
}
