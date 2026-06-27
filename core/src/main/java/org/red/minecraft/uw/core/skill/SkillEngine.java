package org.red.minecraft.uw.core.skill;

import org.jetbrains.annotations.Nullable;
import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.dellarte.library.entity.A_Player;
import org.red.minecraft.dellarte.library.util.map.CoolTimeMap;
import org.red.minecraft.uw.core.UndefinedWorldCorePlugin;
import org.red.minecraft.uw.core.combat.ElementalType;
import org.red.minecraft.uw.core.exeception.CannotPayCostException;
import org.red.minecraft.uw.core.skill.condition.Condition;
import org.red.minecraft.uw.core.skill.cost.*;
import org.red.minecraft.uw.core.skill.effect.Effect;
import org.red.minecraft.uw.core.skill.effect.EffectResult;
import org.red.minecraft.uw.core.skill.effect.PierceIncreaseEffect;
import org.red.minecraft.uw.core.skill.effect.modifier.elemental.ElementalEffect;
import org.red.minecraft.uw.core.skill.factory.SimpleFactory;
import org.red.minecraft.uw.core.skill.factory.SkillFactory;

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
        return costMap.get(type.name());
    }

    public static void runSkill(A_Entity caster, SkillDefinition skill) {
        boolean isPlayer = caster instanceof A_Player;
        CostData costData = skill.getCostData();

        if (skillCoolCheckSet(caster, skill)) {
            if (isPlayer) caster.sendMessage(""); //todo coolTime 메세지
            return;
        }

        // 비용 체크
        for (CostType costType : CostType.values()) {
            List<Cost<?>> costs = costData.getCost(costType);

            if (!costs.getFirst().hasCostMultiple(caster, costs.toArray(new Cost[]{}))) {
                if (isPlayer) caster.sendMessage(""); //todo cost 부족 메세지
                return;
            }
        }

        //비용처리
        for (CostType costType : CostType.values()) {
            List<Cost<?>> costs = costData.getCost(costType);
            try {
                costs.getFirst().payMultiple(caster, costs.toArray(new Cost[]{}));
            } catch (CannotPayCostException exception) {
                UndefinedWorldCorePlugin.sendLog("Cost 지불 에러 발생: " + costType.name() + " Caster:" + (isPlayer ? caster.getName() : caster.getUniqueIdStr()));
            }
        }

        SkillCTX ctx = new SkillCTX(caster);
        //처리 끝 스킬 시작
        runSkillEffect(ctx, skill.getFirstNode());
    }

    /**
     * 스킬의 쿨타임 체크 및 설정
     * 쿨타임 이상없으면 false 아직 안돌았으면 true
     * @param caster 시전자
     * @param skill 스킬
     * @return 사용가능 false, 사용 불가능 true
     */
    private static boolean skillCoolCheckSet(A_Entity caster, SkillDefinition skill) {
        // 쿨타임 체크 및 처리
        boolean isPlayer = caster instanceof A_Player;
        int cool = skill.getSkillCoolDown();

        if (cool == 0) return false;

        CoolTimeMap map = caster.getDataMap(UndefinedWorldCorePlugin.instance).getCoolTimeMap("cool_time_data");

        if (!map.checkCoolTime(skill.getSkillName())) return true;

        map.setCoolTime(skill.getSkillName(), skill.getSkillCoolDown() * 20);
        return false;
    }

    private static void runSkillEffect(SkillCTX ctx, SkillDefinition.SkillNode node) {
        A_Entity caster = ctx.getCTX(CTXType.CASTER);
        for (Condition condition : node.gear().getConditions()) {
            if (!condition.test(ctx)) {
                if (caster instanceof A_Player) caster.sendMessage(""); //todo 조건 불충분 메세지
                return;
            }
        }

        CompletableFuture<EffectResult> completableFuture = node.gear().getEffect().execute(ctx);

        completableFuture.thenAccept(effectResult -> {
           switch (effectResult) {
               case SUCCESS -> runSkillEffect(ctx, node);
               case FAIL -> UndefinedWorldCorePlugin.sendLog("Skill Fail gear:" + node.gear().getID());
               case ERROR -> UndefinedWorldCorePlugin.sendLog("Skill Error gear:" + node.gear().getID());
           }
        });
    }

    public static void setFactories() {
        setEffectFactory(new SimpleFactory<>("pierce_increase", "increase", PierceIncreaseEffect.class, double.class));
        setEffectFactory(new SimpleFactory<>("elemental_effect", "elemental",  ElementalEffect.class, ElementalType.class));

        costMap.put(CostType.MANA, new SimpleFactory<>("mana", ManaCost.class, double.class));
        costMap.put(CostType.HEALTH, new SimpleFactory<>("health", HealthCost.class, double.class));
        costMap.put(CostType.STAMINA, new SimpleFactory<>("stamina", StaminaCost.class, double.class));
    }
}
