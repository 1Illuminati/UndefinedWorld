package org.red.minecraft.uw.core.combat;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.dellarte.library.entity.A_LivingEntity;
import org.red.minecraft.uw.core.UndefinedWorldCore;
import org.red.minecraft.uw.core.attribute.AttributeManager;
import org.red.minecraft.uw.core.attribute.AttributeType;
import org.red.minecraft.uw.core.combat.damage.DamageSource;
import org.red.minecraft.uw.core.combat.damage.process.DamageAtkProcess;
import org.red.minecraft.uw.core.combat.damage.process.DamageProcess;
import org.red.minecraft.uw.core.combat.damage.DamageType;

public final class CombatManager {
    public static void damage(DamageProcess process) {
        process.run();
    }

    public static void damage(A_LivingEntity defender, DamageType type, ElementalType elementType, double damage, double scale, boolean isCritical) {
        damage(new DamageProcess(defender, type, elementType, damage, scale, isCritical));
    }

    public static void damage(A_LivingEntity defender, DamageType type, ElementalType elementType, double damage, double scale) {
        damage(defender, type, elementType, damage, scale, false);
    }

    public static void damage(A_LivingEntity defender, DamageType type, ElementalType elementType, double damage) {
        damage(defender, type, elementType, damage, 1.0, false);
    }

    public static void damage(A_LivingEntity defender, DamageType type, double damage) {
        damage(defender, type, ElementalType.NONE, damage, 1.0, false);
    }

    public static void damage(A_LivingEntity defender, DamageType type, double damage, double scale) {
        damage(defender, type, ElementalType.NONE, damage, scale, false);
    }


    public static void damage(A_Entity attacker, A_LivingEntity defender, DamageType type, ElementalType elementType, double damage, double scale, boolean isCritical) {
        damage(new DamageAtkProcess(attacker, defender, type, elementType, damage, scale, isCritical));
    }

    public static void damage(A_Entity attacker, A_LivingEntity defender, DamageType type, ElementalType elementType, double damage, boolean isCritical) {
        damage(attacker, defender, type, elementType, damage, 1.0, isCritical);
    }

    public static void damage(A_Entity attacker, A_LivingEntity defender, DamageType type, ElementalType elementType, double damage, double scale) {
        damage(attacker, defender, type, elementType, damage, scale, false);
    }

    public static void damage(A_Entity attacker, A_LivingEntity defender, DamageType type, ElementalType elementType, double damage) {
        damage(attacker, defender, type, elementType, damage, 1.0, false);
    }

    public static void damage(A_Entity attacker, A_LivingEntity defender, DamageType type, double damage) {
        damage(attacker, defender, type, ElementalType.NONE, damage, 1.0, false);
    }

    public static void damage(A_Entity attacker, A_LivingEntity defender, DamageType type, double damage, boolean isCritical) {
        damage(attacker, defender, type, ElementalType.NONE, damage, 1.0, isCritical);
    }

    public static void damage(A_Entity attacker, A_LivingEntity defender, DamageType type, double damage, double scale) {
        damage(attacker, defender, type, ElementalType.NONE, damage, scale, false);
    }


    public static void locDamage(Location attackLoc, A_Entity attacker, A_LivingEntity defender, DamageType type, ElementalType elementType, double damage, double scale, boolean isCritical) {
        DamageSource source = new DamageSource(attacker, defender, attackLoc);
        damage(new DamageAtkProcess(source, type, elementType, damage, scale, isCritical));
    }

    public static void locDamage(Location attackLoc, A_Entity attacker, A_LivingEntity defender, DamageType type, ElementalType elementType, double damage, boolean isCritical) {
        locDamage(attackLoc, attacker, defender, type, elementType, damage, 1.0, isCritical);
    }

    public static void locDamage(Location attackLoc, A_Entity attacker, A_LivingEntity defender, DamageType type, ElementalType elementType, double damage, double scale) {
        locDamage(attackLoc, attacker, defender, type, elementType, damage, scale, false);
    }

