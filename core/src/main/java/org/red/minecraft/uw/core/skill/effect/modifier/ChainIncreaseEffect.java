package org.red.minecraft.uw.core.skill.effect.modifier;

import org.red.minecraft.uw.core.skill.CTXType;
import org.red.minecraft.uw.core.skill.SkillCTX;
import org.red.minecraft.uw.core.skill.effect.Effect;
import org.red.minecraft.uw.core.skill.effect.EffectResult;
import org.red.minecraft.uw.core.skill.effect.EffectType;

import java.util.concurrent.CompletableFuture;

/**
 * 연쇄 횟수({@link CTXType#CHAIN})를 더하는 수정자 기어. ({@code PierceIncreaseEffect} 와 동일한 패턴)
 *
 * <p>CHAIN 은 배율이 아니라 <b>절대 횟수</b>이므로 덧셈이다.
 *
 * <p>⚠️ <b>현재 이 CTX 를 읽는 소비처가 없다 — 이 기어를 넣어도 실제 효과는 없다.</b>
 * §2.8 확정에 따라 미사용 CTX 자체는 제거하지 않으므로 수정자만 먼저 만들어 둔다.
 * 소비처(연쇄 낙뢰 등)가 생기면 이 주석을 지울 것.
 *
 * <p>클램프는 상한이 아니라 int 오버플로 방지다 ({@code CountIncreaseEffect} 와 동일).
 */
public class ChainIncreaseEffect implements Effect {

    private final int increase;

    public ChainIncreaseEffect(int increase) {
        this.increase = increase;
    }

    @Override
    public CompletableFuture<EffectResult> execute(SkillCTX ctx) {
        int chain = ctx.getCTX(CTXType.CHAIN, 1);

        long sum = (long) chain + this.increase;
        ctx.setCTX(CTXType.CHAIN, (int) Math.clamp(sum, Integer.MIN_VALUE, Integer.MAX_VALUE));
        return CompletableFuture.completedFuture(EffectResult.SUCCESS);
    }

    @Override
    public EffectType[] getEffectTypes() {
        return new EffectType[]{EffectType.MODIFIER};
    }
}
