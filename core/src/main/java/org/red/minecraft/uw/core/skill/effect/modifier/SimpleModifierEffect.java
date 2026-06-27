package org.red.minecraft.uw.core.skill.effect.modifier;

import org.red.minecraft.uw.core.skill.CTXType;
import org.red.minecraft.uw.core.skill.SkillCTX;
import org.red.minecraft.uw.core.skill.effect.Effect;
import org.red.minecraft.uw.core.skill.effect.EffectResult;

import java.util.concurrent.CompletableFuture;

public class SimpleModifierEffect<T> implements Effect {
    private final CTXType type;
    private final T value;
    public SimpleModifierEffect(CTXType type, T value) {
        this.type = type;
        this.value = value;

        if (type.clazz != value.getClass())
            throw new IllegalArgumentException("SimpleModifierEffect Error:" + type.clazz + " != " + value.getClass());
    }

    public CTXType getType() {
        return type;
    }

    public T value() {
        return value;
    }

    @Override
    public CompletableFuture<EffectResult> execute(SkillCTX ctx) {
        ctx.setCTX(type, this.value);
        return CompletableFuture.completedFuture(EffectResult.SUCCESS);
    }
}
