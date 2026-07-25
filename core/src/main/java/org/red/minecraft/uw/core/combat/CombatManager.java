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
import org.red.minecraft.uw.core.combat.damage.DamageType;
import org.red.minecraft.uw.core.combat.damage.process.DamageAtkProcess;
import org.red.minecraft.uw.core.combat.damage.process.DamageProcess;

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

        return Math.random() <= cri + (cri * (criMul - criDiv));
    }

    /**
     * 회피 판정 (확정 공식, 구조 결정 2.5):
     * 최종받는데미지 - Dodge * ((DODGE_MULTIPLY - DODGE_DIVIDE) / 100) < random(0, Dodge) 이면 회피 성공.
     * Dodge 자체는 배율이 아닌 원시값, MULTIPLY/DIVIDE는 배율 % 처리.
     *
     * @param defender    방어자
     * @param finalDamage 최종 확정 데미지
     * @return 회피 성공 시 true
     */
    public static boolean randomDodgeCheck(A_LivingEntity defender, double finalDamage) {
        AttributeManager manager = UndefinedWorldCore.getAttributeManager(defender);

        double dodge = manager.getAttributeValue(AttributeType.DODGE);
        if (dodge <= 0) return false;

        double dodgeMul = manager.getAttributeValue(AttributeType.DODGE_MULTIPLY);
        double dodgeDiv = manager.getAttributeValue(AttributeType.DODGE_DIVIDE);

        double threshold = finalDamage - dodge * ((dodgeMul - dodgeDiv) / 100);
        return threshold < Math.random() * dodge;
    }

    /**
     * 막기 판정 (확정 공식, 구조 결정 2.5):
     * 확률(%) = Block * (1 + (BLOCK_MULTIPLY - BLOCK_DIVIDE) / 100)
     * Block 자체가 %값, MULTIPLY/DIVIDE는 배율 % 보정. 성공 시 데미지 완전 무효.
     *
     * @param defender 방어자
     * @return 막기 성공 시 true
     */
    public static boolean randomBlockCheck(A_LivingEntity defender) {
        AttributeManager manager = UndefinedWorldCore.getAttributeManager(defender);

        double block = manager.getAttributeValue(AttributeType.BLOCK);
        if (block <= 0) return false;

        double blockMul = manager.getAttributeValue(AttributeType.BLOCK_MULTIPLY);
        double blockDiv = manager.getAttributeValue(AttributeType.BLOCK_DIVIDE);

        double chance = block * (1 + ((blockMul - blockDiv) / 100));
        return Math.random() * 100 < chance;
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
