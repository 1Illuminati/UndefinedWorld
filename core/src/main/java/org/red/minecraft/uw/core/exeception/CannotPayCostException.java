package org.red.minecraft.uw.core.exeception;

import org.red.minecraft.uw.core.skill.cost.Cost;

public class CannotPayCostException extends Exception {
    public CannotPayCostException(Cost<?> cost) {
        super("player cannot pay cost - type:" + cost.getType() + " value:" + cost.getValue());
    }
}
