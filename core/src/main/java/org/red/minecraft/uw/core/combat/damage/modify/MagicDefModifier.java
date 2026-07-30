package org.red.minecraft.uw.core.combat.damage.modify;

import org.red.minecraft.uw.core.UndefinedWorldCore;
import org.red.minecraft.uw.core.UndefinedWorldCorePlugin;
import org.red.minecraft.uw.core.attribute.AttributeManager;
import org.red.minecraft.uw.core.attribute.AttributeType;
import org.red.minecraft.uw.core.combat.damage.DamageCTX;

public class MagicDefModifier implements DamageModifier {
    @Override
    public void apply(DamageCTX ctx, DamageModifierBus bus) {
        AttributeManager manager = UndefinedWorldCore.getAttributeManager(ctx.defender());

        double def = manager.getAttributeValue(AttributeType.MAGIC_DAMAGE_DEFENSE) + manager.getAttributeValue(AttributeType.ALL_DEFENSE);
        double defReduce = manager.getAttributeValue(AttributeType.MAGIC_DAMAGE_DEFENSE_REDUCE);
        double res = manager.getAttributeValue(AttributeType.MAGIC_DAMAGE_RESISTANCE) + manager.getAttributeValue(AttributeType.ALL_RESISTANCE);
        double resReduce = manager.getAttributeValue(AttributeType.MAGIC_DAMAGE_RESISTANCE_REDUCE);

        double finalDef = ((def - defReduce) / (def + ctx.damage()) * ctx.damage());
        double finalRes = 1 - ((res - resReduce) / 100);

        ctx.setDamage((ctx.damage() - (finalDef > 0 ? finalDef : 0)) * finalRes);
        UndefinedWorldCorePlugin.sendLog(String.format("MagicDef def:%f, defRe:%f, res:%f, resRe:%f, fDef:%f, fRes:%f",
                def, defReduce, res, resReduce, finalDef, finalRes));
        UndefinedWorldCorePlugin.sendLog(ctx.toString());
    }
}
