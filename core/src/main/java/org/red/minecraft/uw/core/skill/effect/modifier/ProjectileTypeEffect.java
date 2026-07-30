package org.red.minecraft.uw.core.skill.effect.modifier;

import org.red.minecraft.uw.core.skill.CTXType;
import org.red.minecraft.uw.core.skill.projectile.ProjectileType;

/**
 * 발사체 거동({@link CTXType#PROJECTILE_TYPE})을 지정하는 수정자 기어. (ElementalEffect 와 동일한 패턴)
 *
 * <p>열거형이므로 <b>덮어쓴다.</b> NORMAL(첫 적중 후 소멸) / PIERCE(관통) / GUIDED(유도).
 * CTXType 에 기본값이 없으므로, 이 기어를 쓰지 않으면 소비처의 기본값(NORMAL)이 유지된다.
 *
 * <p>소비처: {@code ProjectileEffect}.
 * <p>{@code SwordAuraEffect} 는 이 CTX 를 따르지 않는다 — 검기는 관통이 정체성이라 PIERCE 고정이다.
 */
public class ProjectileTypeEffect extends SimpleModifierEffect<ProjectileType> {

    public ProjectileTypeEffect(ProjectileType projectileType) {
        super(CTXType.PROJECTILE_TYPE, projectileType);
    }
}
