package org.red.minecraft.uw.core.skill.effect;

import org.red.minecraft.uw.core.skill.SkillCTX;
import org.red.minecraft.uw.core.util.HasID;

import java.util.concurrent.CompletableFuture;

public interface Effect extends HasID {
    /**
     * 실질 스킬 효과가 적용되는 함수
     * 결과에 따라 EffectResult를 반환하며
     * Success를 반환해야 다음 노드로 넘어간다
     * CompletableFuture를 사용해 특정 조건 이후에 넘어가는 노드들도 구현가능
     * @param ctx 스킬 ctx
     * @return CompletableFuture
     */
    CompletableFuture<EffectResult> execute(SkillCTX ctx);
}
