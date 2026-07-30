package org.red.minecraft.uw.core.skill.effect.modifier;

import org.red.minecraft.uw.core.combat.damage.DamageType;
import org.red.minecraft.uw.core.skill.CTXType;

/**
 * 스킬 전체의 데미지 유형을 지정하는 수정자 기어. (ElementalEffect와 동일한 패턴)
 *
 * <p>이 기어 이후의 노드들은 데미지 유형이 지정된 값으로 바뀐다.
 * 데미지를 주는 이펙트들은 각자 기본값을 갖고 있어, 이 기어가 없으면 기존 동작 그대로다.
 * (단 낙뢰처럼 유형이 이펙트 정체성인 경우는 고정 — 해당 이펙트 주석 참고)
 */
public class DamageTypeEffect extends SimpleModifierEffect<DamageType> {
    public DamageTypeEffect(DamageType type) {
        super(CTXType.DAMAGE_TYPE, type);
    }
}
