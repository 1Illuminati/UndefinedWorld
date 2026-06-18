package org.red.minecraft.uw.core.attribute.stat;

public interface StatHolder {
    void setStatValue(Stat type, int value);
    int getStatValue(Stat type);
    boolean hasStatValue(Stat type);
    int getStatPoint();
    void setStatPoint(int statPoint);
    void addStatPoint(int statPoint);
    void applyStatToAttribute();
}
