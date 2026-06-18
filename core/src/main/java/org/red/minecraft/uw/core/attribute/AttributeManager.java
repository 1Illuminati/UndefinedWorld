package org.red.minecraft.uw.core.attribute;

import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.dellarte.library.util.A_DataMap;
import org.red.minecraft.uw.core.StaticValue;
import org.red.minecraft.uw.core.UndefinedWorldCorePlugin;

import java.util.HashMap;
import java.util.Map;

/**
 * 업데이트 버전 각 장비, 스텟별 attribute저장을 다르게 해서 관리
 */
public class AttributeManager {
    private final Map<ContainerType, AttributeContainer> containerMap = new HashMap<>();
    private final A_Entity entity;
    public AttributeManager(A_Entity entity) {
        this.entity = entity;
    }

    public A_Entity getEntity() {
        return entity;
    }

    public double getBaseAttributeValue(AttributeType aType, ContainerType cType) {
        return containerMap.computeIfAbsent(cType, c -> new AttributeContainer(getEntity(), c)).getAttributeValue(aType);
    }

    public boolean hasBaseAttributeValue(AttributeType aType, ContainerType cType) {
        return containerMap.computeIfAbsent(cType, c -> new AttributeContainer(getEntity(), c)).hasAttributeValue(aType);
    }

    public void setBaseAttributeValue(AttributeType aType, ContainerType cType, double value) {
        containerMap.computeIfAbsent(cType, c -> new AttributeContainer(getEntity(), c)).setAttributeValue(aType, value);
    }

    public void addBaseAttributeValue(AttributeType aType, ContainerType cType, double value) {
        this.setBaseAttributeValue(aType, cType, value + getBaseAttributeValue(aType, cType));
    }

    /**
     * 최종적으로 계산에 사용되는 attribute값을 가져올때 사용
     * LivingEntity일 경우 체력은 다르게 처리 된다
     *
     * @param aType 가져올 attributeType
     * @return 최종값
     */
    public double getAttributeValue(AttributeType aType) {
        double result = 0;

        for (ContainerType cType : ContainerType.values()) {
            result += getBaseAttributeValue(aType, cType);
        }

        return result;
    }

    protected record AttributeContainer(A_Entity entity, ContainerType cType) implements AttributeHolder {
        @Override
        public double getAttributeValue(AttributeType type) {
            if (!hasAttributeValue(type)) {
                return 0;
            }

            return this.getDataMap().getDouble(type.name());
        }

        @Override
        public void setAttributeValue(AttributeType type, double value) {
            this.getDataMap().put(type.name(), value);
        }

        @Override
        public boolean hasAttributeValue(AttributeType type) {
            return this.getDataMap().containsKey(type.name());
        }

        public A_DataMap getDataMap() {
            return entity.getDataMap(UndefinedWorldCorePlugin.instance).getDataMap(StaticValue.ATTRIBUTE_CONTAINER_KEY + cType.name());
        }
    }

    public enum ContainerType {
        EQUIPMENT,
        STAT,
        BUFF
    }
}
