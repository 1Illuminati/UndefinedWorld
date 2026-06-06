package org.red.minecraft.uw.core.skill.projectile;

public enum ProjectilesShape {
    SINGLE,      // 단일 (정면 1발)
    SPREAD,      // 부채꼴 (spread 각도로 count발)
    CIRCLE,      // 원형 (360도 방사)
    RAIN,        // 낙하 (하늘에서 아래로)
    SELF,        // 자신 대상
    POINT,       // 지정 위치
}
