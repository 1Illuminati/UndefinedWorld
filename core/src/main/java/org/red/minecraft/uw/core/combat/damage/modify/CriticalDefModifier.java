package org.red.minecraft.uw.core.combat.damage.modify;

import org.red.minecraft.uw.core.UndefinedWorldCore;
import org.red.minecraft.uw.core.attribute.AttributeManager;
import org.red.minecraft.uw.core.attribute.AttributeType;
import org.red.minecraft.uw.core.combat.damage.DamageCTX;

public class CriticalDefModifier implements DamageModifier{
    @Override
    public void apply(DamageCTX ctx, DamageModifierBus bus) {
        AttributeManager manager = UndefinedWorldCore.getAttributeManager(ctx.defender());

        double def = manager.getAttributeValue(AttributeType.CRITICAL_DAMAGE_DEFENSE);
        double defReduce = manager.getAttributeValue(AttributeType.CRITICAL_DAMAGE_DEFENSE_REDUCE);
        double res = manager.getAttributeValue(AttributeType.CRITICAL_DAMAGE_RESISTANCE);
        double resReduce = manager.getAttributeValue(AttributeType.CRITICAL_DAMAGE_RESISTANCE_REDUCE);

        double finalDef = Math.max(ctx.damage() - ((def - defReduce) / (def + ctx.damage()) * ctx.damage()), 0);
        double finalRes = 1 - ((res - resReduce) / 100);

        ctx.setDamage((ctx.damage() - finalDef) * finalRes);
    }
}
