package org.red.minecraft.uw.core.combat.damage.modify;

import org.red.minecraft.uw.core.UndefinedWorldCore;
import org.red.minecraft.uw.core.UndefinedWorldCorePlugin;
import org.red.minecraft.uw.core.attribute.AttributeManager;
import org.red.minecraft.uw.core.attribute.AttributeType;
import org.red.minecraft.uw.core.combat.damage.DamageCTX;

public class TrueDamageModifier implements DamageModifier {
    @Override
    public void apply(DamageCTX ctx, DamageModifierBus bus) {
        AttributeManager attackerManager = UndefinedWorldCore.getAttributeManager(ctx.attacker());

        double damage = ctx.finalDamage();
        double healthTrueAtk = attackerManager.getAttributeValue(AttributeType.HEALTH_TRUE_DAMAGE);
        double trueAtk = attackerManager.getAttributeValue(AttributeType.TRUE_DAMAGE);
        double trueMul = attackerManager.getAttributeValue(AttributeType.TRUE_DAMAGE_MULTIPLY);
        double healthTrueDamage = ctx.defender().getMaxHealth() * healthTrueAtk;

        ctx.setDamage(Math.max(damage, Math.max(trueAtk * trueMul, healthTrueDamage)));
        UndefinedWorldCorePlugin.sendLog(ctx.toString());
    }
}
