package org.red.minecraft.uw.core;

import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.uw.core.attribute.AttributeManager;
import org.red.minecraft.uw.core.combat.buff.BuffManager;
import org.red.minecraft.uw.core.item.U_Item;
import org.red.minecraft.uw.core.module.IItemModule;
import org.red.minecraft.uw.core.module.IMobModule;
import org.red.minecraft.uw.core.module.IModule;

public final class UndefinedWorldCore {
    private static final BuffManager buffManager = new BuffManager(UndefinedWorldCorePlugin.instance);
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

    public static BuffManager getBuffManager() {
        return buffManager;
    }

    /**
     * 아이템 코드로 U_Item 조회 (item 모듈 위임)
     * @param itemCode 아이템 식별 코드
     * @return 없으면 null
     */
    @Nullable
    public static U_Item getItem(String itemCode) {
        if (itemModule == null) return null; // item 모듈은 Nexo 이벤트 시점에 등록되므로 그 전 호출 대비
        return itemModule.getItem(itemCode);
    }

    /**
     * ItemStack으로 U_Item 조회 (item 모듈 위임)
     * @param itemStack 대상 아이템스택
     * @return U_Item이 아니면 null
     */
    @Nullable
    public static U_Item getItem(ItemStack itemStack) {
        if (itemModule == null) return null; // item 모듈은 Nexo 이벤트 시점에 등록되므로 그 전 호출 대비
        return itemModule.getItem(itemStack);
    }

    public static AttributeManager getAttributeManager(A_Entity entity) {
        AttributeManager result = mobModule.getAttributeHolder(entity);
        UndefinedWorldCorePlugin.sendLog(result.getClass().getSimpleName());
        return result;
    }
}
