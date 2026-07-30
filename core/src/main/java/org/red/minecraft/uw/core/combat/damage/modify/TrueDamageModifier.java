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
        // todo HEALTH_TRUE_DAMAGE 단위 확인 필요 (비율 1.0 = 100% 인지, % 값 100 = 100% 인지)
        double healthTrueDamage = ctx.defender().getMaxHealth() * healthTrueAtk;

        // TRUE_DAMAGE_MULTIPLY("고정공격력 증폭")은 배율 % 규칙(Process.md 2.5)을 따른다.
        // 기존 식(trueAtk * trueMul)은 증폭이 0(기본값)일 때 고정공격력 자체가 0이 되어 무효화됐다.
        double finalTrueAtk = trueAtk * (1 + (trueMul / 100));

        ctx.setDamage(Math.max(damage, Math.max(finalTrueAtk, healthTrueDamage)));
        UndefinedWorldCorePlugin.sendLog(ctx.toString());
    }
}
