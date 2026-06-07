package org.red.minecraft.uw.core.skill.effect;

import org.red.minecraft.uw.core.skill.CTXType;
import org.red.minecraft.uw.core.skill.SkillCTX;

import java.util.concurrent.CompletableFuture;

public class SizeIncreaseEffect implements Effect{
    private final double increase;
    public SizeIncreaseEffect(double increase) {
        this.increase = increase;
    }

    @Override
    public CompletableFuture<EffectResult> execute(SkillCTX ctx) {
        double size = ctx.getCTX(CTXType.SIZE);
        ctx.setCTX(CTXType.SIZE, size + this.increase);
        return CompletableFuture.completedFuture(EffectResult.SUCCESS);
    }

    @Override
    public String getID() {
        return "size_increase";
    }
}
