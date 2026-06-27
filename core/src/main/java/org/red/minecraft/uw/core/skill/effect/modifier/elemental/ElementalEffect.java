package org.red.minecraft.uw.core.skill.effect.modifier.elemental;

import org.red.minecraft.uw.core.combat.ElementalType;
import org.red.minecraft.uw.core.skill.CTXType;
import org.red.minecraft.uw.core.skill.effect.modifier.SimpleModifierEffect;

public class ElementalEffect extends SimpleModifierEffect<ElementalType> {
    public ElementalEffect(ElementalType type) {
        super(CTXType.ELEMENTAL, type);
    }
}
