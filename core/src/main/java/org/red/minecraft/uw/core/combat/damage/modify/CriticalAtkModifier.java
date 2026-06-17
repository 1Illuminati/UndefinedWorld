package org.red.minecraft.uw.core.combat.damage.modify;

import org.red.minecraft.uw.core.UndefinedWorldCore;
import org.red.minecraft.uw.core.attribute.AttributeManager;
import org.red.minecraft.uw.core.attribute.AttributeType;
import org.red.minecraft.uw.core.combat.damage.DamageCTX;

public class CriticalAtkModifier implements DamageModifier{
    @Override
    public void apply(DamageCTX ctx, DamageModifierBus bus) {
        AttributeManager manager = UndefinedWorldCore.getAttributeManager(ctx.attacker());

        double cri = manager.getAttributeValue(AttributeType.CRITICAL_DAMAGE);
        double criReduce = manager.getAttributeValue(AttributeType.CRITICAL_DAMAGE_REDUCE);
        double mul = manager.getAttributeValue(AttributeType.CRITICAL_DAMAGE_MULTIPLY);
        double mulReduce = manager.getAttributeValue(AttributeType.CRITICAL_DAMAGE_MULTIPLY_REDUCE);

        double finalCri = Math.max(0, cri - criReduce);
        double finalMul = 1 + ((mul - mulReduce) / 100);

        ctx.setDamage(ctx.damage() * (1 + finalCri * finalMul));
    }
}
