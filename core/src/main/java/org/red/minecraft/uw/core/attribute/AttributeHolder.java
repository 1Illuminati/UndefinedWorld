package org.red.minecraft.uw.core.attribute;

public interface AttributeHolder extends AttributeViewer {
    void setAttributeValue(AttributeType type, double value);
}