    public static void locDamage(Location attackLoc, A_Entity attacker, A_LivingEntity defender, DamageType type, ElementalType elementType, double damage) {
        locDamage(attackLoc, attacker, defender, type, elementType, damage, 1.0, false);
    }

    public static void locDamage(Location attackLoc, A_Entity attacker, A_LivingEntity defender, DamageType type, double damage) {
        locDamage(attackLoc, attacker, defender, type, ElementalType.NONE, damage, 1.0, false);
    }

    public static void locDamage(Location attackLoc, A_Entity attacker, A_LivingEntity defender, DamageType type, double damage, boolean isCritical) {
        locDamage(attackLoc, attacker, defender, type, ElementalType.NONE, damage, 1.0, isCritical);
    }

    public static void locDamage(Location attackLoc, A_Entity attacker, A_LivingEntity defender, DamageType type, double damage, double scale) {
        locDamage(attackLoc, attacker, defender, type, ElementalType.NONE, damage, scale, false);
    }


    public static void locDamage(Location attackLoc, A_LivingEntity defender, DamageType type, ElementalType elementType, double damage, double scale, boolean isCritical) {
        locDamage(attackLoc, null, defender, type, elementType, damage, scale, isCritical);
    }

    public static void locDamage(Location attackLoc, A_LivingEntity defender, DamageType type, ElementalType elementType, double damage, boolean isCritical) {
        locDamage(attackLoc, defender, type, elementType, damage, 1.0, isCritical);
    }

    public static void locDamage(Location attackLoc, A_LivingEntity defender, DamageType type, ElementalType elementType, double damage, double scale) {
        locDamage(attackLoc, defender, type, elementType, damage, scale, false);
    }

    public static void locDamage(Location attackLoc, A_LivingEntity defender, DamageType type, ElementalType elementType, double damage) {
        locDamage(attackLoc, defender, type, elementType, damage, 1.0, false);
    }

    public static void locDamage(Location attackLoc, A_LivingEntity defender, DamageType type, double damage) {
        locDamage(attackLoc, defender, type, ElementalType.NONE, damage, 1.0, false);
    }

    public static void locDamage(Location attackLoc, A_LivingEntity defender, DamageType type, double damage, boolean isCritical) {
        locDamage(attackLoc, defender, type, ElementalType.NONE, damage, 1.0, isCritical);
    }

    public static void locDamage(Location attackLoc, A_LivingEntity defender, DamageType type, double damage, double scale) {
        locDamage(attackLoc, defender, type, ElementalType.NONE, damage, scale, false);
    }


    public static boolean randomCriCheck(A_Entity entity) {
        AttributeManager manager = UndefinedWorldCore.getAttributeManager(entity);

        double cri = manager.getAttributeValue(AttributeType.CRITICAL_CHANCE);
        double criDiv = manager.getAttributeValue(AttributeType.CRITICAL_CHANCE_DIVIDE);
        double criMul = manager.getAttributeValue(AttributeType.CRITICAL_CHANCE_MULTIPLY);

        return Math.random() <= cri + (cri * (criDiv - criMul));
    }

    public static void applyHitEffect(LivingEntity defEntity, Location damageSourceLoc) {
        Vector damageVec = damageSourceLoc != null
                ? defEntity.getLocation().toVector().subtract(damageSourceLoc.toVector())
                : new Vector(0, 0, 0);

        // 붉은색 피격 표현
        float yaw = (float) Math.toDegrees(Math.atan2(damageVec.getZ(), damageVec.getX()));
        defEntity.playHurtAnimation(yaw);

        // 넉백 (damageVec이 zero면 자연 피해 → Y stutter만)
        if (damageVec.lengthSquared() > 0.0001) {
            defEntity.knockback(0.4, damageVec.getX(), damageVec.getZ());
        } else {
            // 어태커 없는 자연 피해: Y축만 살짝 튀기기
            defEntity.setVelocity(defEntity.getVelocity().add(new Vector(0, 0.4, 0)));
        }
    }
}
