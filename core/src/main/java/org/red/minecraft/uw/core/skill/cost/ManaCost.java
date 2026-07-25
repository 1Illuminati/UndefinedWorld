package org.red.minecraft.uw.core.skill.cost;

import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.dellarte.library.entity.A_Player;
import org.red.minecraft.uw.core.exeception.CannotPayCostException;
import org.red.minecraft.uw.core.player.PlayerHelper;

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
        if (!(entity instanceof A_Player player)) return true;

        return new PlayerHelper(player).getMana() >= cost;
    }

    @Override
    public void payCost(A_Entity entity, Double cost) throws CannotPayCostException {
        if (!(entity instanceof A_Player player)) return;
        if (!hasCost(entity, cost)) throw new CannotPayCostException(this);
        else new PlayerHelper(player).addMana(-cost);
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
            if (!(cost instanceof ManaCost)) throw new IllegalArgumentException();//todo
            sumCost += cost.getValue();
        }

        return sumCost;
    }
}
