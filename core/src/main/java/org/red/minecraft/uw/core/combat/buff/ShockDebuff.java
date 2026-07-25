package org.red.minecraft.uw.core.combat.buff;

import org.red.minecraft.dellarte.library.entity.A_Entity;

/**
 * 감전(번개속성) 디버프 — 상태 마커.
 * 실제 효과(추가 데미지 15%, 연쇄)는 데미지 파이프라인에서 처리한다:
 *   - 추가 데미지: ShockedDefModifier (DamageModifierBus)
 *   - 연쇄 효과: ElementalPostProcessor (DamageProcess 후처리)
 */
public class ShockDebuff implements Buff {

    private final BuffContext ctx;

    public ShockDebuff(BuffContext ctx) { this.ctx = ctx; }

    @Override public BuffContext context() { return ctx; }
    @Override public BuffType type()       { return BuffType.SHOCK; }
    @Override public int tickCount()       { return 20; }

    @Override public void tick(A_Entity entity) { /* no-op: 상태 마커 */ }
}
