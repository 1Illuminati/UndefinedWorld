package org.red.minecraft.uw.core.attribute;

public interface AttributeHolder {
    double getAttributeValue(AttributeType type);
    void setAttributeValue(AttributeType type, double value);
    boolean hasAttributeValue(AttributeType type);
}
