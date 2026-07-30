package org.red.minecraft.uw.core.skill.effect;

import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.uw.core.UndefinedWorldCore;
import org.red.minecraft.uw.core.UndefinedWorldCorePlugin;
import org.red.minecraft.uw.core.combat.buff.BuffContext;
import org.red.minecraft.uw.core.combat.buff.BuffType;
import org.red.minecraft.uw.core.skill.CTXType;
import org.red.minecraft.uw.core.skill.SkillCTX;

import java.util.concurrent.CompletableFuture;

/**
 * 버프/디버프 부여 이펙트 — 자신(self=true) 또는 LAST_TARGET_INFO 대상에게 버프를 부여한다.
 * 지속시간은 틱 단위. 레벨 기반 버프(GLOWING/POISON/REGENERATION 등)에 사용한다.
 * 대상이 없거나 실제로 한 대상에도 부여하지 못하면 FAIL.
 * (ATTRIBUTE_BUFF처럼 추가 데이터가 필요한 버프는 별도 이펙트 확정 후 — todo)
 *
 * CTX 수정자 반영 (확정):
 *   LEVEL — 버프 레벨 배율 (기어 YAML level × CTX.LEVEL)
 *   TIME  — 지속시간 배율 (기어 YAML duration × CTX.TIME)
 * 둘 다 <b>배율</b>이다. 자세한 근거는 resolveLevel/resolveDuration 주석 참고.
 */
public class BuffEffect implements Effect {

    private final BuffType buffType;
    private final int level;
    private final long durationTicks;
    private final boolean self;

    public BuffEffect(BuffType buffType, int level, long durationTicks, boolean self) {
        this.buffType = buffType;
        this.level = level;
        this.durationTicks = durationTicks;
        this.self = self;
    }

    @Override
    public CompletableFuture<EffectResult> execute(SkillCTX ctx) {
        A_Entity caster = ctx.getCTX(CTXType.CASTER);

        A_Entity[] targets;
        if (self) {
            targets = new A_Entity[]{caster};
        } else {
            targets = ctx.hasCTX(CTXType.LAST_TARGET_INFO) ? ctx.getCTX(CTXType.LAST_TARGET_INFO) : null;
            if (targets == null || targets.length == 0) {
                UndefinedWorldCorePlugin.sendLog("BuffEffect: LAST_TARGET_INFO 가 비어 있어 실패 (앞선 타겟 기어 확인 필요) buff:" + buffType);
                return CompletableFuture.completedFuture(EffectResult.FAIL);
            }
        }

        int finalLevel = resolveLevel(ctx);
        long finalDuration = resolveDuration(ctx);

        BuffContext buffCtx = BuffContext.builder(finalLevel).caster(caster).build();

        int applied = 0;
        for (A_Entity target : targets) {
            if (target == null) continue;

            // 다른 이펙트와 동일하게 죽은 대상은 건너뛴다.
            // (버프 매니저는 엔티티 생사를 보지 않으므로 시체에 버프가 붙고 만료 처리만 남는다)
            if (target.isDead()) continue;

            // applyBuff 는 적용 실패 시 예외를 다시 던진다. 대상 하나의 실패로 나머지가 통째로 누락되지 않도록 격리한다.
            try {
                UndefinedWorldCore.getBuffManager().applyBuff(target, buffType, buffCtx, finalDuration, false);
                applied++;
            } catch (RuntimeException exception) {
                UndefinedWorldCorePlugin.sendLog("BuffEffect 대상 처리 실패 buff:" + buffType
                        + " target:" + target.getUniqueIdStr() + " - " + exception);
            }
        }

        // 대상은 있었으나 전부 무효라 아무것도 부여하지 못한 경우도 실패로 본다 (다른 이펙트와 동일 기준)
        if (applied == 0) {
            UndefinedWorldCorePlugin.sendLog("BuffEffect: 유효한 대상이 없어 실패 (buff=" + buffType
                    + ", self=" + self + ", targets=" + targets.length + ")");
            return CompletableFuture.completedFuture(EffectResult.FAIL);
        }

        return CompletableFuture.completedFuture(EffectResult.SUCCESS);
    }

    /**
     * 버프 레벨 = 기어 YAML level × CTX.LEVEL <b>(배율)</b>.
     * <p>배율로 해석하는 근거: CTXType.LEVEL 의 기본값이 <b>1</b>이다. 덧셈 수정자라면 기본값이 0이어야
     * 항등이 되는데 1이므로, 곱셈 항등원으로 쓰라는 선언이다. (SIZE/RANGE/SPEED/DAMAGE 배율과 동일한 규약)
     * <p>레벨 0 이하는 의미가 없으므로 최소 1로 잡는다. int 오버플로를 피하려고 long 으로 계산 후 클램프한다.
     */
    private int resolveLevel(SkillCTX ctx) {
        long scaled = (long) level * ctx.getCTX(CTXType.LEVEL, 1);
        return (int) Math.clamp(scaled, 1, Integer.MAX_VALUE);
    }

    /**
     * 지속시간(틱) = 기어 YAML duration × CTX.TIME <b>(배율)</b>.
     * <p>CTXType.TIME 의 기본값이 1.0 이라 위와 같은 이유로 배율이다.
     * <p>0틱 이하면 버프가 붙자마자 사라져 부여 자체가 무의미하므로 최소 1틱으로 잡고,
     * 비유한 값(수정자가 NaN/무한을 만든 경우)은 배율을 무시하고 원래 값을 쓴다.
     */
    private long resolveDuration(SkillCTX ctx) {
        double scaled = durationTicks * ctx.getCTX(CTXType.TIME, 1.0);
        if (!Double.isFinite(scaled)) {
            UndefinedWorldCorePlugin.sendLog("BuffEffect: TIME 배율이 유효하지 않아 무시 buff:" + buffType);
            return durationTicks;
        }
        return (long) Math.clamp(scaled, 1.0, (double) Long.MAX_VALUE);
    }

    @Override
    public EffectType[] getEffectTypes() {
        return new EffectType[]{self ? EffectType.BUFF : EffectType.DEBUFF};
    }
}
