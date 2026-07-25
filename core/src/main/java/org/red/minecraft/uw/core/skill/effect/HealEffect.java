package org.red.minecraft.uw.core.skill.effect;

import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.dellarte.library.entity.A_LivingEntity;
import org.red.minecraft.uw.core.skill.CTXType;
import org.red.minecraft.uw.core.skill.SkillCTX;

import java.util.concurrent.CompletableFuture;

/**
 * 힐 이펙트 — 자신(self=true) 또는 LAST_TARGET_INFO 대상의 체력을 회복한다.
 * todo HEALING_REDUCE(치유 감소) 속성 반영 여부 확정 필요
 */
public class HealEffect implements Effect {

    private final double amount;
    private final boolean self;

    public HealEffect(double amount, boolean self) {
        this.amount = amount;
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

        for (A_Entity target : targets) {
            A_LivingEntity living = target.getALivingEntity();
            if (living == null || living.isDead()) continue;

            living.setHealth(Math.min(living.getMaxHealth(), living.getHealth() + amount));
        }

        return CompletableFuture.completedFuture(EffectResult.SUCCESS);
    }

    @Override
    public EffectType[] getEffectTypes() {
        return new EffectType[]{EffectType.BUFF};
    }
}
