package org.red.minecraft.uw.core.combat.buff;

import org.red.minecraft.dellarte.library.entity.A_Entity;

/**
 * 무적 버프 — 상태 마커. 이 클래스 자체는 아무 동작도 하지 않고, <b>존재 여부</b>만 두 곳에서 읽힌다.
 *
 * <ol>
 *   <li>캐스팅 취소 예외 — {@code CastingManager.onAttacked}</li>
 *   <li><b>데미지 무효</b> (Process.md §2.10 확정) — {@code DamageProcess.isInvincibleBlocked}.
 *       회피/막기보다 먼저 판정하며, 무효에서 빠지는 데미지 타입은
 *       {@code DamageProcess.INVINCIBLE_IGNORED_TYPES} 에 정의되어 있다.</li>
 * </ol>
 *
 * 판정 자체를 여기 두지 않은 이유는 방향 때문이다. {@code combat.damage → combat.buff} 로만
 * 의존이 흐르고 있어서(예: DamageModifierBus 가 SHOCK/SHATTER 를 읽는다), 버프가 DamageType 을
 * 알게 되면 두 패키지가 양방향이 된다.
 */
public class InvincibleBuff implements Buff {

    private final BuffContext ctx;

    public InvincibleBuff(BuffContext ctx) { this.ctx = ctx; }

    @Override public BuffContext context() { return ctx; }
    @Override public BuffType type()       { return BuffType.INVINCIBLE; }
    @Override public String getName()      { return type().name(); }
    @Override public int tickCount()       { return 20; }

    @Override public void tick(A_Entity entity) { /* no-op: 상태 마커 */ }
}
