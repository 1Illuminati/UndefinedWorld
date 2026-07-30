package org.red.minecraft.uw.core.skill.effect.modifier;

import org.red.minecraft.uw.core.skill.CTXType;
import org.red.minecraft.uw.core.skill.target.Target;

/**
 * 타겟 탐색 방식({@link CTXType#SEARCH_TYPE})을 지정하는 수정자 기어. (ElementalEffect 와 동일한 패턴)
 *
 * <p>열거형이므로 <b>덮어쓴다.</b> 이 기어 이후의 타겟 기어들이 지정된 방식으로 탐색한다.
 * CTXType 에 기본값이 없으므로, 이 기어를 쓰지 않으면 소비처의 기본값(RANGE_CIRCLE)이 유지된다.
 *
 * <p>⚠️ {@code BOX}/{@code RANGE_BOX} 는 {@link CTXType#SEARCH_AREA}(BoundingBox 목록)가 필요한데
 * 현재 SEARCH_AREA 를 채우는 경로가 없다 — 소비처({@code TargetEffect}/{@code ThunderEffect})가
 * 박스 없는 {@code EntityTarget} 을 만들기 때문에 두 방식은 <b>항상 빈 결과</b>가 된다.
 * SEARCH_AREA 지정 방식이 확정되기 전까지는 {@code RANGE_CIRCLE}/{@code RANGE_SQUARE} 만 의미가 있다.
 *
 * <p>소비처: {@code TargetEffect}, {@code ThunderEffect.searchNearby}.
 */
public class SearchTypeEffect extends SimpleModifierEffect<Target.SearchType> {

    public SearchTypeEffect(Target.SearchType searchType) {
        super(CTXType.SEARCH_TYPE, searchType);
    }
}
