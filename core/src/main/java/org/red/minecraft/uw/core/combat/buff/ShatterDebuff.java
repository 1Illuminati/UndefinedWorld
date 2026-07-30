package org.red.minecraft.uw.core.combat.buff;

import org.red.minecraft.dellarte.library.entity.A_Entity;

/**
 * 파쇄(땅속성) 디버프 — 상태 마커, 중첩 가능.
 * 중첩 수는 BuffContext.level 로 표현한다. 누적은 BuffManager 가 하므로
 * 호출자(ElementalPostProcessor)는 "이번에 추가할 중첩 수"인 1만 넘긴다.
 * 실제 효과(땅속성 피해 증가)는 ShatterDefModifier (DamageModifierBus) 에서 처리한다.
 *
 * 현재 유일한 StackableBuff 구현체다.
 */
public class ShatterDebuff implements StackableBuff {

    /** 중첩 상한 (사용자 확정: 10 = 최대 +50%). 중첩당 증가율은 ShatterDefModifier 참조 */
    private static final int MAX_STACK = 10;

    private final BuffContext ctx;

    public ShatterDebuff(BuffContext ctx) { this.ctx = ctx; }

    @Override public BuffContext context() { return ctx; }
    @Override public BuffType type()       { return BuffType.SHATTER; }
    @Override public String getName()      { return type().name(); }
    @Override public int tickCount()       { return 20; }
    @Override public int maxStack()        { return MAX_STACK; }

    /** 현재 중첩 수 */
    public int getStacks() { return ctx.level(); }

    @Override public void tick(A_Entity entity) { /* no-op: 상태 마커 */ }
}
