package org.red.minecraft.uw.core.skill.effect;

import org.bukkit.Location;
import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.dellarte.library.entity.A_LivingEntity;
import org.red.minecraft.uw.core.UndefinedWorldCore;
import org.red.minecraft.uw.core.attribute.AttributeType;
import org.red.minecraft.uw.core.combat.CombatManager;
import org.red.minecraft.uw.core.combat.ElementalType;
import org.red.minecraft.uw.core.combat.damage.DamageType;
import org.red.minecraft.uw.core.skill.CTXType;
import org.red.minecraft.uw.core.skill.SkillCTX;
import org.red.minecraft.uw.core.skill.target.EntityTarget;
import org.red.minecraft.uw.core.skill.target.Target;
import org.red.minecraft.uw.core.skill.target.faction.Faction;
import org.red.minecraft.uw.core.skill.target.faction.FactionType;

import java.util.concurrent.CompletableFuture;

/**
 * 번개 공격 이펙트.
 * 대상 결정 우선순위:
 *   1순위: CTX 의 LAST_TARGET_INFO 가 있으면 해당 엔티티 배열 사용
 *   2순위: CTX 의 탐색 파라미터(SEARCH_RANGE, TARGET_COUNT 등)로 주변 탐색
 * 적중한 각 대상에게:
 *   - 번개 시각 효과 (strikeLightningEffect)
 *   - CHAIN_LIGHTING 타입 + THUNDER 속성 데미지
 * 데미지 = 시전자의 MAGIC_DAMAGE 속성 × CTX.DAMAGE 배율
 */
public class ThunderEffect implements Effect {

    @Override
    public CompletableFuture<EffectResult> execute(SkillCTX ctx) {
        A_Entity caster = ctx.getCTX(CTXType.CASTER);

        A_Entity[] targets = resolveTargets(ctx, caster);
        if (targets.length == 0) {
            return CompletableFuture.completedFuture(EffectResult.FAIL);
        }

        double baseDamage = UndefinedWorldCore.getAttributeManager(caster)
                .getAttributeValue(AttributeType.MAGIC_DAMAGE);
        double damageMultiplier = ctx.getCTX(CTXType.DAMAGE);
        double finalDamage = baseDamage * damageMultiplier;

        for (A_Entity target : targets) {
            A_LivingEntity livingTarget = target.getALivingEntity();
            if (livingTarget == null) continue;

            strikeVisual(livingTarget.getLocation());
            boolean isCritical = CombatManager.randomCriCheck(caster);
            CombatManager.damage(caster, livingTarget, DamageType.CHAIN_LIGHTING, ElementalType.THUNDER, finalDamage, 1.0, isCritical);
        }

        // 이번 이펙트가 적중한 엔티티를 LAST_TARGET_INFO 에 저장해 다음 노드에서 활용 가능하도록 한다
        ctx.setCTX(CTXType.LAST_TARGET_INFO, targets);
        return CompletableFuture.completedFuture(EffectResult.SUCCESS);
    }

    // ──────────────────────────────────────────────────
    // 대상 결정
    // ──────────────────────────────────────────────────

    private A_Entity[] resolveTargets(SkillCTX ctx, A_Entity caster) {
        // 1순위: 이전 노드가 채워 둔 LAST_TARGET_INFO
        if (ctx.hasCTX(CTXType.LAST_TARGET_INFO)) {
            A_Entity[] lastTargets = ctx.getCTX(CTXType.LAST_TARGET_INFO);
            if (lastTargets != null && lastTargets.length > 0) {
                return lastTargets;
            }
        }

        // 2순위: CTX 탐색 파라미터로 주변 엔티티 검색
        return searchNearby(ctx, caster);
    }

    private A_Entity[] searchNearby(SkillCTX ctx, A_Entity caster) {
        Location center = ctx.hasCTX(CTXType.SEARCH_CENTER)
                ? ctx.getCTX(CTXType.SEARCH_CENTER)
                : caster.getLocation();

        double range = ctx.getCTX(CTXType.SEARCH_RANGE, 8.0);
        int maxTargets = ctx.getCTX(CTXType.TARGET_COUNT);
        Target.SearchType searchType = ctx.hasCTX(CTXType.SEARCH_TYPE)
                ? ctx.getCTX(CTXType.SEARCH_TYPE)
                : Target.SearchType.RANGE_CIRCLE;
        FactionType factionType = ctx.hasCTX(CTXType.TARGET_FACTION)
                ? ctx.getCTX(CTXType.TARGET_FACTION)
                : FactionType.ENEMY;

        EntityTarget entityTarget = new EntityTarget(center, maxTargets, range, searchType);
        org.bukkit.entity.Entity[] found = entityTarget.getTargets(Faction.predicate(caster, factionType));

        A_Entity[] result = new A_Entity[found.length];
        for (int i = 0; i < found.length; i++) {
            result[i] = org.red.minecraft.dellarte.library.CommediaDellarte.getAEntity(found[i]);
        }
        return result;
    }

    private void strikeVisual(Location location) {
        if (location.getWorld() == null) return;
        location.getWorld().strikeLightningEffect(location);
    }

    @Override
    public EffectType[] getEffectTypes() {
        return new EffectType[]{EffectType.MAGIC, EffectType.TARGET};
    }
}
