package org.red.minecraft.uw.core.combat.damage.process;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.red.minecraft.dellarte.library.CommediaDellarte;
import org.red.minecraft.dellarte.library.entity.A_LivingEntity;
import org.red.minecraft.uw.core.UndefinedWorldCore;
import org.red.minecraft.uw.core.combat.CombatManager;
import org.red.minecraft.uw.core.combat.ElementalType;
import org.red.minecraft.uw.core.combat.buff.BuffContext;
import org.red.minecraft.uw.core.combat.buff.BuffManager;
import org.red.minecraft.uw.core.combat.buff.BuffType;
import org.red.minecraft.uw.core.combat.damage.DamageCTX;
import org.red.minecraft.uw.core.combat.damage.DamageType;
import org.red.minecraft.uw.core.skill.target.EntityTarget;
import org.red.minecraft.uw.core.skill.target.Target;
import org.red.minecraft.uw.core.skill.target.faction.Faction;
import org.red.minecraft.uw.core.skill.target.faction.FactionType;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * 데미지 처리(체력 차감) 이후 속성별 부수효과를 담당하는 후처리 단계.
 * DamageProcess.run() 에서 이벤트가 취소되지 않았을 때만 호출된다. (구조 결정 2.5-1 참조)
 *
 * 속성별 명세는 ElementalType javadoc 기준.
 * 현재 구현: THUNDER(감전). FIRE/WATER/LAND 는 T8에서 확장.
 */
public final class ElementalPostProcessor {

    // ── 감전(THUNDER) 상수 ─────────────────────────────
    /** 감전 지속시간 (틱) — todo 밸런스 확정 필요 (임시 5초) */
    private static final long SHOCK_DURATION_TICKS = 100L;
    /** 연쇄 탐지 범위 (명세: 1칸) */
    private static final double CHAIN_RANGE = 1.0;
    /** 연쇄 데미지 비율 (명세: 기존 공격력 50%) */
    private static final double CHAIN_DAMAGE_RATE = 0.5;
    /** 연쇄 면역 시간 (명세: 2초) */
    private static final long CHAIN_IMMUNE_MS = 2000L;

    /** 연쇄효과 면역 관리: 엔티티 UUID -> 면역 만료 시각(ms).
     *  내부 재귀 방지용 임시 상태라 Buff 시스템 대신 단순 맵으로 관리한다. */
    private static final Map<UUID, Long> chainImmuneUntil = new ConcurrentHashMap<>();

    // ── 화상(FIRE) 상수 ────────────────────────────────
    /** 화상 지속시간 (틱) — todo 밸런스 확정 필요 (임시 5초) */
    private static final long BURN_DURATION_TICKS = 100L;

    // ── 침묵(WATER) 상수 ───────────────────────────────
    /** 침묵 부여 확률 (명세: 5%) — todo 물 공격력/방어력 확률 보정 공식 확정 필요 */
    private static final double SILENCE_CHANCE = 0.05;
    /** 침묵 지속시간 (명세: 3초) */
    private static final long SILENCE_DURATION_TICKS = 60L;

    // ── 파쇄(LAND) 상수 ────────────────────────────────
    /** 파쇄 지속시간 (틱) — todo 밸런스 확정 필요 (임시 5초, 재적용 시 갱신) */
    private static final long SHATTER_DURATION_TICKS = 100L;
    // 파쇄 최대 중첩(maxStack=10)은 BuffManager/ShatterDebuff 가 강제한다 — 여기서 상한을 두지 않는다

    private ElementalPostProcessor() {}

    /** 데미지 확정 이후 속성 부수효과 진입점 */
    public static void process(DamageCTX ctx, EntityDamageEvent event) {
        // 화상 도트(BURNING)는 독자 데미지 유형 — 속성 디버프를 재부여하지 않는다 (무한 갱신 방지)
        if (ctx.type() == DamageType.BURNING) return;

        switch (ctx.elementalType()) {
            case THUNDER -> processThunder(ctx, event);
            case FIRE -> processFire(ctx);
            case WATER -> processWater(ctx);
            case LAND -> processLand(ctx);
            // todo WIND(이속강탈)는 밸런스 확정 대기(⛔), ICE는 명세 미작성
            default -> {}
        }
    }

    // ──────────────────────────────────────────────────
    // THUNDER: 감전 부여 + 감전 상태 피격 시 연쇄
    // ──────────────────────────────────────────────────

    private static void processThunder(DamageCTX ctx, EntityDamageEvent event) {
        A_LivingEntity defender = ctx.defender();
        BuffManager buffs = UndefinedWorldCore.getBuffManager();

        // 피격 시점에 이미 감전 상태였다면 연쇄 발동 (신규 감전 부여보다 먼저 판정)
        boolean wasShocked = buffs.hasBuff(defender, BuffType.SHOCK);
        if (wasShocked && ctx.hasAttacker()) {
            chainDamage(ctx, event);
        }

        // 감전 부여/갱신
        BuffContext buffCtx = ctx.hasAttacker()
                ? BuffContext.builder(1).caster(ctx.attacker()).build()
                : BuffContext.of(1);
        buffs.applyBuff(defender, BuffType.SHOCK, buffCtx, SHOCK_DURATION_TICKS, false);
    }

