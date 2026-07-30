package org.red.minecraft.uw.core.skill.cost;

import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.dellarte.library.entity.A_LivingEntity;
import org.red.minecraft.uw.core.combat.CombatManager;
import org.red.minecraft.uw.core.combat.damage.DamageType;
import org.red.minecraft.uw.core.exeception.CannotPayCostException;

public class HealthCost implements Cost<Double>{
    private final double value;
    public HealthCost(double value) {
        this.value = value;
    }

    @Override
    public CostType getType() {
        return CostType.HEALTH;
    }

    @Override
    public Double getValue() {
        return this.value;
    }

    @Override
    public boolean hasCost(A_Entity entity) {
        return this.hasCost(entity, this.getValue());
    }

    @Override
    public void payCost(A_Entity entity) throws CannotPayCostException {
        this.payCost(entity, this.getValue());
    }

    @Override
    public boolean hasCost(A_Entity entity, Double cost) {
        if (!(entity instanceof A_LivingEntity livingEntity)) return false;
        // 반드시 파라미터 cost로 비교해야 한다. this.getValue()로 비교하면 다중 기어 합산 비용
        // 검사가 무력화되어(자기 몫만 검사) 시전자가 자기 스킬 비용으로 죽는다.
        return livingEntity.getHealth() > cost;
    }

    @Override
    public void payCost(A_Entity entity, Double cost) throws CannotPayCostException {
        if (!hasCost(entity, cost)) throw new CannotPayCostException(this);
        CombatManager.damage(entity.getALivingEntity(), DamageType.COST, cost);
    }

    @Override
    public boolean hasCostMultiple(A_Entity entity, Cost<Double>[] costs) throws IllegalArgumentException {
        return hasCost(entity, sumCosts(costs));
    }

    @Override
    public void payMultiple(A_Entity entity, Cost<Double>[] costs) throws CannotPayCostException, IllegalArgumentException {
        payCost(entity, sumCosts(costs));
    }

    private double sumCosts(Cost<Double>[] costs) throws IllegalArgumentException {
        double sumCost = 0;

        for (Cost<Double> cost : costs) {
            if (!(cost instanceof HealthCost))
                throw new IllegalArgumentException("HealthCost.sumCosts got " + cost.getClass().getSimpleName()
                        + " (type:" + cost.getType() + ")");
            sumCost += cost.getValue();
        }

        return sumCost;
    }
}
