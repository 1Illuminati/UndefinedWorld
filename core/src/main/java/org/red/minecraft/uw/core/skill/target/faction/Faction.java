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
        };
    }
}
