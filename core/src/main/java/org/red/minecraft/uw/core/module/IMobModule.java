package org.red.minecraft.uw.core.module;

import org.bukkit.entity.Entity;
import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.uw.core.attribute.AttributeHolder;

public interface IMobModule extends IModule {
    /**
     * 미스틱몹인지 감지하는 함수 스킬도 true로 반환
     * @param entity 판단할 엔티티
     * @return 결과
     */
    boolean isMythicMob(A_Entity entity);
    boolean isMythicMob(Entity entity);

    /**
     * 해당 몬스터가 공격이 가능한몹인지 판단하는 함수
     * 스킬 용으로 소환되는 아머스탠드들 피하는 목적
     * 리빙 엔티티만 true로 리턴
     * 현재는 리빙엔티티만 감지하지만 추후에 뭐가 추가될지 몰라서 만들어둠
     * @param entity 판단할 엔티티
     * @return 결과
     */
    boolean isDamageableMob(A_Entity entity);
    boolean isDamageableMob(Entity entity);

    AttributeHolder getAttributeHolder(A_Entity entity);
}
