package org.red.minecraft.uw.core.combat.buff;

import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.dellarte.library.entity.A_LivingEntity;

public class RegenerationBuff implements Buff {

    private final BuffContext ctx;

    public RegenerationBuff(BuffContext ctx) { this.ctx = ctx; }

    @Override public BuffContext context() { return ctx; }
    @Override public BuffType type()       { return BuffType.REGENERATION; }
    @Override public int tickCount()       { return 20; }

    @Override
    public void tick(A_Entity entity) {
        if (!(entity instanceof A_LivingEntity living) || living.isDead()) return;
        double max = living.getMaxHealth();
        living.setHealth(Math.min(max, living.getHealth() + ctx.level()));
    }
}
