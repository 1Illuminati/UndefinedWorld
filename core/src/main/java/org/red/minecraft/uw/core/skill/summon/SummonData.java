package org.red.minecraft.uw.core.skill.summon;

import org.red.minecraft.dellarte.library.entity.A_Entity;

/**
 * 소환 파라미터. <b>값만 담는다 — 스탯 계산은 {@link SummonEntity} 담당이다.</b> (사용자 확정)
 *
 * <p>이전에는 이 레코드가 AttributeHolder 를 스텁으로 구현하고 있었는데,
 * 여기에는 <b>소환된 엔티티 참조가 없어</b> "미스틱몹 자체 Attribute"를 읽어올 대상이 없었다.
 * 그래서 AttributeHolder 는 실체를 들고 있는 SummonEntity 로 옮겼다.
 *
 * @param owner    시전자. 소환 시점의 Attribute 10% 가 소환수에게 더해진다 (SummonEntity 참조).
 * @param timeTick 소환 지속 시간(틱). <b>아직 쓰는 곳이 없다</b> — 수명 관리는 미확정이라 구현하지 않았다.
 * @param level    소환 레벨. <b>아직 쓰는 곳이 없다</b> — 레벨이 스탯에 어떻게 반영되는지는 확정되지 않았다.
 *                 (확정된 규칙은 "미스틱몹 자체 + 시전자 10%" 뿐이다. todo 레벨 반영 규칙 확정 필요)
 */
public record SummonData(A_Entity owner, int timeTick, int level) {
}
