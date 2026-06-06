package org.red.minecraft.uw.core.player;

import org.red.minecraft.dellarte.library.entity.A_Player;
import org.red.minecraft.dellarte.library.util.A_DataMap;
import org.red.minecraft.uw.core.UndefinedWorldCorePlugin;
import org.red.minecraft.uw.core.attribute.AttributeHolder;
import org.red.minecraft.uw.core.attribute.AttributeType;

import java.util.Arrays;
import java.util.Collection;

/**
 * 플레이어용 함수 모아두는 클래스
 */
public final class PlayerHelper implements AttributeHolder {
    private final A_Player player;
    public PlayerHelper(A_Player player) {
        this.player = player;
    }

    public A_Player getPlayer() {
        return player;
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
        return player.getDataMap(UndefinedWorldCorePlugin.instance);
    }
}
