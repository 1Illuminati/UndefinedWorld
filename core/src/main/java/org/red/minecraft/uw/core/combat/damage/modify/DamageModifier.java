package org.red.minecraft.uw.core.combat.damage.modify;

import org.red.minecraft.uw.core.combat.damage.DamageCTX;

@FunctionalInterface
public interface DamageModifier {
    void apply(DamageCTX ctx, DamageModifierBus bus);
}
