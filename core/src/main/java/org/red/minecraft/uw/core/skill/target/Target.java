package org.red.minecraft.uw.core.skill.target;

import org.bukkit.Location;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public interface Target<T> {

    /**
     * 실질적으로 타겟을 가져올때 사용하는 함수
     * 필요없으면 null로 가능
     * @param predicate filter용 파라미터
     * @return 타겟 결과
     */
    T[] getTargets(@Nullable Predicate<T> predicate);

    /**
     * 중심점
     * @return 중심좌표
     */
    Location getLocation();

    /**
     * 타겟의 수
     * @return 타겟의 수
     */
    int targetCount();

    /**
     * 타겟을 감지하는 범위, 원형으로 특정 거리로 감지할 때 사용
     * 사용하지 않을경우 -1으로 반환
     * @return 감지하는 범위의 값
     */
    double targetSearchRange();

    /**
     * 특정 공간내에 타겟을 감지할때 사용
     * 박스 단위로 감지할때 사용
     * 중심점을 기준으로 바운딩 박스를 재생성
     * 사용하지 않을경우 비어있는 배열 반환
     * @return 감지하는 범위의 바운딩박스
     */
    BoundingBox[] targetSearchBox();

    SearchType getSearchType();

    enum SearchType {
        RANGE_CIRCLE,
        RANGE_SQUARE,
        BOX,
        RANGE_BOX,
    }
}
