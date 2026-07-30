package org.red.minecraft.uw.core.skill.effect.modifier;

import org.red.minecraft.uw.core.skill.CTXType;

/**
 * 타겟 탐색 반경({@link CTXType#SEARCH_RANGE})을 지정하는 수정자 기어. (ElementalEffect 와 동일한 패턴)
 *
 * <p><b>타입 규칙({@code double.class} → 곱셈)의 확정된 예외다 — 이 CTX 만 덮어쓰기다.</b> (사용자 확정)
 * 탐색 반경 <b>배율</b>은 {@link CTXType#RANGE}({@code range_multiply}) 하나로 통일하기로 했고,
 * SEARCH_RANGE 는 반경 <b>절대값</b>을 직접 지정하는 용도로 남긴다. 두 배율 CTX 가 같은 일을 하는 중복을 없앤 것이다.
 *
 * <p>그래서 {@code CTXType.SEARCH_RANGE} 에는 <b>기본값을 두지 않는다.</b> 기본값이 있으면 {@code SkillCTX}
 * 생성자가 항상 채워, 소비처의 {@code getCTX(SEARCH_RANGE, 8.0)} 폴백이 죽고 탐색 반경이 조용히 붕괴한다.
 * ⛔ 여기에 기본값을 넣지 말 것.
 *
 * <p>소비처: {@code ThunderEffect.searchNearby}(기본 8.0).
 */
public class SearchRangeEffect extends SimpleModifierEffect<Double> {

    /**
     * @throws IllegalArgumentException 반경이 유한하지 않거나 음수일 때.
     *         무한/NaN 반경은 탐색 박스를 사실상 월드 전체로 만들어 서버가 멈추고,
     *         음수 반경은 탐색 결과가 항상 0이라 스킬이 조용히 실패한다.
     *         기어 로드 시점에서 막는 편이 추적하기 쉽다.
     */
    public SearchRangeEffect(double range) {
        super(CTXType.SEARCH_RANGE, validate(range));
    }

    private static double validate(double range) {
        if (!Double.isFinite(range) || range < 0)
            throw new IllegalArgumentException("SearchRangeEffect Error: invalid range (" + range + ")");

        return range;
    }
}
