package org.red.minecraft.uw.core.combat.damage.resolver;

import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.uw.core.attribute.AttributeManager;
import org.red.minecraft.uw.core.attribute.AttributeType;

public abstract class CriticalResolver extends DamageResolver {
    public CriticalResolver(A_Entity entity) {
        super(entity);
    }

    public double resolveDefCritDamage(double originDamage) {
        AttributeManager manager = new AttributeManager(this.getEntity());

        double damage = this.resolveDefDamage(originDamage);
        double def = manager.getAttributeValue(AttributeType.CRITICAL_DAMAGE_DEFENSE);
        double defReduce = manager.getAttributeValue(AttributeType.CRITICAL_DAMAGE_DEFENSE_REDUCE);
        double res = manager.getAttributeValue(AttributeType.CRITICAL_DAMAGE_RESISTANCE);
        double resReduce = manager.getAttributeValue(AttributeType.CRITICAL_DAMAGE_RESISTANCE_REDUCE);

        double finalDef = Math.max(damage - ((def - defReduce) / (def + damage) * damage), 0);
        double finalRes = 1 - ((res - resReduce) / 100);

        return (damage - finalDef) * finalRes;
    }

    public double resolveAtkCritDamage(double baseDamage) {
        AttributeManager manager = new AttributeManager(this.getEntity());

        double damage = this.resolveAtkDamage(baseDamage);
        double cri = manager.getAttributeValue(AttributeType.CRITICAL_DAMAGE);
        double criReduce = manager.getAttributeValue(AttributeType.CRITICAL_DAMAGE_REDUCE);
        double mul = manager.getAttributeValue(AttributeType.CRITICAL_DAMAGE_MUTIPLY);
        double mulReduce = manager.getAttributeValue(AttributeType.CRITICAL_DAMAGE_MUTIPLY_REDUCE);

        double finalCri = Math.max(0, cri - criReduce);
        double finalMul = 1 + ((mul - mulReduce) / 100);

        return damage * (1 + finalCri * finalMul);
    }
}
