package org.red.minecraft.uw.core.attribute;

import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.dellarte.library.entity.A_LivingEntity;
import org.red.minecraft.dellarte.library.util.A_DataMap;
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

    /**
     * 최종적으로 계산에 사용되는 attribute값을 가져올때 사용
     * LivingEntity일 경우 체력은 다르게 처리 된다
     * @param aType 가져올 attributeType
     * @return 최종값
     */
    public double getAttributeValue(AttributeType aType) {

        if (this.entity instanceof A_LivingEntity livingEntity && aType == AttributeType.HEALTH)
            return livingEntity.getHealth();

        double result = 0;

        for (AttributeContainer c : containerMap.values()) {
            result += c.getAttributeValue(aType);
        }

        return result;
    }

    protected record AttributeContainer(A_Entity entity, ContainerType type) implements AttributeHolder {
        @Override
        public double getAttributeValue(AttributeType type) {
            if (!hasAttributeValue(type)) return 0;

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
            return entity.getDataMap(UndefinedWorldCorePlugin.instance).getDataMap(type.name());
        }
    }

    public enum ContainerType {
        EQUIPMENT,
        STAT,
        BUFF
    }
}
