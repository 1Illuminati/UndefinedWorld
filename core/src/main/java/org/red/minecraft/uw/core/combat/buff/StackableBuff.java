package org.red.minecraft.uw.core.combat.buff;

/**
 * 중첩 가능한 버프만 구현하는 인터페이스. (Process.md §2.6 버프 구조 개편 2·4)
 *
 * 중첩 카운트는 별도 필드가 아니라 {@link BuffContext#level()} 로 표현한다.
 * 이 인터페이스를 구현하지 않는 일반 버프는 재적용 시 그대로 <b>덮어쓰기</b>(지속시간·레벨 전부 교체)되고
 * 최대 레벨 제한도 없다.
 *
 * 중첩은 <b>타입과 이름이 모두 같을 때</b>만 성립한다. (Buff#getName 참조)
 * 누적은 매니저가 한다 — 호출자는 "이번에 추가할 양"만 level 로 넘긴다.
 * (BuffManager.resolveStack: 새 레벨 = min(maxStack, 기존레벨 + 요청레벨))
 *
 * 구현 주의: 재적용 시 레벨만 바뀐 컨텍스트로 인스턴스를 다시 만들므로
 * {@link Buff#getName()} 이 level 에 의존하면 안 된다. (키가 바뀌어 중첩이 성립하지 않는다)
 */
public interface StackableBuff extends Buff {

    /**
     * 중첩 상한. BuffManager 가 적용 시 누적 레벨을 이 값 이하로 자른다.
     * 1 미만이면 1로 클램프된다. (BuffManager.resolveStack)
     */
    int maxStack();
}
