package org.red.minecraft.uw.core.combat.buff;

import org.red.minecraft.dellarte.library.entity.A_Entity;

public class GlowingBuff implements Buff {

    private final BuffContext ctx;

    /** 적용 전 발광 상태. 무조건 false로 되돌리면 다른 원인(주시의 화살 등)으로 켜져 있던 발광을 꺼버린다. */
    private boolean previousGlowing;

    public GlowingBuff(BuffContext ctx) { this.ctx = ctx; }

    @Override public BuffContext context() { return ctx; }
    @Override public BuffType type()       { return BuffType.GLOWING; }
    @Override public String getName()      { return type().name(); }
    @Override public int tickCount()       { return 100; }

    @Override public void tick(A_Entity entity) { /* no-op: 타이머 유지용 */ }

    @Override public void onApply(A_Entity entity)  {
        this.previousGlowing = entity.isGlowing();
        entity.setGlowing(true);
    }

    @Override public void onRemove(A_Entity entity, BuffRemoveReason reason) {
        entity.setGlowing(this.previousGlowing);
    }
}
