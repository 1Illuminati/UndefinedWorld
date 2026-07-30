package org.red.minecraft.uw.core.combat.buff;

import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.dellarte.library.entity.A_LivingEntity;

public class RegenerationBuff implements Buff {

    private final BuffContext ctx;

    public RegenerationBuff(BuffContext ctx) { this.ctx = ctx; }

    @Override public BuffContext context() { return ctx; }
    @Override public BuffType type()       { return BuffType.REGENERATION; }
    @Override public String getName()      { return type().name(); }
    @Override public int tickCount()       { return 20; }

    @Override
    public void tick(A_Entity entity) {
        if (!(entity instanceof A_LivingEntity living) || living.isDead()) return;

        double max = living.getMaxHealth();
        double current = living.getHealth();
        // 최대체력이 줄어든 직후 등 current > max 상태에서 min()을 그대로 쓰면
        // 회복 버프가 오히려 체력을 깎는다. 회복 여지가 없으면 아무것도 하지 않는다.
        if (max <= 0 || current >= max) return;

        living.setHealth(Math.min(max, current + ctx.level()));
    }
}
