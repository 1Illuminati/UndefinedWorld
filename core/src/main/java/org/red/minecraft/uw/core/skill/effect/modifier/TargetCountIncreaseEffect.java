package org.red.minecraft.uw.core.skill.effect.modifier;

import org.red.minecraft.uw.core.skill.CTXType;
import org.red.minecraft.uw.core.skill.SkillCTX;
import org.red.minecraft.uw.core.skill.effect.Effect;
import org.red.minecraft.uw.core.skill.effect.EffectResult;
import org.red.minecraft.uw.core.skill.effect.EffectType;

import java.util.concurrent.CompletableFuture;

/**
 * 탐색할 대상 수({@link CTXType#TARGET_COUNT})를 더하는 수정자 기어.
 *
 * <p>TARGET_COUNT 는 {@code int.class} CTX 이므로 <b>덧셈</b>이다 (수정자 연산 규칙은
 * {@link SimpleModifierEffect} 클래스 주석 참고).
 *
 * <p>{@code CTXType.TARGET_COUNT} 의 기본값은 <b>0</b>(덧셈의 항등원)이다. 그래서 소비처는 보정 없이
 * 자기 기본값에 그대로 더하면 되고, 수정자 기어가 없으면 기어 YAML 값이 그대로 살아난다:
 * <pre>{@code
 * int finalCount = yamlCount + ctx.getCTX(CTXType.TARGET_COUNT, 0);
 * }</pre>
 *
 * <p>⛔ <b>소비처는 이 CTX 를 절대 개수로 읽으면 안 된다.</b> 기본값 0 이 항상 채워지므로
 * {@code getCTX(TARGET_COUNT, 1)} 같이 읽으면 <b>대상이 항상 0 이 되어 스킬이 조용히 실패</b>한다.
 * 컴파일로는 잡히지 않으니 새 소비처를 추가할 때 특히 주의할 것.
 *
 * <p>소비처: {@code TargetEffect}(YAML {@code count} + CTX),
 * {@code ThunderEffect.searchNearby}({@code BASE_TARGET_COUNT} + CTX).
 */
public class TargetCountIncreaseEffect implements Effect {

    private final int increase;

    public TargetCountIncreaseEffect(int increase) {
        this.increase = increase;
    }

    @Override
    public CompletableFuture<EffectResult> execute(SkillCTX ctx) {
        int targetCount = ctx.getCTX(CTXType.TARGET_COUNT, 0);

        // 클램프는 상한이 아니라 int 오버플로 방지다 (CountIncreaseEffect 와 동일).
        long sum = (long) targetCount + this.increase;
        ctx.setCTX(CTXType.TARGET_COUNT, (int) Math.clamp(sum, Integer.MIN_VALUE, Integer.MAX_VALUE));
        return CompletableFuture.completedFuture(EffectResult.SUCCESS);
    }

    @Override
    public EffectType[] getEffectTypes() {
        return new EffectType[]{EffectType.MODIFIER};
    }
}
