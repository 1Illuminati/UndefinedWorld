package org.red.minecraft.uw.core.combat.buff;

/**
 * 버프가 제거되는 사유. onRemove()로 전달되어 구현체가 상황별로 다르게 정리할 수 있게 한다.
 */
public enum BuffRemoveReason {
    EXPIRED,   // 1. 버프 시간 종료
    DEATH,     // 2. 대상 사망
    QUIT,      // 3. 로그아웃 (재접속 시 복원됨)
    FORCED     // 4. 그 외 강제 제거
}
