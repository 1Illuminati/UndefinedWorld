package org.red.minecraft.uw.core.skill.target.faction;

public enum FactionType {
    PARTY, // 파티원 - 정확히 파티 내부 인원들만 취급
    FRIENDLY, // 아군 - 우호적 관계의 인원들 서로 공격하지 않는 상태일때
    NEUTRAL, //중립 - 아직까지는 우호적관계이나 특정조건시 관계변경
    ENEMY, // 적 - 서로 공격하는 관계
    SELF, // 본인 자신
    NONE, // 알 수 없음
}
