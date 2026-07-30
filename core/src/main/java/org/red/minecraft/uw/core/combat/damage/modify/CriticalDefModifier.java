package org.red.minecraft.uw.core.combat.damage.modify;

import org.red.minecraft.uw.core.UndefinedWorldCore;
import org.red.minecraft.uw.core.UndefinedWorldCorePlugin;
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

        // PhysicalDefModifier/MagicDefModifier와 동일한 형태: finalDef는 "감소량"이며 현재 데미지에서 차감한다.
        // (기존 식은 finalDef에 "남은 데미지"를 담고 다시 차감해 감소량만 남아, 치명타 방어력이 0일 때 데미지가 0이 됐다)
        double finalDef = ((def - defReduce) / (def + ctx.damage()) * ctx.damage());
        double finalRes = 1 - ((res - resReduce) / 100);

        ctx.setDamage((ctx.damage() - (finalDef > 0 ? finalDef : 0)) * finalRes);
        UndefinedWorldCorePlugin.sendLog(String.format("CriticalDef def:%f, defRe:%f, res:%f, resRe:%f, fDef:%f, fRes:%f",
                def, defReduce, res, resReduce, finalDef, finalRes));
        UndefinedWorldCorePlugin.sendLog(ctx.toString());
    }
}
