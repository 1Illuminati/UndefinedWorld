package org.red.minecraft.uw.core.skill.effect.conversion;

import org.red.minecraft.uw.core.skill.SkillDefinition;

public class MergeEffect extends ConversionEffect {
    private final int mergeNum;
    public MergeEffect(int mergeNum) {
        this.mergeNum = mergeNum;
    }

    @Override
    public void setConversion(SkillDefinition skillDef, SkillDefinition.SkillNode effectNode) {

    }
}
