package org.red.minecraft.uw.core.skill.effect;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.red.minecraft.dellarte.library.CommediaDellarte;
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
import org.red.minecraft.uw.core.skill.target.EntityTarget;
import org.red.minecraft.uw.core.skill.target.Target;
import org.red.minecraft.uw.core.skill.target.faction.Faction;
import org.red.minecraft.uw.core.skill.target.faction.FactionType;

import java.util.ArrayList;
import java.util.List;
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

    /**
     * 이 이펙트가 보유한 기본 탐색 대상 수. ({@code thunder} 기어는 YAML 파라미터가 없다)
     * <p>{@link CTXType#TARGET_COUNT} 는 절대 개수가 아니라 <b>여기에 더해지는 가산값</b>이다.
     */
    private static final int BASE_TARGET_COUNT = 1;

    @Override
    public CompletableFuture<EffectResult> execute(SkillCTX ctx) {
        A_Entity caster = ctx.getCTX(CTXType.CASTER);

        A_Entity[] targets = resolveTargets(ctx, caster);
        if (targets.length == 0) {
            UndefinedWorldCorePlugin.sendLog("ThunderEffect: 대상 없음 (LAST_TARGET_INFO 없음 + 주변 탐색 결과 0)");
            return CompletableFuture.completedFuture(EffectResult.FAIL);
        }

        double baseDamage = UndefinedWorldCore.getAttributeManager(caster)
                .getAttributeValue(AttributeType.MAGIC_DAMAGE);
        double damageMultiplier = ctx.getCTX(CTXType.DAMAGE);
        double finalDamage = baseDamage * damageMultiplier;

        List<A_Entity> struck = new ArrayList<>();
        for (A_Entity target : targets) {
            if (target == null) continue;

            A_LivingEntity livingTarget = target.getALivingEntity();
            if (livingTarget == null || livingTarget.isDead()) continue;

            // 한 대상의 실패가 나머지 대상 처리를 중단시키지 않도록 대상 단위로 격리한다
            try {
                strikeVisual(livingTarget.getLocation());
                // 치명타는 DamageAtkProcess 가 판정한다. 여기서 미리 굴려 넘기면
                // 파이프라인이 "이미 치명타" 가 아닐 때 한 번 더 굴려서 실효 확률이 p + (1-p)p 로 부풀어 오른다.
                //
                // CHAIN_LIGHTING / THUNDER 는 CTX(DAMAGE_TYPE, ELEMENTAL)를 따르지 않고 고정한다.
                // 이 둘은 "기본값"이 아니라 낙뢰 이펙트의 정체성이며, CHAIN_LIGHTING 은 흡혈 불가·고정데미지 같은
                // 고유 플래그를 가진 메커니즘 자체다. 물리 낙뢰로 바뀌면 이펙트가 성립하지 않는다.
                // (반면 SwordAuraEffect 의 PHYSICAL 은 특수 플래그가 없는 일반 기본값이라 CTX 를 따른다)
                CombatManager.damage(caster, livingTarget, DamageType.CHAIN_LIGHTING, ElementalType.THUNDER, finalDamage);
                struck.add(target);
            } catch (RuntimeException exception) {
                UndefinedWorldCorePlugin.sendLog("ThunderEffect 대상 처리 실패 target:" + target.getUniqueIdStr() + " - " + exception);
            }
        }

        // 대상 배열은 있었지만 전부 무효라 아무것도 타격하지 못한 경우도 실패로 본다 (다른 이펙트와 동일 기준)
        if (struck.isEmpty()) {
            UndefinedWorldCorePlugin.sendLog("ThunderEffect: 유효한 대상이 없어 실패 (targets=" + targets.length + ")");
            return CompletableFuture.completedFuture(EffectResult.FAIL);
        }

        // 이번 이펙트가 "실제로 타격한" 엔티티만 LAST_TARGET_INFO 에 저장한다.
        // (탐색 결과를 그대로 넘기면 죽었거나 비리빙이라 건너뛴 대상까지 다음 노드가 적중 대상으로 받는다)
        ctx.setCTX(CTXType.LAST_TARGET_INFO, struck.toArray(new A_Entity[0]));
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

        // 기본값이 고정 상수인 항목은 2인자 getCTX 로 통일한다 (ProjectileEffect 와 동일한 관용구).
        // SEARCH_CENTER 만 기본값이 caster 기준 계산이라 hasCTX 분기를 유지한다.
        double range = ctx.getCTX(CTXType.SEARCH_RANGE, 8.0);
        // TARGET_COUNT 는 절대 개수가 아니라 자기 기본값에 더해지는 가산값이다 (TargetEffect 와 동일한 규약).
        // 기본값이 0(덧셈 항등원)이라 수정자 기어가 없으면 BASE_TARGET_COUNT 가 그대로 쓰인다.
        // ⛔ getCTX(TARGET_COUNT, 1) 처럼 절대 개수로 읽으면 기본값 0 이 항상 채워져 낙뢰가 항상 실패한다.
        int maxTargets = BASE_TARGET_COUNT + ctx.getCTX(CTXType.TARGET_COUNT, 0);
        Target.SearchType searchType = ctx.getCTX(CTXType.SEARCH_TYPE, Target.SearchType.RANGE_CIRCLE);
        FactionType factionType = ctx.getCTX(CTXType.TARGET_FACTION, FactionType.ENEMY);

        EntityTarget entityTarget = new EntityTarget(center, maxTargets, range, searchType);
        Entity[] found = entityTarget.getTargets(Faction.predicate(caster, factionType));

        A_Entity[] result = new A_Entity[found.length];
        for (int i = 0; i < found.length; i++) {
            result[i] = CommediaDellarte.getAEntity(found[i]);
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
