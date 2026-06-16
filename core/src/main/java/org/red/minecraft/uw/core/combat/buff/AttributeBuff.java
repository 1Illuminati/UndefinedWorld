package org.red.minecraft.uw.core.combat.buff;

import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.uw.core.UndefinedWorldCore;
import org.red.minecraft.uw.core.attribute.AttributeHolder;
import org.red.minecraft.uw.core.attribute.AttributeType;

public abstract class AttributeBuff implements Buff {
    private final BuffContext ctx;
    private final AttributeType type;
    private final double value;

    public AttributeBuff(BuffContext ctx, AttributeType type, double value) {
        this.ctx = ctx;
        this.type = type;
        this.value = value;
    }

    @Override
    public BuffContext context() {
        return this.ctx;
    }

    public AttributeType getAttributeType() {
        return this.type;
    }

    public double getValue() {
        return this.value;
    }

    @Override public void onApply(A_Entity entity)  {
        AttributeHolder holder = UndefinedWorldCore.getAttributeHolder(entity);
        //todo
    }
    @Override public void onRemove(A_Entity entity, BuffRemoveReason reason) {
        //todo
    }

    @Override
    public int tickCount() { return 100; }
    @Override
    public void tick(A_Entity entity) {}
}