    /**
     * 연쇄효과: 방어자 주변 CHAIN_RANGE 내의 적에게 확정 데미지의 50%를 추가로 가한다.
     * 연쇄 데미지를 받은 적은 CHAIN_IMMUNE_MS 동안 연쇄 면역. (재귀 폭발 방지)
     */
    private static void chainDamage(DamageCTX ctx, EntityDamageEvent event) {
        double chainDamage = event.getDamage() * CHAIN_DAMAGE_RATE;

        // 데미지가 0(리스너가 무효화)이면 연쇄로 얻는 게 없는데 대상의 2초 연쇄 면역만 소모된다.
        // NaN 도 여기서 걸린다 (NaN > 0 은 false)
        if (!(chainDamage > 0)) return;

        A_LivingEntity defender = ctx.defender();
        Location center = defender.getLocation();

        Predicate<Entity> filter = Faction.predicate(ctx.attacker(), FactionType.ENEMY)
                .and(e -> !e.getUniqueId().equals(defender.getUniqueId()))
                .and(e -> !isChainImmune(e.getUniqueId()));

        Entity[] targets = new EntityTarget(center, Integer.MAX_VALUE, CHAIN_RANGE, Target.SearchType.RANGE_CIRCLE)
                .getTargets(filter);

        for (Entity target : targets) {
            // getTargets 의 면역 필터는 스트림 종료 시점에 한 번에 평가된 스냅샷이다.
            // 루프 도중 재귀 연쇄가 뒤쪽 대상을 이미 때려 면역으로 만들 수 있으므로 때리기 직전에 다시 확인한다.
            // (이 재확인이 없으면 대상이 2초 안에 연쇄 데미지를 두 번 받아 면역 규칙이 우회된다)
            if (isChainImmune(target.getUniqueId())) continue;

            A_LivingEntity living = CommediaDellarte.getAEntity(target).getALivingEntity();
            if (living == null) continue;

            // Faction.predicate(ENEMY)는 리빙 여부만 보고 생사는 보지 않는다.
            // 같은 틱에 죽은 시체가 아직 월드에 남아 있으면 연쇄가 시체를 때려 setHealth(0)/피격연출이 다시 돈다.
            // (VamfirePostProcessor 는 동일한 isDead 가드를 이미 갖고 있다)
            if (living.isDead()) continue;

            // 면역을 먼저 걸어 연쇄 → 연쇄 재귀를 차단한다
            setChainImmune(target.getUniqueId());
            CombatManager.damage(ctx.attacker(), living, DamageType.CHAIN_LIGHTING, ElementalType.THUNDER, chainDamage);
        }
    }

    // ──────────────────────────────────────────────────
    // FIRE: 화상 부여 (도트 데미지는 BurnDebuff가 담당)
    // ──────────────────────────────────────────────────

    private static void processFire(DamageCTX ctx) {
        UndefinedWorldCore.getBuffManager()
                .applyBuff(ctx.defender(), BuffType.BURN, casterContext(ctx, 1), BURN_DURATION_TICKS, false);
    }

    // ──────────────────────────────────────────────────
    // WATER: 확률 침묵 부여 (스킬 차단은 SkillEngine이 담당)
    // ──────────────────────────────────────────────────

    private static void processWater(DamageCTX ctx) {
        if (Math.random() > SILENCE_CHANCE) return;

        UndefinedWorldCore.getBuffManager()
                .applyBuff(ctx.defender(), BuffType.SILENCE, casterContext(ctx, 1), SILENCE_DURATION_TICKS, false);
    }

    // ──────────────────────────────────────────────────
    // LAND: 파쇄 중첩 부여 (피해 증가는 ShatterDefModifier가 담당)
    // ──────────────────────────────────────────────────

    private static void processLand(DamageCTX ctx) {
        // 중첩 누적은 BuffManager 담당이다 (새 레벨 = min(maxStack, 기존레벨 + 요청레벨), 파쇄 maxStack=10 확정).
        // 호출부는 "이번에 추가할 양"만 넘긴다. 여기서 기존 레벨을 읽어 +1 하면 매니저 누적과 겹쳐 중첩이 두 배로 붙는다.
        UndefinedWorldCore.getBuffManager()
                .applyBuff(ctx.defender(), BuffType.SHATTER, casterContext(ctx, 1), SHATTER_DURATION_TICKS, false);
    }

    /** 공격자가 있으면 caster를 담은 BuffContext 생성 */
    private static BuffContext casterContext(DamageCTX ctx, int level) {
        return ctx.hasAttacker()
                ? BuffContext.builder(level).caster(ctx.attacker()).build()
                : BuffContext.of(level);
    }

    // ── 연쇄 면역 관리 ─────────────────────────────────

    private static boolean isChainImmune(UUID id) {
        Long until = chainImmuneUntil.get(id);
        if (until == null) return false;
        if (until <= System.currentTimeMillis()) {
            chainImmuneUntil.remove(id);
            return false;
        }
        return true;
    }

    private static void setChainImmune(UUID id) {
        chainImmuneUntil.put(id, System.currentTimeMillis() + CHAIN_IMMUNE_MS);
        cleanupExpired();
    }

    /** 만료된 면역 엔트리 정리 (호출 시점 lazy 정리, 별도 태스크 없음) */
    private static void cleanupExpired() {
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<UUID, Long>> it = chainImmuneUntil.entrySet().iterator();
        while (it.hasNext()) {
            if (it.next().getValue() <= now) it.remove();
        }
    }
}
