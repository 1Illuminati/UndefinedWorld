package org.red.minecraft.uw.core.combat.buff;

import org.red.minecraft.dellarte.library.entity.A_Entity;

/**
 * 침묵(수속성) 디버프 — 상태 마커.
 * 실제 효과(스킬 사용 차단)는 SkillEngine.runSkill 진입부에서 처리한다.
 */
public class SilenceDebuff implements Buff {

    private final BuffContext ctx;

    public SilenceDebuff(BuffContext ctx) { this.ctx = ctx; }

    @Override public BuffContext context() { return ctx; }
    @Override public BuffType type()       { return BuffType.SILENCE; }
    @Override public int tickCount()       { return 20; }

    @Override public void tick(A_Entity entity) { /* no-op: 상태 마커 */ }
}
