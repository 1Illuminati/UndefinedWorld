package org.red.minecraft.uw.core.combat.buff;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Function;

/**
 * 각 버프 종류. 팩토리가 BuffContext를 받아 Buff 인스턴스를 만든다.
 * (재접속 복원 시 보류해 둔 context로부터 Buff를 다시 생성)
 */
public enum BuffType implements ConfigurationSerializable {
    ATTRIBUTE_BUFF(AttributeBuff::new),
    REGENERATION(RegenerationBuff::new),
    POISON(PoisonBuff::new),
    GLOWING(GlowingBuff::new),
    /** 감전 (번개속성 디버프) — 효과는 데미지 파이프라인에서 처리, ShockDebuff 참조 */
    SHOCK(ShockDebuff::new),
    /** 화상 (화염속성 디버프) — 주기 도트, BurnDebuff 참조 */
    BURN(BurnDebuff::new),
    /** 침묵 (수속성 디버프) — 스킬 사용 차단, SilenceDebuff 참조 */
    SILENCE(SilenceDebuff::new),
    /** 파쇄 (땅속성 디버프) — 중첩형 땅속성 피해 증가, ShatterDebuff 참조 */
    SHATTER(ShatterDebuff::new),
    /** 무적 — 데미지 무효 + 캐스팅 취소 예외, InvincibleBuff 참조 */
    INVINCIBLE(InvincibleBuff::new);

    private final Function<BuffContext, Buff> factory;

    BuffType(Function<BuffContext, Buff> factory) {
        this.factory = factory;
    }

    /** 주어진 컨텍스트로 새 Buff 인스턴스 생성. */
    public Buff create(BuffContext ctx) {
        return factory.apply(ctx);
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        return Map.of("name", this.name());
    }

    public static BuffType deserialize(Map<String, Object> map) {
        return BuffType.valueOf((String) map.get("name"));
    }
}
