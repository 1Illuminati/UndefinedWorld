package org.red.minecraft.uw.core.skill.effect.modifier;

import org.red.minecraft.uw.core.skill.CTXType;
import org.red.minecraft.uw.core.skill.SkillCTX;
import org.red.minecraft.uw.core.skill.effect.Effect;
import org.red.minecraft.uw.core.skill.effect.EffectResult;
import org.red.minecraft.uw.core.skill.effect.EffectType;

import java.util.concurrent.CompletableFuture;

/**
 * 발사 개수({@link CTXType#COUNT})를 더하는 수정자 기어. ({@code PierceIncreaseEffect} 와 동일한 패턴)
 *
 * <p>COUNT 는 배율이 아니라 <b>절대 개수</b>다 — 소비처가 배율로 곱하지 않고 개수 그대로 쓴다
 * ({@code ProjectileEffect.resolveDirections}, {@code ArrowEffect} 의 발사 루프).
 * 그래서 곱셈이 아니라 <b>덧셈</b>이며, 기본값 1 에 +2 를 더하면 3 발이 된다.
 *
 * <p><b>개수 상한은 두지 않는다(사용자 확정).</b> 과도한 발사는 기어 파워/쿨타임/비용으로 관리한다.
 * 아래 클램프는 상한이 아니라 <b>int 오버플로 방지</b>다 — 오버플로하면 개수가 음수로 뒤집혀
 * 발사 자체가 최소값으로 죽는다.
 */
public class CountIncreaseEffect implements Effect {

    private final int increase;

    public CountIncreaseEffect(int increase) {
        this.increase = increase;
    }

    @Override
    public CompletableFuture<EffectResult> execute(SkillCTX ctx) {
        // 명시적 기본값을 둔다 — CTXType.COUNT 의 기본값이 제거돼도 언박싱 NPE 가 나지 않는다.
        int count = ctx.getCTX(CTXType.COUNT, 1);

        long sum = (long) count + this.increase;
        ctx.setCTX(CTXType.COUNT, (int) Math.clamp(sum, Integer.MIN_VALUE, Integer.MAX_VALUE));
        return CompletableFuture.completedFuture(EffectResult.SUCCESS);
    }

    @Override
    public EffectType[] getEffectTypes() {
        return new EffectType[]{EffectType.MODIFIER};
    }
}
