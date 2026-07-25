package org.red.minecraft.uw.core.combat.buff;

import org.red.minecraft.dellarte.library.entity.A_Entity;

/**
 * 무적 버프 — 상태 마커.
 * 캐스팅 취소 예외 판정에 사용된다 (CastingManager.onAttacked 참조).
 * todo 무적 상태의 데미지 무효 처리 여부는 미정 — 확정 시 DamageProcess에 판정 추가
 */
public class InvincibleBuff implements Buff {

    private final BuffContext ctx;

    public InvincibleBuff(BuffContext ctx) { this.ctx = ctx; }

    @Override public BuffContext context() { return ctx; }
    @Override public BuffType type()       { return BuffType.INVINCIBLE; }
    @Override public int tickCount()       { return 20; }

    @Override public void tick(A_Entity entity) { /* no-op: 상태 마커 */ }
}
