package org.red.minecraft.uw.core.skill.effect;

import org.red.minecraft.uw.core.skill.CTXType;
import org.red.minecraft.uw.core.skill.SkillCTX;

import java.util.concurrent.CompletableFuture;

public class PierceIncreaseEffect implements Effect{
    private final int increase;
    public PierceIncreaseEffect(int increase) {
        this.increase = increase;
    }

    @Override
    public CompletableFuture<EffectResult> execute(SkillCTX ctx) {
        int pierce = ctx.getCTX(CTXType.PIERCE);
        ctx.setCTX(CTXType.PIERCE, pierce + this.increase);
        return CompletableFuture.completedFuture(EffectResult.SUCCESS);
    }

    @Override
    public EffectType[] getEffectTypes() {
        return new EffectType[]{EffectType.MODIFIER};
    }

}
