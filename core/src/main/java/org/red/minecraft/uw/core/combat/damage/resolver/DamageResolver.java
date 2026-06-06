package org.red.minecraft.uw.core.combat.damage.resolver;

import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.uw.core.combat.damage.DamageType;

public abstract class DamageResolver {
    private final A_Entity entity;
    public DamageResolver(A_Entity entity) {
        this.entity = entity;
    }

    public A_Entity getEntity() {
        return this.entity;
    }

    public abstract DamageType getType();

    /**
     * 플레이어가 해당 데미지로 공격받았을때 방어력 계산을 하고 최종적인 데미지를 반환하는 함수
     * @param originDamage 받은 공격의 데미지
     * @return 방어력 연산이 끝난 후 최종적으로 플레이어가 받는 데미지
     */
    public abstract double resolveDefDamage(double originDamage);

    /**
     * 플레이어가 해당 데미지로 공격할때 추가적인 계산이 들어가는 함수
     * 기본 데미지취급이 공격력에 몇% 로 취급하기 때문에 베이스데미지는 %로 들어간다
     * @param originDamage 해당 공격에 베이스 공격력 (%)
     * @return 공격력 연산이 끝난 후 최종적으로 플레이어가 주는 데미지
     */
    public abstract double resolveAtkDamage(double originDamage);
}
