package org.red.minecraft.uw.core.item.consume.buff;

import org.red.minecraft.dellarte.library.util.A_DataMap;
import org.red.minecraft.uw.core.combat.buff.BuffType;
import org.red.minecraft.uw.core.item.consume.ConsumeItem;

public interface BuffItem extends ConsumeItem {
    BuffType getBuffType();
    int level();
    A_DataMap data();
}
