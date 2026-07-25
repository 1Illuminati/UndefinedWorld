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
import org.red.minecraft.uw.core.skill.effect.modifier.elemental.ElementalEffect;
import org.red.minecraft.uw.core.skill.factory.FunctionFactory;
import org.red.minecraft.uw.core.skill.factory.SimpleFactory;
import org.red.minecraft.uw.core.skill.factory.SkillFactory;
import org.red.minecraft.uw.core.skill.target.faction.FactionType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

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

        // 캐스팅 규칙 2: 캐스팅 중 다른 스킬 사용 → 기존 캐스팅 취소 후 새 스킬 진행
        CastingManager.cancelCast(caster);

        // 침묵(수속성 디버프) 상태면 스킬 사용 불가
        if (UndefinedWorldCore.getBuffManager().hasBuff(caster, BuffType.SILENCE)) {
            if (isPlayer) caster.sendMessage("침묵 상태에서는 스킬을 사용할 수 없습니다."); //todo 문구/형식 사용자 확정 필요
            return;
        }

        // 쿨타임 사전 체크 (적용은 캐스팅 완료 시점 — 확정 규칙)
        if (isOnCoolDown(caster, skill)) {
            if (isPlayer) caster.sendMessage("아직 스킬이 준비되지 않았습니다."); //todo 문구/형식(MiniMessage 등) 사용자 확정 필요
            return;
        }

        // 비용 사전 체크 (지불은 캐스팅 완료 시점 — 확정 규칙)
        CostType lacking = findLackingCost(caster, skill.getCostData());
        if (lacking != null) {
            if (isPlayer) caster.sendMessage(lacking.name() + " 자원이 부족합니다."); //todo 문구/형식 사용자 확정 필요
            return;
        }

        int castTicks = skill.getCastingTime() * 20; //todo castingTime 단위 확인 필요 (쿨타임과 동일하게 초로 가정)
        if (castTicks <= 0) {
            completeCast(caster, skill);
            return;
        }

        CastingManager.startCast(caster, castTicks, () -> completeCast(caster, skill));
    }

    /**
     * 캐스팅 완료 시점 처리 (확정 규칙: 비용/쿨타임은 완료 시 적용)
     * 비용 재확인 → 지불 → 쿨타임 적용 → 노드 실행
     */
    private static void completeCast(A_Entity caster, SkillDefinition skill) {
        boolean isPlayer = caster instanceof A_Player;
        CostData costData = skill.getCostData();

        // 캐스팅 동안 자원이 소모됐을 수 있으므로 재확인
        CostType lacking = findLackingCost(caster, costData);
        if (lacking != null) {
            if (isPlayer) caster.sendMessage(lacking.name() + " 자원이 부족해 스킬이 취소되었습니다."); //todo 문구/형식 사용자 확정 필요
            return;
        }

        //비용처리
        for (CostType costType : CostType.values()) {
            List<Cost<?>> costs = costData.getCost(costType);
            if (costs.isEmpty()) continue;

            try {
                costs.getFirst().payMultiple(caster, costs.toArray(new Cost[]{}));
            } catch (CannotPayCostException exception) {
                UndefinedWorldCorePlugin.sendLog("Cost 지불 에러 발생: " + costType.name() + " Caster:" + (isPlayer ? caster.getName() : caster.getUniqueIdStr()));
            }
        }

        applyCoolDown(caster, skill);

        SkillCTX ctx = new SkillCTX(caster);
        //처리 끝 스킬 시작
        runSkillEffect(ctx, skill.getFirstNode());
    }

    /**
     * 지불 불가능한 비용 타입을 찾는다.
     * @return 부족한 첫 CostType, 전부 지불 가능하면 null
     */
    @Nullable
    private static CostType findLackingCost(A_Entity caster, CostData costData) {
        for (CostType costType : CostType.values()) {
            List<Cost<?>> costs = costData.getCost(costType);
            if (costs.isEmpty()) continue;

            if (!costs.getFirst().hasCostMultiple(caster, costs.toArray(new Cost[]{}))) return costType;
        }

        return null;
    }

    /**
     * 스킬 쿨타임 체크 (설정하지 않음 — 적용은 applyCoolDown)
     * @return 쿨타임이 아직 안 돌았으면 true
     */
    private static boolean isOnCoolDown(A_Entity caster, SkillDefinition skill) {
        if (skill.getSkillCoolDown() == 0) return false;

        CoolTimeMap map = caster.getDataMap(UndefinedWorldCorePlugin.instance).getCoolTimeMap("cool_time_data");
        return !map.checkCoolTime(skill.getSkillName());
    }

    /** 스킬 쿨타임 적용 (캐스팅 완료 시점에 호출) */
    private static void applyCoolDown(A_Entity caster, SkillDefinition skill) {
        if (skill.getSkillCoolDown() == 0) return;

        CoolTimeMap map = caster.getDataMap(UndefinedWorldCorePlugin.instance).getCoolTimeMap("cool_time_data");
        map.setCoolTime(skill.getSkillName(), skill.getSkillCoolDown() * 20);
    }

    private static void runSkillEffect(SkillCTX originCTX, List<SkillDefinition.SkillNode> nodes) {
        for (SkillDefinition.SkillNode node : nodes) {
            // 동시 실행 노드 간 ctx 격리를 위해 노드마다 복사
            SkillCTX ctx = originCTX.copy();
            A_Entity caster = ctx.getCTX(CTXType.CASTER);

            // Condition 체크: 하나라도 실패하면 이 노드 스킵
            boolean conditionFailed = false;
            for (Condition condition : node.gear().getConditions()) {
                if (!condition.test(ctx)) {
                    if (caster instanceof A_Player) caster.sendMessage("발동 조건이 충족되지 않았습니다."); // todo 문구/형식 사용자 확정 필요
                    conditionFailed = true;
                    break;
                }
            }
            if (conditionFailed) continue;

            CompletableFuture<EffectResult> completableFuture = node.gear().getEffect().execute(ctx);

            completableFuture.thenAcceptAsync(effectResult -> {
                switch (effectResult) {
                    case SUCCESS -> {
                        List<SkillDefinition.SkillNode> next = node.nextNode();
                        if (next != null && !next.isEmpty()) runSkillEffect(ctx, next);
                    }
                    case FAIL -> UndefinedWorldCorePlugin.sendLog("Skill Fail gear:" + node.gear().getID());
                    case ERROR -> UndefinedWorldCorePlugin.sendLog("Skill Error gear:" + node.gear().getID());
                }
            }, runnable -> Bukkit.getScheduler().runTask(UndefinedWorldCorePlugin.instance, runnable));
        }
    }

    /**
     * Effect/Condition/Cost 팩토리 전체 등록. (T20 확정: onEnable 초입에서 호출)
     * Gear 아이템(Nexo)이 로드 시 이 팩토리들을 참조하므로 Nexo/item 모듈보다 먼저 등록돼야 한다.
     */
    public static void setFactories() {
        // ── Effect (수정자) ──
        setEffectFactory(new SimpleFactory<>("pierce_increase", "increase", PierceIncreaseEffect.class, double.class));
        setEffectFactory(new SimpleFactory<>("size_increase", "increase", SizeIncreaseEffect.class, double.class));
        setEffectFactory(new SimpleFactory<>("elemental_effect", "elemental",  ElementalEffect.class, ElementalType.class));
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
