package org.red.minecraft.uw.core.skill.effect;

import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.dellarte.library.entity.A_LivingEntity;
import org.red.minecraft.uw.core.UndefinedWorldCore;
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
 * 속성은 CTX.ELEMENTAL을 따른다. 대상이 없으면 FAIL.
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
        if (targets == null || targets.length == 0) return CompletableFuture.completedFuture(EffectResult.FAIL);

        AttributeType baseAttribute = damageType == DamageType.MAGIC ? AttributeType.MAGIC_DAMAGE : AttributeType.PHYSICS_DAMAGE;
        double baseDamage = UndefinedWorldCore.getAttributeManager(caster).getAttributeValue(baseAttribute);
        double damage = baseDamage * scale * (double) ctx.getCTX(CTXType.DAMAGE);

        ElementalType elemental = ctx.getCTX(CTXType.ELEMENTAL);

        for (A_Entity target : targets) {
            A_LivingEntity living = target.getALivingEntity();
            if (living == null || living.isDead()) continue;

            CombatManager.damage(caster, living, damageType, elemental, damage);
        }

        return CompletableFuture.completedFuture(EffectResult.SUCCESS);
    }

    @Override
    public EffectType[] getEffectTypes() {
        return new EffectType[]{damageType == DamageType.MAGIC ? EffectType.MAGIC : EffectType.MELEE};
    }
}
