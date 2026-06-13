package org.red.minecraft.uw.core.item.attribute;

import org.red.minecraft.uw.core.attribute.AttributeHolder;
import org.red.minecraft.uw.core.attribute.AttributeType;

public interface AttributeItem extends AttributeHolder {

    @Override
    default void setAttributeValue(AttributeType type, double value) {
        throw new UnsupportedOperationException();
    }
}
