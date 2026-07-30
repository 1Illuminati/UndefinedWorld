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

    /**
     * 버프 고유 이름. (Process.md §2.6 버프 구조 개편 3)
     *
     * 타입으로 기능을 묶고 이름으로 구분한다. <b>같은 클래스여도 이름이 다르면 다른 버프</b>이며,
     * BuffManager 의 활성 버프 키가 (type, name) 이라 이름이 다르면 동시에 유지된다.
     * (스탯이 서로 다른 AttributeBuff 여러 개가 공존해야 하는 이유)
     * 중첩(StackableBuff) 도 <b>이름이 같아야</b> 성립한다.
     *
     * 계약: 인스턴스 수명 동안 <b>항상 같은 값</b>을 돌려줘야 한다.
     * 적용 시점과 종료 시점의 키가 달라지면 활성 맵에서 제거되지 않아 유령 버프가 된다.
     */
    String getName();

    /**
     * tick() 호출 주기. 단위는 <b>틱</b>이며, BuffManager에 넘기는 durationTicks와 같은 단위다.
     * 0 이하면 1틱으로 클램프된다. (BuffData.period())
     * 상태 마커 버프도 사망/로그아웃 자체 감지를 위해 주기를 갖는다.
     */
    int tickCount();

    void tick(A_Entity entity);

    /**
     * 적용 시 1회 실행. 여기서 건 효과는 onRemove에서 <b>정확히 되돌려야 한다</b>.
     * (AttributeBuff처럼 수치를 더하는 버프는 원복되지 않으면 스탯이 영구히 남는다)
     */
    default void onApply(A_Entity entity) {}

    /** 종료 시 1회 실행. onApply에서 건 효과를 되돌린다. 종료 사유 4가지 모두에서 호출된다. */
    default void onRemove(A_Entity entity, BuffRemoveReason reason) {}
}
