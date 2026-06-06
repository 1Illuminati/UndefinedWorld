package org.red.minecraft.uw.core.skill.projectile;

import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.red.minecraft.dellarte.library.entity.A_Entity;

/**
 * 발사체를 위한 데이터용 레코드
 * 각각 개별의 발사체들의 각각의 데이터를 담당한다
 * @param shooter 쏜 놈
 * @param startLoc 시작 장소
 * @param direction 방향
 * @param speed 속도
 * @param range 거리
 * @param targetRender 발사체가 타겟에 닿았는지 감지하는 범위
 */
public record ProjectileData(A_Entity shooter, Location startLoc, Vector direction, double speed, double range, double targetRender, ProjectileType type) {
}
