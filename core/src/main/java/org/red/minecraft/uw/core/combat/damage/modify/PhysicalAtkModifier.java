package org.red.minecraft.uw.core.combat.damage.modify;

import org.red.minecraft.uw.core.UndefinedWorldCore;
import org.red.minecraft.uw.core.UndefinedWorldCorePlugin;
import org.red.minecraft.uw.core.attribute.AttributeManager;
import org.red.minecraft.uw.core.attribute.AttributeType;
import org.red.minecraft.uw.core.combat.damage.DamageCTX;

public class PhysicalAtkModifier implements DamageModifier{
    @Override
    public void apply(DamageCTX ctx, DamageModifierBus bus) {
        AttributeManager manager = UndefinedWorldCore.getAttributeManager(ctx.attacker());

        double atk = manager.getAttributeValue(AttributeType.PHYSICS_DAMAGE) + manager.getAttributeValue(AttributeType.ALL_DAMAGE);
        double atkReduce = manager.getAttributeValue(AttributeType.PHYSICS_DAMAGE_REDUCE);
        double mul = manager.getAttributeValue(AttributeType.PHYSICS_DAMAGE_MULTIPLY) + manager.getAttributeValue(AttributeType.ALL_DAMAGE_MULTIPLY);
        double mulReduce = manager.getAttributeValue(AttributeType.PHYSICS_DAMAGE_MULTIPLY_REDUCE);

        double finalAtk = (ctx.damage() + atk) * (1 - atkReduce);
        double finalMul = 1 + ((mul - mulReduce) / 100);

        final double result = (finalAtk * finalMul * ctx.scale());
        ctx.setDamage(result);
        UndefinedWorldCorePlugin.sendLog(String.format("PhysicalAtk atk:%f, atkRe:%f, mul:%f, mulRe:%f, fAtk:%f, fMul:%f, result:%f",
                atk, atkReduce, mul, mulReduce, finalAtk, finalMul, result));
        UndefinedWorldCorePlugin.sendLog(ctx.toString());
    }
}
