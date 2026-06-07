package org.red.minecraft.uw.core;

import org.bukkit.entity.Entity;
import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.uw.core.attribute.AttributeHolder;
import org.red.minecraft.uw.core.module.IItemModule;
import org.red.minecraft.uw.core.module.IMobModule;
import org.red.minecraft.uw.core.module.IModule;

public final class UndefinedWorldCore {
    private static IMobModule mobModule;
    private static IItemModule itemModule;
    public static void registerModule(IModule module) {
        if (module instanceof IMobModule mModule) mobModule = mModule;
        else if (module instanceof IItemModule iModule) itemModule = iModule;
    }

    /**
     * 미스틱몹인지 감지하는 함수 스킬도 true로 반환
     * @param entity 판단할 엔티티
     * @return 결과
     */
    public static boolean isMythicMob(A_Entity entity) {
        return mobModule.isMythicMob(entity);
    }

    public static boolean isMythicMob(Entity entity) {
        return mobModule.isMythicMob(entity);
    }

    /**
     * 해당 몬스터가 공격이 가능한몹인지 판단하는 함수
     * 스킬 용으로 소환되는 아머스탠드들 피하는 목적
     * 리빙 엔티티만 true로 리턴
     * 현재는 리빙엔티티만 감지하지만 추후에 뭐가 추가될지 몰라서 만들어둠
     * @param entity 판단할 엔티티
     * @return 결과
     */
    public static boolean isDamageableMob(A_Entity entity) {
        return mobModule.isDamageableMob(entity);
    }

    public static boolean isDamageableMob(Entity entity) {
        return mobModule.isDamageableMob(entity);
    }

    public static AttributeHolder getAttributeHolder(A_Entity entity) {
        return mobModule.getAttributeHolder(entity);
    }
}
