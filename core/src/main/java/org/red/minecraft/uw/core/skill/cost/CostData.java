package org.red.minecraft.uw.core.skill.cost;

import java.util.ArrayList;
import java.util.Collections;
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

    /** 전체 비용 목록 (읽기 전용 뷰). 추가는 addCost로만 한다 — 상태 변경 위치를 한 곳에 고정한다. */
    public List<Cost> getCosts() {
        return Collections.unmodifiableList(costs);
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
