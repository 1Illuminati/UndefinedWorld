package org.red.minecraft.uw.core.item.attribute;

import org.red.minecraft.uw.core.attribute.AttributeHolder;
import org.red.minecraft.uw.core.attribute.AttributeType;
import org.red.minecraft.uw.core.item.U_Item;

public interface AttributeItem extends AttributeHolder, U_Item {

    @Override
    default void setAttributeValue(AttributeType type, double value) {
        throw new UnsupportedOperationException();
    }
}
