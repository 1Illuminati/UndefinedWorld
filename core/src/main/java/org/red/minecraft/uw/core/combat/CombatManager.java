package org.red.minecraft.uw.core.combat;

import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.dellarte.library.entity.A_LivingEntity;
import org.red.minecraft.uw.core.UndefinedWorldCore;
import org.red.minecraft.uw.core.attribute.AttributeHolder;
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

    /**
     * 주의! 해당 함수에서의 데미지는 데미지 고정값이 아닌 퍼센트 소숫점으로 입력해야한다
     * 0.5를 입력하면 공격엔티티의 해당 공격타입 스텟의 50%의 데미지를 주는 방식이다
     * @param attacker 공격자
     * @param damager 방어자
     * @param type 데미지유형
     * @param damage 데미지 (퍼센트) ex). 0.5, 0.7...
     * @param isCritical 크리티컬 여부
     */
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
        AttributeHolder holder = UndefinedWorldCore.getAttributeHolder(entity);

        double cri = holder.getAttributeValue(AttributeType.CRITICAL_CHANCE);
        double criDiv = holder.getAttributeValue(AttributeType.CRITICAL_CHANCE_DIVIDE);
        double criMul = holder.getAttributeValue(AttributeType.CRITICAL_CHANCE_MULTIPLY);

        return Math.random() <= cri + (cri * (criDiv - criMul));
    }

    public static void applyHitEffect(LivingEntity defEntity, Vector damageVec) {
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

    public static DamageResolver getResolverByType(A_Entity entity, DamageType type) {
        return switch (type) {
            case MAGIC -> new MagicResolver(entity);
            case PHYSICAL -> new PhysicalResolver(entity);
            default -> null;
        };
    }
}
