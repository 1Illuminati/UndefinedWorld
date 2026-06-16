package org.red.minecraft.uw.core.combat.buff;

import org.red.minecraft.dellarte.library.entity.A_Entity;

public class GlowingBuff implements Buff {

    private final BuffContext ctx;

    public GlowingBuff(BuffContext ctx) { this.ctx = ctx; }

    @Override public BuffContext context() { return ctx; }
    @Override public BuffType type()       { return BuffType.GLOWING; }
    @Override public int tickCount()       { return 100; }

    @Override public void tick(A_Entity entity) { /* no-op: 타이머 유지용 */ }

    @Override public void onApply(A_Entity entity)  { entity.setGlowing(true); }
    @Override public void onRemove(A_Entity entity, BuffRemoveReason reason) { entity.setGlowing(false); }
}
