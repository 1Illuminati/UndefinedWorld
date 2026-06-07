package org.red.minecraft.uw.core.skill.effect;

import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.uw.core.skill.SkillCTX;

import java.util.concurrent.CompletableFuture;

public class ThunderEffect implements Effect{
    @Override
    public CompletableFuture<EffectResult> execute(SkillCTX ctx) {
        return null;
    }

    @Override
    public String getID() {
        return "thunder";
    }
}
