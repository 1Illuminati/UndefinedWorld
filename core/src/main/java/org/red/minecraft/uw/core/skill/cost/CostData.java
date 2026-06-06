package org.red.minecraft.uw.core.skill.cost;

import java.util.ArrayList;
import java.util.List;

public class CostData {
    private final List<Cost> costs;
    public CostData() {
        this(new ArrayList<>());
    }

    public CostData(List<Cost> costs) {
        this.costs = costs;
    }

    public void addCost(Cost<?> cost) {
        costs.add(cost);
    }

    public void addCost(List<Cost> cost) {
        costs.addAll(cost);
    }

    public List<Cost> getCosts() {
        return costs;
    }

    public List<Cost<?>> getCost(CostType type) {
        List<Cost<?>> result = new ArrayList<>();
        for (Cost cost : costs) {
            if (cost.getType() == type) {
                result.add(cost);
            }
        }

        return result;
    }
}
