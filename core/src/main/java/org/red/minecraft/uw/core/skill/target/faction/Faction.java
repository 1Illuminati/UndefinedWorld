package org.red.minecraft.uw.core.skill.target.faction;

import org.bukkit.entity.Entity;
import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.uw.core.UndefinedWorldCore;

import java.util.function.Predicate;

/**
 * todo 미스틱몹 시스템과 연계한 개발 필요
 * 중요한건 고정적인 관계가 아닌 상황에 따라 변하는 관계를 어떻게 설정할것인지
 * + 특정 인물들하고만의 관계는 어떻게 설명할것인지
 */
public class Faction {

    /**
     * @param caster    스킬 시전자 (A_Entity)
     * @param factionType 대상 범위 유형
     * @return 해당 faction 조건을 만족하는 엔티티인지 판별하는 Predicate
     */
    public static Predicate<Entity> predicate(A_Entity caster, FactionType factionType) {
        return switch (factionType) {
            case SELF -> entity -> entity.getUniqueId().equals(caster.getUniqueId());

            case ENEMY -> entity ->
                    !entity.getUniqueId().equals(caster.getUniqueId())
                    && UndefinedWorldCore.isDamageableMob(entity);

            // TODO: 파티 시스템 구현 이후 파티원 포함 조건으로 교체
            case PARTY -> entity -> entity.getUniqueId().equals(caster.getUniqueId());

            // TODO: 관계(우호/중립) 시스템 구현 이후 실제 관계 조건으로 교체 (임시: PARTY와 동일하게 자신만)
            case FRIENDLY, NEUTRAL -> entity -> entity.getUniqueId().equals(caster.getUniqueId());

            // TODO: NONE(알 수 없음)의 대상 판정 정의 필요 (임시: 아무도 매칭하지 않음)
            case NONE -> entity -> false;
        };
    }
}
