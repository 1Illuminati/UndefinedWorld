package org.red.minecraft.uw.core.skill.cost;

import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.uw.core.UndefinedWorldCore;
import org.red.minecraft.uw.core.attribute.AttributeHolder;
import org.red.minecraft.uw.core.attribute.AttributeType;
import org.red.minecraft.uw.core.exeception.CannotPayCostException;

public class ManaCost implements Cost<Double> {
    private final double value;
    public ManaCost(double value) {
        this.value = value;
    }
    @Override
    public CostType getType() {
        return CostType.MANA;
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
        payCost(entity, this.getValue());
    }

    @Override
    public boolean hasCost(A_Entity entity, Double cost) {
        AttributeHolder holder = UndefinedWorldCore.getAttributeHolder(entity);
        if (!holder.hasAttributeValue(AttributeType.MANA)) return false;
        return holder.getAttributeValue(AttributeType.MANA) >= cost;
    }

    @Override
    public void payCost(A_Entity entity, Double cost) throws CannotPayCostException {
        if (!this.hasCost(entity, cost)) throw new CannotPayCostException(this);
        AttributeHolder holder = UndefinedWorldCore.getAttributeHolder(entity);
        holder.setAttributeValue(AttributeType.MANA, holder.getAttributeValue(AttributeType.MANA) - cost);
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
        double sumMana = 0;

        for (Cost<Double> cost : costs) {
            if (!cost.getClass().isAssignableFrom(ManaCost.class)) throw new IllegalArgumentException();//todo
            sumMana += cost.getValue();
        }

        return sumMana;
    }
}
