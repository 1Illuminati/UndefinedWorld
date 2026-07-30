package org.red.minecraft.uw.core.skill.cost;

import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.uw.core.exeception.CannotPayCostException;

/**
 * 스킬 비용 한 건.
 *
 * <p><b>자원을 가지지 않는 엔티티 처리 규칙 (구현체 공통):</b>
 * 몹 스킬도 같은 Gear/Cost 구조를 쓰기 때문에, 해당 자원 개념이 없는 엔티티에 대해서는
 * {@code hasCost}가 true(지불 가능)를 반환하고 {@code payCost}는 아무 것도 하지 않는다.
 * 즉 "자원이 없는 엔티티에게는 비용이 없다"는 뜻이며, 스킬을 막지 않는다.
 * <ul>
 *     <li>ManaCost / StaminaCost — 플레이어 전용 자원. 비플레이어는 무비용 통과.</li>
 *     <li>HealthCost — 리빙 엔티티 공통. 리빙이 아니면 지불 불가(false)로 막는다.</li>
 * </ul>
 * 이 비대칭(마나/스테미나는 통과, 체력은 차단)은 <b>의도된 동작</b>이다 —
 * 비리빙 엔티티는 체력 개념 자체가 없으므로 체력 비용 스킬을 쓸 수 없다 (§2.6 스킬 코어 8).
 *
 * <p><b>다중 비용 지불 규칙:</b> 같은 CostType의 비용은 기어별로 나눠 지불하지 않고
 * {@link #hasCostMultiple}/{@link #payMultiple}로 <b>한 번에 합산해</b> 검사·지불한다.
 * 기어별로 나눠 지불하면 뒤쪽 기어에서 자원이 모자랄 때 앞쪽 몫만 소모된 채 스킬이 끊긴다.
 */
public interface Cost<T> {
    CostType getType();

    T getValue();

    /**
     * 해당 객체가 코스트를 지불할 수 있는 상태인지 확인
     * @param entity 코스트를 지불할 엔티티
     * @return 가능하면 true 불가능하면 false
     */
    boolean hasCost(A_Entity entity);

    /**
     * 코스트를 지불 처리 함수
     * 만약 코스를 지불할 수 없을 경우 CannotPayCostException 반환한다
     * @param entity 코스트를 지불할 엔티티
     */
    void payCost(A_Entity entity) throws CannotPayCostException;

    boolean hasCost(A_Entity entity, T cost);

    void payCost(A_Entity entity, T cost) throws CannotPayCostException;

    /**
     * 다중 코스트 처리용 함수 해당 클래스를 포함하여 같은 타입의 코스트 클래스를 전부 파라미터로 준다
     * @param entity 코스트를 지불할 엔티티
     * @param costs 지불할 코스트들
     * @return 지불이 가능하면 true 불가능하면 false
     */
    boolean hasCostMultiple(A_Entity entity, Cost<T>[] costs) throws IllegalArgumentException;

    /**
     * 다중 코스트 처리용 함수 해당 클래스를 포함하여 같은 타입의 코스트 클래스를 전부 파라미터로 준다
     * 코스트를 지불하지 못할 경우 CannotPayCostException을 반환한다
     * 이때 해당 클래스로 CannotPayCostException을 만든다
     * @param entity 코스트를 지불할 엔티티
     * @param costs 지불할 코스트들
     */
    void payMultiple(A_Entity entity, Cost<T>[] costs) throws CannotPayCostException, IllegalArgumentException;
}
