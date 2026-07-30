package org.red.minecraft.uw.core.skill.effect.modifier;

import org.red.minecraft.uw.core.skill.CTXType;
import org.red.minecraft.uw.core.skill.SkillCTX;
import org.red.minecraft.uw.core.skill.effect.Effect;
import org.red.minecraft.uw.core.skill.effect.EffectResult;
import org.red.minecraft.uw.core.skill.effect.EffectType;

import java.util.concurrent.CompletableFuture;

/**
 * 레벨 값({@link CTXType#LEVEL})을 더하는 수정자 기어.
 *
 * <p>LEVEL 은 {@code int.class} CTX 이므로 <b>덧셈</b>이다 (수정자 연산 규칙은
 * {@link SimpleModifierEffect} 클래스 주석 참고).
 *
 * <p><b>⚠️ 수정자 층과 소비 층을 혼동하지 말 것 — 두 층의 연산이 다르다.</b>
 * <ul>
 *   <li><b>수정자 층(이 클래스)</b>: CTX 값 자체를 <b>더한다.</b> LEVEL 1 에 +1 → LEVEL 2.</li>
 *   <li><b>소비 층({@code BuffEffect.resolveLevel})</b>: 그 값을 <b>배율로 쓴다.</b>
 *       기어 YAML level × CTX.LEVEL → level 3 짜리 버프가 LEVEL 2 에서 6 이 된다.</li>
 * </ul>
 * 즉 "수정자는 덧셈, 소비처는 곱셈"이 정상이며 서로 모순이 아니다.
 * 수정자 연산은 CTXType 의 선언 타입이 정하고, 소비 방식은 각 소비처의 규약이 정한다.
 *
 * <p>소비처: {@code BuffEffect}(버프 레벨).
 */
public class LevelIncreaseEffect implements Effect {

    private final int increase;

    public LevelIncreaseEffect(int increase) {
        this.increase = increase;
    }

    @Override
    public CompletableFuture<EffectResult> execute(SkillCTX ctx) {
        // 명시적 기본값을 둔다 — CTXType.LEVEL 의 기본값이 제거돼도 언박싱 NPE 가 나지 않는다.
        int level = ctx.getCTX(CTXType.LEVEL, 1);

        // 클램프는 상한이 아니라 int 오버플로 방지다 (CountIncreaseEffect 와 동일).
        long sum = (long) level + this.increase;
        ctx.setCTX(CTXType.LEVEL, (int) Math.clamp(sum, Integer.MIN_VALUE, Integer.MAX_VALUE));
        return CompletableFuture.completedFuture(EffectResult.SUCCESS);
    }

    @Override
    public EffectType[] getEffectTypes() {
        return new EffectType[]{EffectType.MODIFIER};
    }
}
