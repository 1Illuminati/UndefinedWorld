package org.red.minecraft.uw.core.skill.condition;

import org.red.minecraft.uw.core.skill.SkillCTX;

public interface Condition {
    boolean test(SkillCTX ctx);
}
