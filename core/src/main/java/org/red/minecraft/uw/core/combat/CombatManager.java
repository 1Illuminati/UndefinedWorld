package org.red.minecraft.uw.core.combat;

import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.dellarte.library.entity.A_LivingEntity;
import org.red.minecraft.uw.core.attribute.AttributeManager;
import org.red.minecraft.uw.core.attribute.AttributeType;
import org.red.minecraft.uw.core.combat.damage.DamageAtkProcess;
import org.red.minecraft.uw.core.combat.damage.DamageProcess;
import org.red.minecraft.uw.core.combat.damage.DamageType;
import org.red.minecraft.uw.core.combat.damage.resolver.DamageResolver;
import org.red.minecraft.uw.core.combat.damage.resolver.MagicResolver;
import org.red.minecraft.uw.core.combat.damage.resolver.PhysicalResolver;

public final class CombatManager {
    public static void damage(A_LivingEntity entity, DamageType type, double damage, boolean isCritical) {
        DamageProcess process = new DamageProcess(entity, type, damage, isCritical);
        process.process();
    }

    public static void damage(A_Entity attacker, A_LivingEntity damager, DamageType type, double damage, boolean isCritical) {
        DamageAtkProcess process = new DamageAtkProcess(attacker, damager, type, damage, isCritical);
        process.process();
    }

    /**
     * 크리티컬을 확률을 계산하는 데미지 처리
     * @param attacker 공격하는 엔티티
     * @param damager 데미지를 입는 엔티티
     * @param type 데미지 유형
     * @param damage 베이스 데미지 값
     */
    public static void criCheckDamage(A_Entity attacker, A_LivingEntity damager, DamageType type, double damage) {
        damage(attacker, damager, type, damage, randomCriCheck(attacker));
    }

    public static boolean randomCriCheck(A_Entity entity) {
        AttributeManager manager = new AttributeManager(entity);

        double cri = manager.getAttributeValue(AttributeType.CRITICAL_CHANCE);
        double criDiv = manager.getAttributeValue(AttributeType.CRITICAL_CHANCE_DIVIDE);
        double criMul = manager.getAttributeValue(AttributeType.CRITICAL_CHANCE_MULTIPLY);

        return Math.random() <= cri + (cri * (criDiv - criMul));
    }

    public static DamageResolver getResolverByType(A_Entity entity, DamageType type) {
        return switch (type) {
            case MAGIC -> new MagicResolver(entity);
            case PHYSICAL -> new PhysicalResolver(entity);
            default -> null;
        };
    }
}
