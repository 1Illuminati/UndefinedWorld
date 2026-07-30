package org.red.minecraft.uw.core.skill.effect.modifier;

import org.red.minecraft.uw.core.skill.CTXType;
import org.red.minecraft.uw.core.skill.projectile.ProjectilesShape;

/**
 * 발사 형태({@link CTXType#PROJECTILE_SHAPE})를 지정하는 수정자 기어. (ElementalEffect 와 동일한 패턴)
 *
 * <p>열거형이므로 <b>덮어쓴다.</b> SINGLE(정면 1발) / SPREAD(부채꼴) / CIRCLE(360도 방사).
 * 발사 수는 {@link CTXType#COUNT} 를 따른다.
 * CTXType 에 기본값이 없으므로, 이 기어를 쓰지 않으면 소비처의 기본값(SINGLE)이 유지된다.
 *
 * <p>⚠️ {@code RAIN}/{@code SELF}/{@code POINT} 는 위치 지정 방식이 미확정이라
 * {@code ProjectileEffect.resolveDirections} 가 빈 목록을 내고 <b>이펙트가 FAIL 로 끝난다.</b>
 * (로그는 남는다) 확정 전까지 이 셋은 쓰지 말 것.
 *
 * <p>소비처: {@code ProjectileEffect}.
 */
public class ProjectileShapeEffect extends SimpleModifierEffect<ProjectilesShape> {

    public ProjectileShapeEffect(ProjectilesShape projectileShape) {
        super(CTXType.PROJECTILE_SHAPE, projectileShape);
    }
}
