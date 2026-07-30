package org.red.minecraft.uw.core.skill.effect;

import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.dellarte.library.entity.A_LivingEntity;
import org.red.minecraft.uw.core.UndefinedWorldCore;
import org.red.minecraft.uw.core.UndefinedWorldCorePlugin;
import org.red.minecraft.uw.core.attribute.AttributeType;
import org.red.minecraft.uw.core.combat.CombatManager;
import org.red.minecraft.uw.core.combat.ElementalType;
import org.red.minecraft.uw.core.combat.damage.DamageType;
import org.red.minecraft.uw.core.skill.CTXType;
import org.red.minecraft.uw.core.skill.SkillCTX;

import java.util.concurrent.CompletableFuture;

/**
 * 단순 데미지 이펙트 — LAST_TARGET_INFO의 대상들에게 데미지를 가한다.
 * TargetEffect/ProjectileEffect 등 타겟 지정 기어의 후속 노드로 사용한다.
 *
 * 데미지 = 시전자 공격력(PHYSICAL→PHYSICS_DAMAGE, MAGIC→MAGIC_DAMAGE) × scale × CTX.DAMAGE
 * 데미지 유형은 CTX.DAMAGE_TYPE 우선, 없으면 기어 YAML damageType(= 이펙트가 보유한 기본값).
 * 공격력 기준 속성도 최종 유형을 따라간다 (MAGIC이면 MAGIC_DAMAGE).
 * 속성은 CTX.ELEMENTAL을 따른다. 대상이 없거나 실제로 한 대상도 타격하지 못하면 FAIL.
 * 치명타는 데미지 파이프라인(DamageAtkProcess)이 판정하므로 여기서 굴리지 않는다.
 */
public class DamageEffect implements Effect {

    private final DamageType damageType;
    private final double scale;

    public DamageEffect(DamageType damageType, double scale) {
        this.damageType = damageType;
        this.scale = scale;
    }

    @Override
    public CompletableFuture<EffectResult> execute(SkillCTX ctx) {
        A_Entity caster = ctx.getCTX(CTXType.CASTER);

        A_Entity[] targets = ctx.hasCTX(CTXType.LAST_TARGET_INFO) ? ctx.getCTX(CTXType.LAST_TARGET_INFO) : null;
        if (targets == null || targets.length == 0) {
            UndefinedWorldCorePlugin.sendLog("DamageEffect: LAST_TARGET_INFO 가 비어 있어 실패 (앞선 타겟 기어 확인 필요)");
            return CompletableFuture.completedFuture(EffectResult.FAIL);
        }

        // CTX 우선, 없으면 기어 YAML 값 (TargetEffect와 동일한 관용구).
        // CTXType.DAMAGE_TYPE에는 기본값이 없으므로 수정자 기어가 세팅하지 않았으면 정확히 기존 동작이 된다.
        DamageType finalDamageType = ctx.getCTX(CTXType.DAMAGE_TYPE, damageType);

        AttributeType baseAttribute = finalDamageType == DamageType.MAGIC ? AttributeType.MAGIC_DAMAGE : AttributeType.PHYSICS_DAMAGE;
        double baseDamage = UndefinedWorldCore.getAttributeManager(caster).getAttributeValue(baseAttribute);
        double damage = baseDamage * scale * (double) ctx.getCTX(CTXType.DAMAGE);

        ElementalType elemental = ctx.getCTX(CTXType.ELEMENTAL);

        int applied = 0;
        for (A_Entity target : targets) {
            if (target == null) continue;

            A_LivingEntity living = target.getALivingEntity();
            if (living == null || living.isDead()) continue;

            // 한 대상의 데미지 처리 실패가 나머지 대상 처리를 통째로 중단시키지 않도록 대상 단위로 격리한다
            try {
                CombatManager.damage(caster, living, finalDamageType, elemental, damage);
                applied++;
            } catch (RuntimeException exception) {
                UndefinedWorldCorePlugin.sendLog("DamageEffect 대상 처리 실패 target:" + target.getUniqueIdStr() + " - " + exception);
            }
        }

        // 대상 배열은 있었지만 전부 무효(죽음/비리빙/실패)라 아무것도 적용하지 못한 경우도 실패로 본다.
        // (SwordAura/Projectile 이 적중 0에 FAIL 을 내는 것과 동일한 기준)
        if (applied == 0) {
            UndefinedWorldCorePlugin.sendLog("DamageEffect: 유효한 대상이 없어 실패 (targets=" + targets.length + ")");
            return CompletableFuture.completedFuture(EffectResult.FAIL);
        }

        return CompletableFuture.completedFuture(EffectResult.SUCCESS);
    }

    /**
     * 기어 자체의 분류라 <b>기어 YAML 값</b>으로 판단한다.
     * <p>Effect 인터페이스에 ctx가 없어 런타임 CTX.DAMAGE_TYPE 은 반영할 수 없다. 즉 수정자 기어로 유형이
     * 바뀌어도 이 분류는 그대로다. 현재 getEffectTypes() 를 읽는 코드가 없어 실害는 없지만,
     * 소비처가 생기면 분류 시점(정의 시 / 실행 시)을 먼저 정해야 한다.
     */
    @Override
    public EffectType[] getEffectTypes() {
        return new EffectType[]{damageType == DamageType.MAGIC ? EffectType.MAGIC : EffectType.MELEE};
    }
}
