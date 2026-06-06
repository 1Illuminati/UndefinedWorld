package org.red.minecraft.uw.core.attribute;

import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.dellarte.library.util.A_DataMap;
import org.red.minecraft.uw.core.UndefinedWorldCorePlugin;

/**
 * 일시적으로 Attribute데이터를 가져오거나 수정할때 사용하는 클래스
 */
public class AttributeManager implements AttributeHolder {
    private final A_Entity entity;
    public AttributeManager(A_Entity entity) {
        this.entity = entity;
    }

    public A_Entity getPlayer() {
        return entity;
    }

    @Override
    public double getAttributeValue(AttributeType type) {
        return this.getDataMap().getDouble(type.toString());
    }

    @Override
    public void setAttributeValue(AttributeType type, double value) {
        this.getDataMap().put(type.toString(), value);
    }

    @Override
    public boolean hasAttributeValue(AttributeType type) {
        return this.getDataMap().containsKey(type.toString());
    }

    public A_DataMap getDataMap() {
        return entity.getDataMap(UndefinedWorldCorePlugin.instance);
    }
}
