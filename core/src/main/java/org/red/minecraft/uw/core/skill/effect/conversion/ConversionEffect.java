package org.red.minecraft.uw.core.skill.effect.conversion;

import org.red.minecraft.uw.core.skill.SkillCTX;
import org.red.minecraft.uw.core.skill.SkillDefinition;
import org.red.minecraft.uw.core.skill.effect.Effect;
import org.red.minecraft.uw.core.skill.effect.EffectResult;
import org.red.minecraft.uw.core.skill.effect.EffectType;

import java.util.concurrent.CompletableFuture;

public abstract class ConversionEffect implements Effect {
    public CompletableFuture<EffectResult> execute(SkillCTX ctx) {
        return CompletableFuture.completedFuture(EffectResult.SUCCESS);
    }

    public EffectType[] getEffectTypes() {
        return new EffectType[]{EffectType.CONVERSION};
    }

    public abstract void setConversion(SkillDefinition skillDef, SkillDefinition.SkillNode effectNode);
}
