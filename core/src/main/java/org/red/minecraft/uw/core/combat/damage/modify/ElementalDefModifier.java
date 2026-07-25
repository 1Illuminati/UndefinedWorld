package org.red.minecraft.uw.core.combat.damage.modify;

import org.red.minecraft.uw.core.UndefinedWorldCore;
import org.red.minecraft.uw.core.UndefinedWorldCorePlugin;
import org.red.minecraft.uw.core.attribute.AttributeManager;
import org.red.minecraft.uw.core.attribute.AttributeType;
import org.red.minecraft.uw.core.combat.ElementalType;
import org.red.minecraft.uw.core.combat.damage.DamageCTX;

/**
 * 속성 방어력/저항 계산 (공통 ELEMENT_* + 개별속성 <ELEM>_* 합산).
 * PhysicalDefModifier 패턴을 따른다. 속성에는 DEFENSE_REDUCE 계열 속성이 없어 defReduce는 없다.
 * 등록 조건: ctx.elementalType() != NONE (DamageModifierBus.create)
 */
public class ElementalDefModifier implements DamageModifier {
    @Override
    public void apply(DamageCTX ctx, DamageModifierBus bus) {
        ElementalType elem = ctx.elementalType();
        if (elem == ElementalType.NONE) return;

        AttributeManager manager = UndefinedWorldCore.getAttributeManager(ctx.defender());

        double def = manager.getAttributeValue(AttributeType.ELEMENT_DAMAGE_DEFENSE) + manager.getAttributeValue(elem.defense);
        double res = manager.getAttributeValue(AttributeType.ELEMENT_DAMAGE_RESISTANCE) + manager.getAttributeValue(elem.resistance);
        double resReduce = manager.getAttributeValue(AttributeType.ELEMENT_DAMAGE_RESISTANCE_REDUCE) + manager.getAttributeValue(elem.resistanceReduce);

        double finalDef = (def / (def + ctx.damage()) * ctx.damage());
        double finalRes = 1 - ((res - resReduce) / 100);

        ctx.setDamage((ctx.damage() - (finalDef > 0 ? finalDef : 0)) * finalRes);
        UndefinedWorldCorePlugin.sendLog(String.format("ElementalDef[%s] def:%f, res:%f, resRe:%f, result:%f",
                elem.name(), def, res, resReduce, ctx.damage()));
    }
}
