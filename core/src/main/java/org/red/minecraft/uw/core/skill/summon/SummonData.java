package org.red.minecraft.uw.core.skill.summon;

import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.uw.core.attribute.AttributeHolder;
import org.red.minecraft.uw.core.attribute.AttributeType;

public record SummonData(A_Entity owner, int timeTick, int level) implements AttributeHolder {
    @Override
    public void setAttributeValue(AttributeType type, double value) {

    }

    @Override
    public double getAttributeValue(AttributeType type) {
        return 0;
    }

    @Override
    public boolean hasAttributeValue(AttributeType type) {
        return false;
    }
}
