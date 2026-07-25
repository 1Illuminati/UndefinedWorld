package org.red.minecraft.uw.mob.attribute;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import io.lumine.mythic.core.skills.stats.StatType;
import org.red.minecraft.dellarte.library.CommediaDellarte;
import org.red.minecraft.uw.core.attribute.AttributeManager;
import org.red.minecraft.uw.core.attribute.AttributeType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * MythicMob Stat 호환 Attribute Holder
 * ActiveMob으로 활성화된 몹한테 쓸때 사용한다
 */
public class MythicAttributeManager extends AttributeManager {
    private final Map<AttributeType, Double> buffAttributes = new HashMap<>();
    private final ActiveMob activeMob;
    public MythicAttributeManager(ActiveMob mob) {
        super(CommediaDellarte.getAEntity(mob.getEntity().getBukkitEntity()));
        this.activeMob = mob;
    }

    public double getBaseAttributeValue(AttributeType aType, ContainerType cType) {
        return switch (cType) {
            case EQUIPMENT -> throw new IllegalArgumentException("mythicMob cant use EquipmentContainer");
            case BUFF -> this.buffAttributes.getOrDefault(aType, 0D);
            case STAT -> {
                if (!hasBaseAttributeValue(aType, cType)) yield 0;
                yield  this.activeMob.getStatRegistry().get(this.getStatType(aType));
            }
        };
    }

    public boolean hasBaseAttributeValue(AttributeType aType, ContainerType cType) {
        return switch (cType) {
            case EQUIPMENT -> throw new IllegalArgumentException("mythicMob cant use EquipmentContainer");
            case BUFF -> this.buffAttributes.containsKey(aType);
            case STAT -> this.activeMob.getStatRegistry().getStatData(this.getStatType(aType)).isPresent();
        };
    }

    public void setBaseAttributeValue(AttributeType aType, ContainerType cType, double value) {
        switch (cType) {
            case EQUIPMENT, STAT -> throw new IllegalArgumentException("mythicMob cant set Equipment, Stat Container");
            case BUFF -> buffAttributes.put(aType, value);
        };
    }

    public double getAttributeValue(AttributeType aType) {
        double result = 0;

        for (ContainerType cType : ContainerType.values()) {
            if (cType == ContainerType.EQUIPMENT) continue;
            result += getBaseAttributeValue(aType, cType);
        }

        return result;
    }

    public StatType getStatType(AttributeType type) {
        Optional<StatType> statType = MythicBukkit.inst().getStatManager().getStat(type.name());
        if (statType.isEmpty()) throw new IllegalStateException("미스틱몹 스텟 설정 안해놓음");
        return statType.get();
    }
}
