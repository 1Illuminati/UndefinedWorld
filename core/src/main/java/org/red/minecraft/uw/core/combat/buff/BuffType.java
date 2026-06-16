package org.red.minecraft.uw.core.combat.buff;

import java.util.function.Function;

/**
 * 각 버프 종류. 팩토리가 BuffContext를 받아 Buff 인스턴스를 만든다.
 * (재접속 복원 시 보류해 둔 context로부터 Buff를 다시 생성)
 */
public enum BuffType {
    PHYSICAL_DAMAGE_BUFF(PhysicalDamageBuff::new),
    REGENERATION(RegenerationBuff::new),
    POISON(PoisonBuff::new),
    GLOWING(GlowingBuff::new);

    private final Function<BuffContext, Buff> factory;

    BuffType(Function<BuffContext, Buff> factory) {
        this.factory = factory;
    }

    /** 주어진 컨텍스트로 새 Buff 인스턴스 생성. */
    public Buff create(BuffContext ctx) {
        return factory.apply(ctx);
    }
}
