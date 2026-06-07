package org.red.minecraft.uw.mob.attribute;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import io.lumine.mythic.core.skills.stats.StatModifierType;
import io.lumine.mythic.core.skills.stats.StatType;
import org.red.minecraft.uw.core.attribute.AttributeHolder;
import org.red.minecraft.uw.core.attribute.AttributeType;

import java.util.Optional;

/**
 * MythicMob Stat 호환 Attribute Holder
 * ActiveMob으로 활성화된 몹한테 쓸때 사용한다
 */
public class MythicAttributeHolder implements AttributeHolder {
    private final ActiveMob activeMob;
    public MythicAttributeHolder(ActiveMob mob) {
        this.activeMob = mob;
    }

    @Override
    public double getAttributeValue(AttributeType type) {
        return this.activeMob.getStatRegistry().get(this.getStatType(type));
    }

    @Override
    public void setAttributeValue(AttributeType type, double value) {
        this.activeMob.getStatRegistry().putValue(getStatType(type), this.activeMob, StatModifierType.SETTER, value);
    }

    @Override
    public boolean hasAttributeValue(AttributeType type) {
        return this.activeMob.getStatRegistry().getStatData(this.getStatType(type)).isPresent();
    }

    public StatType getStatType(AttributeType type) {
        Optional<StatType> statType = MythicBukkit.inst().getStatManager().getStat(type.name());
        if (statType.isEmpty()) throw new IllegalStateException("미스틱몹 스텟 설정 안해놓음");
        return statType.get();
    }
}
