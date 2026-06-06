package org.red.minecraft.uw.core.skill.condition;

import org.red.minecraft.uw.core.skill.SkillCTX;
import org.red.minecraft.uw.core.util.HasID;

public interface Condition extends HasID {
    boolean test(SkillCTX ctx);
}
