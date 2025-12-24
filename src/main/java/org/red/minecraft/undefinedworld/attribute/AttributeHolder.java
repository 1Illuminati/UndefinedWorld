package org.red.minecraft.undefinedworld.attribute;

public interface AttributeHolder {
    double getAttributeValue(AttributeType type);
    void setAttributeValue(AttributeType type, double value);
    boolean hasAttributeValue(AttributeType type);
}
