package org.red.minecraft.uw.core.combat.damage.modify;

import org.red.minecraft.uw.core.UndefinedWorldCore;
import org.red.minecraft.uw.core.UndefinedWorldCorePlugin;
import org.red.minecraft.uw.core.attribute.AttributeManager;
import org.red.minecraft.uw.core.attribute.AttributeType;
import org.red.minecraft.uw.core.combat.ElementalType;
import org.red.minecraft.uw.core.combat.damage.DamageCTX;

/**
 * 속성 공격력 계산 (공통 ELEMENT_* + 개별속성 <ELEM>_* 합산).
 * PhysicalAtkModifier 패턴을 따르되, scale은 기본 공격 Modifier가 이미 적용했으므로 중복 적용하지 않는다.
 * 등록 조건: ctx.elementalType() != NONE && hasAttacker (DamageModifierBus.create)
 */
public class ElementalAtkModifier implements DamageModifier {
    @Override
    public void apply(DamageCTX ctx, DamageModifierBus bus) {
        ElementalType elem = ctx.elementalType();
        if (elem == ElementalType.NONE) return;

        AttributeManager manager = UndefinedWorldCore.getAttributeManager(ctx.attacker());

        double atk = manager.getAttributeValue(AttributeType.ELEMENT_DAMAGE) + manager.getAttributeValue(elem.damage);
        double atkReduce = manager.getAttributeValue(AttributeType.ELEMENT_DAMAGE_REDUCE) + manager.getAttributeValue(elem.damageReduce);
        double mul = manager.getAttributeValue(AttributeType.ELEMENT_DAMAGE_MULTIPLY) + manager.getAttributeValue(elem.damageMultiply);

        double finalAtk = (ctx.damage() + atk) * (1 - atkReduce);
        double finalMul = 1 + (mul / 100);

        ctx.setDamage(finalAtk * finalMul);
        UndefinedWorldCorePlugin.sendLog(String.format("ElementalAtk[%s] atk:%f, atkRe:%f, mul:%f, result:%f",
                elem.name(), atk, atkReduce, mul, ctx.damage()));
    }
}
