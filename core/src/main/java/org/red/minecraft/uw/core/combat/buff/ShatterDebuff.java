package org.red.minecraft.uw.core.combat.buff;

import org.red.minecraft.dellarte.library.entity.A_Entity;

/**
 * 파쇄(땅속성) 디버프 — 상태 마커, 중첩 가능.
 * 중첩 수는 BuffContext.level 로 표현한다 (재적용 시 level+1 로 갱신, ElementalPostProcessor 참조).
 * 실제 효과(땅속성 피해 증가)는 ShatterDefModifier (DamageModifierBus) 에서 처리한다.
 */
public class ShatterDebuff implements Buff {

    private final BuffContext ctx;

    public ShatterDebuff(BuffContext ctx) { this.ctx = ctx; }

    @Override public BuffContext context() { return ctx; }
    @Override public BuffType type()       { return BuffType.SHATTER; }
    @Override public int tickCount()       { return 20; }

    /** 현재 중첩 수 */
    public int getStacks() { return ctx.level(); }

    @Override public void tick(A_Entity entity) { /* no-op: 상태 마커 */ }
}
