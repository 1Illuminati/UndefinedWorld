package org.red.minecraft.uw.core.attribute.stat;

import org.red.minecraft.uw.core.attribute.AttributeType;

public interface StatHolder {
    void setStatValue(Stat type, int value);
    int getStatValue(Stat type);
    boolean hasStatValue(Stat type);
}
