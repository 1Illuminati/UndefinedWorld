package org.red.minecraft.undefinedworld.attribute.stat;

public interface StatHolder {
    int getStatValue(Stat type);
    void setStatValue(Stat type, int value);
    boolean hasStatValue(Stat type);
}
