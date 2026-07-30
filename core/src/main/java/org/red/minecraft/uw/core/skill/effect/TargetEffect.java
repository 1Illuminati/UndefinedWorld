package org.red.minecraft.uw.core.skill.effect;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.red.minecraft.dellarte.library.CommediaDellarte;
import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.uw.core.UndefinedWorldCorePlugin;
import org.red.minecraft.uw.core.skill.CTXType;
import org.red.minecraft.uw.core.skill.SkillCTX;
import org.red.minecraft.uw.core.skill.target.EntityTarget;
import org.red.minecraft.uw.core.skill.target.Target;
import org.red.minecraft.uw.core.skill.target.faction.Faction;
import org.red.minecraft.uw.core.skill.target.faction.FactionType;

import java.util.concurrent.CompletableFuture;

/**
 * 타겟 탐색 이펙트 — 시전자 주변에서 대상을 찾아 LAST_TARGET_INFO에 저장한다.
 * 후속 노드(데미지/버프 등)에 대상을 전달하는 기본 기어.
 *
 * 탐색 파라미터 우선순위 (확정):
 *   TARGET_COUNT  — 기어 YAML 값에 <b>더해지는 가산값</b> (§2.11 타입 규칙: int.class → 덧셈)
 *   RANGE         — 기어 YAML 값에 곱해지는 배율 (§2.11 타입 규칙: double.class → 곱셈)
 *   TARGET_FACTION / SEARCH_TYPE — CTX에 값이 있으면 CTX, 없으면 기어 YAML 값
 *   SEARCH_CENTER                — CTX에 값이 있으면 CTX, 없으면 시전자 위치
 * 대상이 없으면 FAIL.
 *
 * <p><b>덮어쓰기 계열 CTX(TARGET_FACTION/SEARCH_TYPE/SEARCH_CENTER)는 CTXType에 기본값이 없어야</b>
 * 이 폴백이 성립한다. 기본값이 있으면 SkillCTX 생성 시 항상 채워져 "CTX에 값이 없을 때"가 성립하지 않고
 * 기어 YAML 값이 죽는다.
 * <p>반면 <b>연산 계열 CTX(TARGET_COUNT/RANGE)는 기본값이 항등원</b>이라 폴백이 필요 없다 —
 * 수정자 기어가 없으면 TARGET_COUNT=0(덧셈 항등원), RANGE=1.0(곱셈 항등원)이라 YAML 값이 그대로 나온다.
 */
public class TargetEffect implements Effect {

    private final double range;
    private final int count;
    private final FactionType faction;

    public TargetEffect(double range, int count, FactionType faction) {
        this.range = range;
        this.count = count;
        this.faction = faction;
    }

    @Override
    public CompletableFuture<EffectResult> execute(SkillCTX ctx) {
        A_Entity caster = ctx.getCTX(CTXType.CASTER);

        Location center = ctx.hasCTX(CTXType.SEARCH_CENTER)
                ? ctx.getCTX(CTXType.SEARCH_CENTER)
                : caster.getLocation();

        double finalRange = range * (double) ctx.getCTX(CTXType.RANGE);

        // TARGET_COUNT 는 절대 개수가 아니라 기어 YAML 값에 더해지는 가산값이다.
        // 기본값이 0(덧셈 항등원)이므로 수정자 기어가 없으면 YAML count 가 그대로 쓰인다.
        // ⛔ getCTX(TARGET_COUNT, count) 처럼 절대 개수로 읽지 말 것 — 기본값 0 이 항상 채워져 대상이 0 이 된다.
        int finalCount = count + ctx.getCTX(CTXType.TARGET_COUNT, 0);
        FactionType finalFaction = ctx.getCTX(CTXType.TARGET_FACTION, faction);
        Target.SearchType searchType = ctx.getCTX(CTXType.SEARCH_TYPE, Target.SearchType.RANGE_CIRCLE);

        Entity[] found = new EntityTarget(center, finalCount, finalRange, searchType)
                .getTargets(Faction.predicate(caster, finalFaction));

        if (found.length == 0) {
            UndefinedWorldCorePlugin.sendLog("TargetEffect: 대상 없음 (range=" + finalRange
                    + ", count=" + finalCount + ", faction=" + finalFaction + ", searchType=" + searchType + ")");
            return CompletableFuture.completedFuture(EffectResult.FAIL);
        }

        A_Entity[] result = new A_Entity[found.length];
        for (int i = 0; i < found.length; i++) {
            result[i] = CommediaDellarte.getAEntity(found[i]);
        }

        ctx.setCTX(CTXType.LAST_TARGET_INFO, result);
        return CompletableFuture.completedFuture(EffectResult.SUCCESS);
    }

    @Override
    public EffectType[] getEffectTypes() {
        return new EffectType[]{EffectType.TARGET};
    }
}
