package org.red.minecraft.uw.core.combat.buff;

import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.uw.core.attribute.AttributeType;

public class PhysicalDamageBuff extends AttributeBuff {

    public PhysicalDamageBuff(BuffContext ctx) {
        super(ctx, AttributeType.PHYSICS_DAMAGE, ctx.level() * 20);
    }

    @Override
    public BuffType type() {
        return BuffType.PHYSICAL_DAMAGE_BUFF;
    }
}
