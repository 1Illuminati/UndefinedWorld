package org.red.minecraft.uw.core.skill.effect;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.red.minecraft.dellarte.library.CommediaDellarte;
import org.red.minecraft.dellarte.library.entity.A_Entity;
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
 * range에는 CTX.RANGE 배율이 적용된다. 대상이 없으면 FAIL.
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

        Entity[] found = new EntityTarget(center, count, finalRange, Target.SearchType.RANGE_CIRCLE)
                .getTargets(Faction.predicate(caster, faction));

        if (found.length == 0) return CompletableFuture.completedFuture(EffectResult.FAIL);

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
