package org.red.minecraft.uw.core.combat.buff;

import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.uw.core.UndefinedWorldCore;
import org.red.minecraft.uw.core.attribute.AttributeManager;
import org.red.minecraft.uw.core.attribute.AttributeType;

public class AttributeBuff implements Buff {
    private final BuffContext ctx;
    private final AttributeType type;
    private final double value;

    public AttributeBuff(BuffContext ctx) {
        this.ctx = ctx;
        this.type = ctx.get("type");
        this.value = ctx.get("value");
    }

    @Override
    public BuffContext context() {
        return this.ctx;
    }

    @Override
    public BuffType type() {
        return BuffType.ATTRIBUTE_BUFF;
    }

    public AttributeType getAttributeType() {
        return this.type;
    }

    public double getValue() {
        return this.value;
    }

    @Override public void onApply(A_Entity entity)  {
        AttributeManager manager = UndefinedWorldCore.getAttributeManager(entity);
        manager.addBaseAttributeValue(this.getAttributeType(), AttributeManager.ContainerType.BUFF, this.value);
    }
    @Override public void onRemove(A_Entity entity, BuffRemoveReason reason) {
        AttributeManager manager = UndefinedWorldCore.getAttributeManager(entity);
        manager.addBaseAttributeValue(this.getAttributeType(), AttributeManager.ContainerType.BUFF, -this.value);
    }

    @Override
    public int tickCount() { return 100; }
    @Override
    public void tick(A_Entity entity) {}
}
