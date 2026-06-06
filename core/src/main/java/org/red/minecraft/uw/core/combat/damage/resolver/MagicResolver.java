package org.red.minecraft.uw.core.combat.damage.resolver;

import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.uw.core.attribute.AttributeManager;
import org.red.minecraft.uw.core.attribute.AttributeType;
import org.red.minecraft.uw.core.combat.damage.DamageType;

public class MagicResolver extends CriticalResolver {
    public MagicResolver(A_Entity entity) {
        super(entity);
    }

    @Override
    public DamageType getType() {
        return DamageType.MAGIC;
    }

    @Override
    public double resolveDefDamage(double originDamage) {
        AttributeManager manager = new AttributeManager(this.getEntity());

        double def = manager.getAttributeValue(AttributeType.MAGIC_DAMAGE_DEFENSE);
        double defReduce = manager.getAttributeValue(AttributeType.MAGIC_DAMAGE_DEFENSE_REDUCE);
        double res = manager.getAttributeValue(AttributeType.MAGIC_DAMAGE_RESISTANCE);
        double resReduce = manager.getAttributeValue(AttributeType.MAGIC_DAMAGE_RESISTANCE_REDUCE);

        double finalDef = ((def - defReduce) / (def + originDamage) * originDamage);
        double finalRes = 1 - ((res - resReduce) / 100);

        return (originDamage - finalDef > 0 ? finalDef : 0) * finalRes;
    }

    @Override
    public double resolveAtkDamage(double originDamage) {
        AttributeManager manager = new AttributeManager(this.getEntity());

        double atk = manager.getAttributeValue(AttributeType.MAGIC_DAMAGE);
        double atkReduce = manager.getAttributeValue(AttributeType.MAGIC_DAMAGE_REDUCE);
        double mul = manager.getAttributeValue(AttributeType.MAGIC_DAMAGE_MULTIPLY);
        double mulReduce = manager.getAttributeValue(AttributeType.MAGIC_DAMAGE_MULTIPLY_REDUCE);

        double finalAtk = atk * originDamage * (1 - atkReduce);
        double finalMul = 1 + ((mul - mulReduce) / 100);

        return finalAtk * finalMul;
    }
}
