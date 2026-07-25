package org.red.minecraft.uw.core.skill.effect;

import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.uw.core.UndefinedWorldCore;
import org.red.minecraft.uw.core.combat.buff.BuffContext;
import org.red.minecraft.uw.core.combat.buff.BuffType;
import org.red.minecraft.uw.core.skill.CTXType;
import org.red.minecraft.uw.core.skill.SkillCTX;

import java.util.concurrent.CompletableFuture;

/**
 * 버프/디버프 부여 이펙트 — 자신(self=true) 또는 LAST_TARGET_INFO 대상에게 버프를 부여한다.
 * 지속시간은 틱 단위. 레벨 기반 버프(GLOWING/POISON/REGENERATION 등)에 사용한다.
 * (ATTRIBUTE_BUFF처럼 추가 데이터가 필요한 버프는 별도 이펙트 확정 후 — todo)
 */
public class BuffEffect implements Effect {

    private final BuffType buffType;
    private final int level;
    private final long durationTicks;
    private final boolean self;

    public BuffEffect(BuffType buffType, int level, long durationTicks, boolean self) {
        this.buffType = buffType;
        this.level = level;
        this.durationTicks = durationTicks;
        this.self = self;
    }

    @Override
    public CompletableFuture<EffectResult> execute(SkillCTX ctx) {
        A_Entity caster = ctx.getCTX(CTXType.CASTER);

        A_Entity[] targets;
        if (self) {
            targets = new A_Entity[]{caster};
        } else {
            targets = ctx.hasCTX(CTXType.LAST_TARGET_INFO) ? ctx.getCTX(CTXType.LAST_TARGET_INFO) : null;
            if (targets == null || targets.length == 0) return CompletableFuture.completedFuture(EffectResult.FAIL);
        }

        BuffContext buffCtx = BuffContext.builder(level).caster(caster).build();
        for (A_Entity target : targets) {
            UndefinedWorldCore.getBuffManager().applyBuff(target, buffType, buffCtx, durationTicks, false);
        }

        return CompletableFuture.completedFuture(EffectResult.SUCCESS);
    }

    @Override
    public EffectType[] getEffectTypes() {
        return new EffectType[]{self ? EffectType.BUFF : EffectType.DEBUFF};
    }
}
