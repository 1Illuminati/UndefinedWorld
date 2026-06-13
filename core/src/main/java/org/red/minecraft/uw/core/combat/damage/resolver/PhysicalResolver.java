package org.red.minecraft.uw.core.combat.damage.resolver;

import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.uw.core.UndefinedWorldCore;
import org.red.minecraft.uw.core.UndefinedWorldCorePlugin;
import org.red.minecraft.uw.core.attribute.AttributeHolder;
import org.red.minecraft.uw.core.attribute.AttributeType;
import org.red.minecraft.uw.core.combat.damage.DamageType;

public class PhysicalResolver extends CriticalResolver {
    public PhysicalResolver(A_Entity entity) {
        super(entity);
    }

    @Override
    public DamageType getType() {
        return DamageType.PHYSICAL;
    }

    @Override
    public double resolveDefDamage(double originDamage) {
        AttributeHolder holder = UndefinedWorldCore.getAttributeHolder(this.getEntity());

        double def = holder.getAttributeValue(AttributeType.PHYSICS_DAMAGE_DEFENSE);
        double defReduce = holder.getAttributeValue(AttributeType.PHYSICS_DAMAGE_DEFENSE_REDUCE);
        double res = holder.getAttributeValue(AttributeType.PHYSICS_DAMAGE_RESISTANCE);
        double resReduce = holder.getAttributeValue(AttributeType.PHYSICS_DAMAGE_RESISTANCE_REDUCE);

        double finalDef = ((def - defReduce) / (def + originDamage) * originDamage);
        double finalRes = 1 - ((res - resReduce) / 100);

        final double result = (originDamage - (finalDef > 0 ? finalDef : 0)) * finalRes;

        UndefinedWorldCorePlugin.sendLog(String.format("ResolvePhysicalDef - def:%f, DefRd:%f, res:%f, ResRd:%f, fDef:%f, fRes:%f, result:%f",
                def, defReduce, res, resReduce, finalDef, finalRes, result));

        return result;
    }

    @Override
    public double resolveAtkDamage(double originDamage, double scale) {
        AttributeHolder holder = UndefinedWorldCore.getAttributeHolder(this.getEntity());

        double atk = holder.getAttributeValue(AttributeType.PHYSICS_DAMAGE);
        double atkReduce = holder.getAttributeValue(AttributeType.PHYSICS_DAMAGE_REDUCE);
        double mul = holder.getAttributeValue(AttributeType.PHYSICS_DAMAGE_MULTIPLY);
        double mulReduce = holder.getAttributeValue(AttributeType.PHYSICS_DAMAGE_MULTIPLY_REDUCE);

        double finalAtk = atk * scale * (1 - atkReduce) + (originDamage * scale);
        double finalMul = 1 + ((mul - mulReduce) / 100);

        final double result = finalAtk * finalMul;
        UndefinedWorldCorePlugin.sendLog(String.format("ResolvePhysicalAtk - origin:%f, scale:%f, atk:%f, atkRd:%f, mul:%f, mulRd:%f, fAtk:%f, fMul:%f, result:%f",
                originDamage, scale, atk, atkReduce, mul, mulReduce, finalAtk, finalMul, result));

        return result;
    }
}
