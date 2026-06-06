package org.red.minecraft.uw.core.skill.cost;

import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.uw.core.exeception.CannotPayCostException;

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
