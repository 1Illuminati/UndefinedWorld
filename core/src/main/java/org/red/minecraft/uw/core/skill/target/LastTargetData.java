package org.red.minecraft.uw.core.skill.target;

import org.bukkit.Location;
import org.red.minecraft.dellarte.library.entity.A_Entity;

/**
 * 스킬이 실제로 적중한 대상 정보를 담는 레코드.
 * ProjectileController 의 onHit 콜백, 또는 이펙트가 직접 타겟을 찾은 뒤
 * 다음 노드에 넘길 정보로 사용한다.
 *
 * @param entities    적중된 엔티티 배열
 * @param hitLocation 적중 위치 (발사체 기준이면 충돌 지점, 직접 타겟이면 대상 위치)
 */
public record LastTargetData(A_Entity[] entities, Location hitLocation) {

    /** 단일 대상 적중 편의 생성자 */
    public static LastTargetData of(A_Entity entity, Location hitLocation) {
        return new LastTargetData(new A_Entity[]{entity}, hitLocation);
    }

    /** 엔티티가 없는 위치 기반 적중 (장판, 폭발 등) */
    public static LastTargetData ofLocation(Location hitLocation) {
        return new LastTargetData(new A_Entity[0], hitLocation);
    }
}
