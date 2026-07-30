package org.red.minecraft.uw.core.skill;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;
import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.dellarte.library.entity.A_Player;
import org.red.minecraft.dellarte.library.util.map.CoolTimeMap;
import org.red.minecraft.uw.core.UndefinedWorldCore;
import org.red.minecraft.uw.core.UndefinedWorldCorePlugin;
import org.red.minecraft.uw.core.attribute.stat.Stat;
import org.red.minecraft.uw.core.combat.ElementalType;
import org.red.minecraft.uw.core.combat.buff.BuffType;
import org.red.minecraft.uw.core.combat.damage.DamageType;
import org.red.minecraft.uw.core.exeception.CannotPayCostException;
import org.red.minecraft.uw.core.skill.condition.*;
import org.red.minecraft.uw.core.skill.cost.*;
import org.red.minecraft.uw.core.skill.effect.*;
import org.red.minecraft.uw.core.skill.effect.conversion.MergeEffect;
import org.red.minecraft.uw.core.skill.effect.modifier.*;
import org.red.minecraft.uw.core.skill.projectile.ProjectileType;
import org.red.minecraft.uw.core.skill.projectile.ProjectilesShape;
import org.red.minecraft.uw.core.skill.target.Target;
import org.red.minecraft.uw.core.skill.effect.modifier.elemental.ElementalEffect;
import org.red.minecraft.uw.core.skill.factory.FunctionFactory;
import org.red.minecraft.uw.core.skill.factory.SimpleFactory;
import org.red.minecraft.uw.core.skill.factory.SkillFactory;
import org.red.minecraft.uw.core.skill.target.faction.FactionType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class SkillEngine {
    private static final Map<String, SkillFactory<? extends Effect>> effectMap = new HashMap<>();
    private static final Map<String, SkillFactory<? extends Condition>> conditionMap = new HashMap<>();
    private static final Map<CostType, SkillFactory<? extends Cost<?>>> costMap = new HashMap<>();

    @Nullable
    public static SkillFactory<? extends Effect> getEffectFactory(String effectName) {
        return effectMap.getOrDefault(effectName, null);
    }

    public static void setEffectFactory(SkillFactory<? extends Effect> effectFactory) {
        effectMap.put(effectFactory.getID(), effectFactory);
    }

    public static boolean hasEffectFactory(String effectName) {
        return effectMap.containsKey(effectName);
    }

    @Nullable
    public static SkillFactory<? extends Condition> getConditionFactory(String conditionName) {
        return conditionMap.getOrDefault(conditionName, null);
    }

    public static void setConditionFactory(SkillFactory<? extends Condition> conditionFactory) {
        conditionMap.put(conditionFactory.getID(), conditionFactory);
    }

    public static boolean hasConditionFactory(String conditionName) {
        return conditionMap.containsKey(conditionName);
    }

    @Nullable
    public static SkillFactory<? extends Cost<?>> getCostFactory(CostType type) {
        return costMap.get(type);
    }

    public static void runSkill(A_Entity caster, SkillDefinition skill) {
        boolean isPlayer = caster instanceof A_Player;

        SkillDebugManager.onSkillStart(caster, skill);

        // 스킬 사용 자체가 막히는 상태(침묵/탈것 탑승) 체크
        String blocked = findBlockedReason(caster);
        if (blocked != null) {
            reportBlocked(caster, isPlayer, blocked);
            return;
        }

        // 쿨타임 사전 체크 (적용은 캐스팅 완료 시점 — 확정 규칙)
        if (isOnCoolDown(caster, skill)) {
            double remainCoolTime = getRemainCoolTime(caster, skill);
            if (isPlayer) caster.sendMessage(String.format(
                    "아직 스킬이 준비되지 않았습니다. (남은 시간 %.1f초)",
                    remainCoolTime)); //todo 문구/형식(MiniMessage 등) 사용자 확정 필요
            SkillDebugManager.onCoolDownBlocked(caster, remainCoolTime);
            return;
        }

        // 비용 사전 체크 (지불은 캐스팅 완료 시점 — 확정 규칙)
        CostType lacking = findLackingCost(caster, skill.getCostData());
        if (lacking != null) {
            if (isPlayer) caster.sendMessage(String.format(
                    "%s 자원이 부족합니다. (필요 %.1f)",
                    lacking.name(), sumRequired(skill.getCostData(), lacking))); //todo 문구/형식 사용자 확정 필요
            SkillDebugManager.onCostLacking(caster, lacking, skill.getCostData(), "사전 체크");
            return;
        }

        // 캐스팅 규칙 2: 캐스팅 중 다른 스킬 사용 → 기존 캐스팅 취소 후 새 스킬 진행.
        // 사전 체크를 모두 통과해 "실제로 사용이 성립한 뒤"에 취소해야 한다.
        // (먼저 취소하면 쿨타임/자원 부족으로 발동조차 안 되는 스킬 입력에 기존 캐스팅이 날아간다)
        // 이 경로만 CastingManager.cancel(사유) 을 거치지 않으므로 사유 출력을 여기서 한다.
        SkillDebugManager.onCastCancelled(caster, "other_skill");
        CastingManager.cancelCast(caster);

        // castingTime은 틱 단위다 (§2.6 확정). 플레이어 안내에만 초로 환산해 표기한다.
        int castTicks = skill.getCastingTime();
        if (castTicks <= 0) {
            SkillDebugManager.onCastSkipped(caster, castTicks);
            completeCast(caster, skill);
            return;
        }

        if (isPlayer) caster.sendMessage(String.format(
                "캐스팅 중... (%.1f초, 이동/피격 시 취소)",
                ticksToSeconds(castTicks))); //todo 문구/형식(진행바 등) 사용자 확정 필요

        CastingManager.startCast(caster, castTicks, () -> completeCast(caster, skill));
    }

    /**
     * 스킬 사용 자체가 차단되는 상태인지 판정. (§2.6 스킬 코어 2·3)
     * <p>진입 시점과 캐스팅 완료 시점 양쪽에서 같은 규칙을 적용한다 — 캐스팅 도중 상태가 바뀌었는데
     * 완료 경로만 통과하면 "침묵/탑승 중 발동 금지" 규칙이 우회된다.
     * @return 차단 사유 안내 문구, 차단 상태가 아니면 null
     */
    @Nullable
    private static String findBlockedReason(A_Entity caster) {
        // 침묵(수속성 디버프) 상태면 스킬 사용 불가
        if (UndefinedWorldCore.getBuffManager().hasBuff(caster, BuffType.SILENCE))
            return "침묵 상태에서는 스킬을 사용할 수 없습니다."; //todo 문구/형식 사용자 확정 필요

        // 탈것 탑승 중에는 스킬 사용 불가 (탑승 중엔 이동 차단이 우회되므로 사용 자체를 막는다).
        // 대상은 플레이어 한정 — 근거는 CastingManager.isVehicleRuleTarget 주석 참조.
        if (CastingManager.isVehicleRuleTarget(caster) && caster.isInsideVehicle())
            return "탈것에 탑승한 상태에서는 스킬을 사용할 수 없습니다."; //todo 문구/형식 사용자 확정 필요

        return null;
    }

    /**
     * 차단 사유 전달. 플레이어는 채팅으로, 비플레이어(몹 스킬)는 안내 창구가 없으므로 로그로 남긴다.
     * (로그가 없으면 몹 스킬이 왜 안 나가는지 추적할 방법이 없다)
     */
    private static void reportBlocked(A_Entity caster, boolean isPlayer, String reason) {
        if (isPlayer) caster.sendMessage(reason);
        else UndefinedWorldCorePlugin.sendLog("Skill blocked: " + reason + " caster:" + caster.getUniqueIdStr());

        SkillDebugManager.onBlocked(caster, reason);
    }

    /** 틱 → 초 환산 (플레이어 표기 전용. 내부 계산은 전부 틱으로 한다) */
    private static double ticksToSeconds(int ticks) {
        return ticks / 20.0;
    }

    /**
     * 캐스팅 완료 시점 처리 (확정 규칙: 비용/쿨타임은 완료 시 적용)
     * 비용 재확인 → 지불 → 쿨타임 적용 → 노드 실행
     */
    private static void completeCast(A_Entity caster, SkillDefinition skill) {
        boolean isPlayer = caster instanceof A_Player;
        CostData costData = skill.getCostData();

        // 캐스팅 동안 침묵이 걸리거나 탈것에 탑승했을 수 있으므로 재확인.
        // (침묵은 버프 부여 시점에 CastingManager.onSilenced가 끊는 것이 1차 방어, 여기가 2차 방어다)
        String blocked = findBlockedReason(caster);
        if (blocked != null) {
            reportBlocked(caster, isPlayer, blocked);
            return;
        }

        // 캐스팅 동안 자원이 소모됐을 수 있으므로 재확인
        CostType lacking = findLackingCost(caster, costData);
        if (lacking != null) {
            if (isPlayer) caster.sendMessage(lacking.name() + " 자원이 부족해 스킬이 취소되었습니다."); //todo 문구/형식 사용자 확정 필요
            SkillDebugManager.onCostLacking(caster, lacking, costData, "캐스팅 완료");
            return;
        }

        //비용처리
        for (CostType costType : CostType.values()) {
            List<Cost<?>> costs = costData.getCost(costType);
            if (costs.isEmpty()) continue;

            try {
                costs.getFirst().payMultiple(caster, costs.toArray(new Cost[]{}));
            } catch (CannotPayCostException | IllegalArgumentException exception) {
                // 지불 실패 시 스킬을 발동시키면 "비용 미지불 무료 시전"이 된다 → 반드시 중단.
                // (앞선 타입이 이미 지불됐을 수 있으므로 어떤 타입에서 끊겼는지 로그로 남긴다)
                UndefinedWorldCorePlugin.sendLog("Cost 지불 에러 발생: " + costType.name()
                        + " Caster:" + (isPlayer ? caster.getName() : caster.getUniqueIdStr())
                        + " - " + exception);
                if (isPlayer) caster.sendMessage("비용을 지불할 수 없어 스킬이 취소되었습니다."); //todo 문구/형식 사용자 확정 필요
                SkillDebugManager.onCostPayFailed(caster, costType, exception);
                return;
            }

            SkillDebugManager.onCostPaid(caster, costType, costs);
        }

        applyCoolDown(caster, skill);
        SkillDebugManager.onCoolDownApplied(caster, skill.getSkillCoolDown());

        List<SkillDefinition.SkillNode> firstNode = skill.getFirstNode();
        if (firstNode == null || firstNode.isEmpty()) {
            UndefinedWorldCorePlugin.sendLog("Skill has no node: " + skill.getSkillName());
            SkillDebugManager.onChainAborted(caster, "실행할 노드가 없음");
            return;
        }

        SkillCTX ctx = new SkillCTX(caster);
        //처리 끝 스킬 시작
        runSkillEffect(ctx, firstNode);
    }

    /**
     * 지불 불가능한 비용 타입을 찾는다.
     * <p>같은 CostType 안에서는 기어별로 나눠 검사하지 않고 <b>합산 한 번</b>으로 검사한다
     * (지불도 동일하게 payMultiple 합산 1회 — §2.6 스킬 코어 7).
     * <p>같은 타입에 구현 클래스가 섞이면 sumCosts가 IllegalArgumentException을 던진다.
     * 이 예외가 그대로 올라가면 스킬 사용 이벤트에서 스택트레이스로 터지므로,
     * 여기서 잡아 "검증 불가 = 지불 불가"로 막고 원인을 로그로 남긴다.
     * @return 부족한 첫 CostType, 전부 지불 가능하면 null
     */
    @Nullable
    private static CostType findLackingCost(A_Entity caster, CostData costData) {
        for (CostType costType : CostType.values()) {
            List<Cost<?>> costs = costData.getCost(costType);
            if (costs.isEmpty()) continue;

            try {
                if (!costs.getFirst().hasCostMultiple(caster, costs.toArray(new Cost[]{}))) return costType;
            } catch (RuntimeException exception) {
                UndefinedWorldCorePlugin.sendLog("Cost 검사 예외: " + costType.name()
                        + " Caster:" + caster.getUniqueIdStr() + " - " + exception);
                return costType;
            }
        }

        return null;
    }

    /**
     * 안내 메시지용 — 해당 타입 비용의 요구 합계.
     * 수치형(Number) 비용만 합산한다. (수치가 아닌 비용 타입이 생기면 0으로 표기된다)
     */
    private static double sumRequired(CostData costData, CostType type) {
        double sum = 0;
        for (Cost<?> cost : costData.getCost(type)) {
            if (cost.getValue() instanceof Number number) sum += number.doubleValue();
        }
        return sum;
    }

    private static CoolTimeMap getCoolTimeMap(A_Entity caster) {
        return caster.getDataMap(UndefinedWorldCorePlugin.instance).getCoolTimeMap("cool_time_data");
    }

    /**
     * 스킬 쿨타임 체크 (설정하지 않음 — 적용은 applyCoolDown)
     * @return 쿨타임이 아직 안 돌았으면 true
     */
    private static boolean isOnCoolDown(A_Entity caster, SkillDefinition skill) {
        // 기어 power 음수가 허용되므로(§2.6 스킬 코어 6) 합산 쿨타임이 음수가 될 수 있다 → 쿨타임 없음으로 취급
        // (사용자 확정 2026-07-30: 최소 쿨타임은 두지 않는다)
        // todo 밸런스 확인 필요 — 모든 기어가 power 0 / cool 0이면 쿨타임 0이라 연타 난사가 가능하다.
        //      코드로 막지 않는다(확정). 기어 수치 설계 단계에서 조정할 것.
        if (skill.getSkillCoolDown() <= 0) return false;

        return !getCoolTimeMap(caster).checkCoolTime(skill.getSkillName());
    }

    /** 남은 쿨타임 (초). 안내 메시지용 — isOnCoolDown이 true인 상태에서만 의미가 있다. */
    private static double getRemainCoolTime(A_Entity caster, SkillDefinition skill) {
        return Math.max(getCoolTimeMap(caster).getLessCoolTime(skill.getSkillName()), 0);
    }

    /** 스킬 쿨타임 적용 (캐스팅 완료 시점에 호출) */
    private static void applyCoolDown(A_Entity caster, SkillDefinition skill) {
        // isOnCoolDown과 같은 기준(<=0은 쿨타임 없음)을 써야 한다.
        // 음수를 그대로 넘기면 과거 시각이 기록돼 checkCoolTime 결과를 신뢰할 수 없다.
        if (skill.getSkillCoolDown() <= 0) return;

        // CoolTimeMap.setCoolTime(String, double)의 기본 단위는 TimeType.SECOND다.
        // skillCoolDown도 초 단위이므로 틱 변환(*20)을 하면 쿨타임이 20배가 된다.
        getCoolTimeMap(caster).setCoolTime(skill.getSkillName(), skill.getSkillCoolDown());
    }

    /**
     * 노드 레벨 실행. 같은 레벨의 노드들은 동시 실행(병렬 그룹)이다.
     * <p>노드 체인은 여러 틱에 걸쳐 실행되므로 중간에 시전자가 사망/퇴장할 수 있다.
     * 확정 규칙(§2.6 스킬 코어 5)에 따라 이 경우 남은 노드를 즉시 중단한다.
     */
    private static void runSkillEffect(SkillCTX originCTX, List<SkillDefinition.SkillNode> nodes) {
        if (nodes == null || nodes.isEmpty()) return;

        A_Entity origin = originCTX.getCTX(CTXType.CASTER);
        if (origin == null) {
            // SkillCTX 생성자가 non-null을 보장하지만, Effect가 CTX를 덮어쓸 수 있으므로 방어한다
            UndefinedWorldCorePlugin.sendLog("Skill chain aborted (no CASTER in ctx)");
            return;
        }
        if (!CastingManager.isCasterAvailable(origin)) {
            UndefinedWorldCorePlugin.sendLog("Skill chain aborted (caster unavailable): " + origin.getUniqueIdStr());
            SkillDebugManager.onChainAborted(origin, "시전자 상태 불가(사망/퇴장)");
            return;
        }

        // 같은 레벨(병렬 그룹)의 노드가 여러 개 실패해도 안내는 한 번만 보낸다 (동일 메시지 도배 방지)
        boolean conditionNotified = false;

        for (SkillDefinition.SkillNode node : nodes) {
            // 동시 실행 노드 간 ctx 격리를 위해 노드마다 복사
            SkillCTX ctx = originCTX.copy();

            // Condition 체크: 하나라도 실패하면 이 노드 스킵
            // (어느 조건이 막았는지는 checkConditions 안에서 디버그로 출력한다 — 판정 지점이 거기다)
            if (!checkConditions(ctx, node)) {
                if (origin instanceof A_Player && !conditionNotified) {
                    origin.sendMessage("발동 조건이 충족되지 않았습니다."); // todo 문구/형식 사용자 확정 필요
                    conditionNotified = true;
                }
                continue;
            }

            SkillDebugManager.onGearStart(origin, node);
            runNodeEffect(ctx, node);
        }
    }

    /**
     * 노드의 Condition 전체 평가. 하나라도 실패하면 false.
     * 조건 구현체의 예외는 노드 실패로 처리하되, 어떤 조건에서 터졌는지 반드시 로그로 남긴다.
     * <p>판정만 하고 안내는 호출부(runSkillEffect)가 한다 — 병렬 그룹에서 메시지가 중복되지 않게 하기 위함.
     */
    private static boolean checkConditions(SkillCTX ctx, SkillDefinition.SkillNode node) {
        for (Condition condition : node.gear().getConditions()) {
            boolean passed;
            try {
                passed = condition.test(ctx);
            } catch (RuntimeException exception) {
                UndefinedWorldCorePlugin.sendLog("Condition 예외 gear:" + node.gear().getID()
                        + " condition:" + condition.getClass().getSimpleName() + " - " + exception);
                SkillDebugManager.onConditionFailed(ctx.getCTX(CTXType.CASTER), node, condition, exception);
                return false;
            }

            if (!passed) {
                UndefinedWorldCorePlugin.sendLog("Condition 실패 gear:" + node.gear().getID()
                        + " condition:" + condition.getClass().getSimpleName());
                SkillDebugManager.onConditionFailed(ctx.getCTX(CTXType.CASTER), node, condition, null);
                return false;
            }
        }

        return true;
    }

    /**
     * 단일 노드의 Effect 실행 → 결과에 따라 다음 노드 체인.
     * 동기 예외/비동기 예외 완료/null 반환을 모두 로그로 남긴다.
     * (처리하지 않으면 체인이 조용히 끊겨 원인 추적이 불가능하다)
     */
    private static void runNodeEffect(SkillCTX ctx, SkillDefinition.SkillNode node) {
        // 디버그 출력 대상은 "이 노드를 시작시킨 시전자"다. Effect가 CTX의 CASTER를 덮어쓸 수 있으므로
        // 실행 전에 붙잡아 둔다 (콜백 시점에 다시 읽으면 엉뚱한 대상에게 출력될 수 있다).
        A_Entity debugCaster = ctx.getCTX(CTXType.CASTER);

        CompletableFuture<EffectResult> completableFuture;
        try {
            completableFuture = node.gear().getEffect().execute(ctx);
        } catch (RuntimeException exception) {
            UndefinedWorldCorePlugin.sendLog("Effect 실행 예외 gear:" + node.gear().getID() + " - " + exception);
            SkillDebugManager.onGearBroken(debugCaster, node, "실행 예외", exception);
            return;
        }

        if (completableFuture == null) {
            UndefinedWorldCorePlugin.sendLog("Effect returned null future gear:" + node.gear().getID());
            SkillDebugManager.onGearBroken(debugCaster, node, "null future", null);
            return;
        }

        completableFuture.whenCompleteAsync((effectResult, throwable) -> {
            try {
                if (throwable != null) {
                    UndefinedWorldCorePlugin.sendLog("Effect 비동기 예외 gear:" + node.gear().getID() + " - " + throwable);
                    SkillDebugManager.onGearBroken(debugCaster, node, "비동기 예외", throwable);
                    return;
                }
                if (effectResult == null) {
                    UndefinedWorldCorePlugin.sendLog("Effect returned null result gear:" + node.gear().getID());
                    SkillDebugManager.onGearBroken(debugCaster, node, "null 결과", null);
                    return;
                }

                SkillDebugManager.onGearResult(debugCaster, node, effectResult);

                switch (effectResult) {
                    case SUCCESS -> runSkillEffect(ctx, node.nextNode());
                    case FAIL -> UndefinedWorldCorePlugin.sendLog("Skill Fail gear:" + node.gear().getID());
                    case ERROR -> UndefinedWorldCorePlugin.sendLog("Skill Error gear:" + node.gear().getID());
                }
            } catch (RuntimeException exception) {
                // whenComplete 콜백의 예외는 반환 future로 흘러가 버려지므로 여기서 잡아 남긴다
                UndefinedWorldCorePlugin.sendLog("Effect 후처리 예외 gear:" + node.gear().getID() + " - " + exception);
                SkillDebugManager.onGearBroken(debugCaster, node, "후처리 예외", exception);
            }
        }, mainThreadExecutor());
    }

    /**
     * Effect가 비동기로 완료해도 이후 노드 처리는 메인스레드에서 수행하기 위한 executor.
     * <p>플러그인 비활성화 중에는 스케줄러 등록이 IllegalPluginAccessException을 던지고,
     * 그 예외는 CompletableFuture 내부로 삼켜져 원인이 남지 않는다 → 미리 걸러 로그로 남긴다.
     */
    private static Executor mainThreadExecutor() {
        return runnable -> {
            UndefinedWorldCorePlugin plugin = UndefinedWorldCorePlugin.instance;
            if (plugin == null || !plugin.isEnabled()) {
                UndefinedWorldCorePlugin.sendLog("Skill chain dropped (plugin disabled)");
                return;
            }

            Bukkit.getScheduler().runTask(plugin, runnable);
        };
    }

    /**
     * Effect/Condition/Cost 팩토리 전체 등록. (T20 확정: onEnable 초입에서 호출)
     * Gear 아이템(Nexo)이 로드 시 이 팩토리들을 참조하므로 Nexo/item 모듈보다 먼저 등록돼야 한다.
     */
    public static void setFactories() {
        // ── Effect (수정자) ──
        // valueClass는 대상 클래스의 생성자 시그니처와 일치해야 한다.
        // (SimpleFactory는 원시↔래퍼 폴백만 하므로 int 생성자에 double.class를 주면 생성 자체가 실패한다)
        setEffectFactory(new SimpleFactory<>("pierce_increase", "increase", PierceIncreaseEffect.class, int.class));
        // ── CTX 수정자 (§2.11 타입 규칙: double.class → 곱셈 / int.class → 덧셈 / 그 외 → 덮어쓰기) ──
        // size_increase(덧셈)는 size_multiply(곱셈)로 교체됐다. 연산 의미가 바뀌었으므로 기존 기어 YAML 값 재조정 필요.
        setEffectFactory(new SimpleFactory<>("size_multiply", "multiply", SizeMultiplyEffect.class, double.class));
        setEffectFactory(new SimpleFactory<>("speed_multiply", "multiply", SpeedMultiplyEffect.class, double.class));
        setEffectFactory(new SimpleFactory<>("range_multiply", "multiply", RangeMultiplyEffect.class, double.class));
        setEffectFactory(new SimpleFactory<>("damage_multiply", "multiply", DamageMultiplyEffect.class, double.class));
        setEffectFactory(new SimpleFactory<>("time_multiply", "multiply", TimeMultiplyEffect.class, double.class));
        setEffectFactory(new SimpleFactory<>("render_multiply", "multiply", RenderMultiplyEffect.class, double.class));

        setEffectFactory(new SimpleFactory<>("count_increase", "increase", CountIncreaseEffect.class, int.class));
        setEffectFactory(new SimpleFactory<>("repeat_increase", "increase", RepeatIncreaseEffect.class, int.class));
        setEffectFactory(new SimpleFactory<>("chain_increase", "increase", ChainIncreaseEffect.class, int.class));
        setEffectFactory(new SimpleFactory<>("level_increase", "increase", LevelIncreaseEffect.class, int.class));
        setEffectFactory(new SimpleFactory<>("target_count_increase", "increase", TargetCountIncreaseEffect.class, int.class));

        setEffectFactory(new SimpleFactory<>("search_range_effect", "searchRange", SearchRangeEffect.class, double.class));
        setEffectFactory(new SimpleFactory<>("search_type_effect", "searchType", SearchTypeEffect.class, Target.SearchType.class));
        setEffectFactory(new SimpleFactory<>("target_faction_effect", "faction", TargetFactionEffect.class, FactionType.class));
        setEffectFactory(new SimpleFactory<>("projectile_type_effect", "projectileType", ProjectileTypeEffect.class, ProjectileType.class));
        setEffectFactory(new SimpleFactory<>("projectile_shape_effect", "projectileShape", ProjectileShapeEffect.class, ProjectilesShape.class));

        setEffectFactory(new SimpleFactory<>("search_center_effect", "mode", SearchCenterEffect.class, SearchCenterEffect.CenterMode.class));
        setEffectFactory(new SimpleFactory<>("elemental_effect", "elemental",  ElementalEffect.class, ElementalType.class));
        // 이후 기어들이 주는 데미지의 유형을 바꾼다 (§2.6 DAMAGE_TYPE CTX)
        setEffectFactory(new SimpleFactory<>("damage_type_effect", "damageType", DamageTypeEffect.class, DamageType.class));
        setEffectFactory(new SimpleFactory<>("merge", MergeEffect.class, int.class));

        // ── Effect (실행) ──
        setEffectFactory(new FunctionFactory<>("thunder", section -> new ThunderEffect()));
        setEffectFactory(new FunctionFactory<>("target", section -> new TargetEffect(
                section.getDouble("range", 8.0),
                section.getInt("count", 1),
                FactionType.valueOf(section.getString("faction", "ENEMY")))));
        setEffectFactory(new FunctionFactory<>("damage", section -> new DamageEffect(
                DamageType.valueOf(section.getString("damageType", "PHYSICAL")),
                section.getDouble("scale", 1.0))));
        setEffectFactory(new FunctionFactory<>("heal", section -> new HealEffect(
                section.getDouble("amount", 1.0),
                section.getBoolean("self", true))));
        setEffectFactory(new FunctionFactory<>("buff", section -> new BuffEffect(
                BuffType.valueOf(section.getString("buffType", "GLOWING")),
                section.getInt("level", 1),
                section.getLong("duration", 100L),
                section.getBoolean("self", true))));
        setEffectFactory(new FunctionFactory<>("arrow", section -> new ArrowEffect(
                section.getDouble("speed", 2.0))));
        setEffectFactory(new FunctionFactory<>("sword_aura", section -> new SwordAuraEffect(
                section.getDouble("speed", 0.8),
                section.getDouble("range", 6.0),
                section.getDouble("size", 1.2),
                section.getDouble("scale", 1.0))));
        setEffectFactory(new FunctionFactory<>("projectile", section -> new ProjectileEffect(
                section.getDouble("speed", 1.0),
                section.getDouble("range", 20.0),
                section.getDouble("size", 0.8))));

        // ── Condition ──
        setConditionFactory(new FunctionFactory<>("stat", section -> {
            Stat stat = Stat.name(section.getString("stat", ""));
            if (stat == null) throw new IllegalArgumentException("Invalid stat: " + section.getString("stat"));
            return new StatCondition(stat, section.getInt("min", 0));
        }));
        setConditionFactory(new FunctionFactory<>("weapon", section -> new WeaponCondition(
                section.getString("itemCode", ""))));
        setConditionFactory(new FunctionFactory<>("health", section -> new HealthCondition(
                section.getDouble("ratio", 1.0),
                section.getBoolean("above", false))));
        setConditionFactory(new FunctionFactory<>("resource", section -> new ResourceCondition(
                CostType.valueOf(section.getString("resource", "MANA")),
                section.getDouble("min", 0))));
        setConditionFactory(new FunctionFactory<>("target_exist", section -> new TargetExistCondition(
                section.getDouble("range", 8.0),
                FactionType.valueOf(section.getString("faction", "ENEMY")))));
        setConditionFactory(new FunctionFactory<>("buff", section -> new BuffCondition(
                BuffType.valueOf(section.getString("buffType", "GLOWING")),
                section.getBoolean("has", true))));

        // ── Cost ──
        costMap.put(CostType.MANA, new SimpleFactory<>("mana", ManaCost.class, double.class));
        costMap.put(CostType.HEALTH, new SimpleFactory<>("health", HealthCost.class, double.class));
        costMap.put(CostType.STAMINA, new SimpleFactory<>("stamina", StaminaCost.class, double.class));
    }
}
