package org.red.minecraft.uw.core.combat.buff;

import org.red.minecraft.dellarte.library.entity.A_Entity;

/**
 * 실질적인 버프 동작을 정의하는 인터페이스.
 * 구현체는 가급적 상태가 없거나(stateless) 인스턴스 단위 상태만 갖도록 작성한다.
 */
public interface Buff {

    /** 이 버프 인스턴스가 생성될 때의 컨텍스트(level/caster/data). 복원 스냅샷에 사용된다. */
    BuffContext context();

    BuffType type();
    int tickCount();
    void tick(A_Entity entity);

    default void onApply(A_Entity entity) {}
    default void onRemove(A_Entity entity, BuffRemoveReason reason) {}
}
