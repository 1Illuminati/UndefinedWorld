package org.red.minecraft.uw.core.player;

import org.red.minecraft.dellarte.library.entity.A_Player;
import org.red.minecraft.dellarte.library.util.A_DataMap;
import org.red.minecraft.uw.core.StaticValue;
import org.red.minecraft.uw.core.UndefinedWorldCorePlugin;
import org.red.minecraft.uw.core.attribute.AttributeManager;
import org.red.minecraft.uw.core.attribute.AttributeType;
import org.red.minecraft.uw.core.attribute.stat.Stat;
import org.red.minecraft.uw.core.attribute.stat.StatHolder;

/**
 * 플레이어용 함수 모아두는 클래스
 */
public final class PlayerHelper extends AttributeManager implements StatHolder {
    public PlayerHelper(A_Player player) {
        super(player);
    }

    public A_Player getPlayer() {
        return this.getEntity();
    }

    @Override
    public A_Player getEntity() {
        return (A_Player) super.getEntity();
    }

    public double getMana() {
        return getPlayer().getDataMap(UndefinedWorldCorePlugin.instance).getDouble(StaticValue.MANA_KEY);
    }

    public double getStamina() {
        return getPlayer().getDataMap(UndefinedWorldCorePlugin.instance).getDouble(StaticValue.STAMINA_KEY);
    }

    public void setMana(double value) {
        getPlayer().getDataMap(UndefinedWorldCorePlugin.instance).put(StaticValue.MANA_KEY, Math.clamp(value, 0, this.getAttributeValue(AttributeType.MANA_MAX)));
    }

    public void setStamina(double value) {
        getPlayer().getDataMap(UndefinedWorldCorePlugin.instance).put(StaticValue.STAMINA_KEY, Math.clamp(value, 0, this.getAttributeValue(AttributeType.STAMINA_MAX)));
    }

    public void addMana(double value) {
        setMana(getMana() + value);
    }

    public void addStamina(double value) {
        setStamina(getStamina() + value);
    }

    @Override
    public void setStatValue(Stat type, int value) {
        this.getStatDataMap().put(type.name(), value);
    }

    @Override
    public int getStatValue(Stat type) {
        if (!hasStatValue(type)) return 0;
        return this.getStatDataMap().getInt(type.name());
    }

    @Override
    public boolean hasStatValue(Stat type) {
        return this.getStatDataMap().containsKey(type.name());
    }

    @Override
    public int getStatPoint() {
        return this.getStatDataMap().getInt(StaticValue.STAT_POINT_KEY);
    }

    @Override
    public void setStatPoint(int statPoint) {
        this.getStatDataMap().put(StaticValue.STAT_POINT_KEY, statPoint);
    }

    @Override
    public void addStatPoint(int statPoint) {
        this.setStatPoint(this.getStatPoint() + statPoint);
    }

    public A_DataMap getStatDataMap() {
        return this.getPlayer().getDataMap(UndefinedWorldCorePlugin.instance).getDataMap(StaticValue.STAT_MAP_KEY);
    }

    /**
     * 플레이어의 스텟을 attribute에 적용시키는 함수
     */
    @Override
    public void applyStatToAttribute() {
        for (AttributeType type : AttributeType.values()) {
            this.setBaseAttributeValue(type, ContainerType.STAT, 0);
        }

        for (Stat stat : Stat.stats()) {
            double value = getStatValue(stat);

            stat.map().forEach((k, v) -> {
                this.addBaseAttributeValue(k, ContainerType.STAT, v * value);
            });
        }
    }
}
