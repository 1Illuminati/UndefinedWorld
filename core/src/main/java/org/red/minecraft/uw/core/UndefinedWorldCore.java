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
    /**
     * 지연 생성한다. 클래스 초기화 시점에 생성하면 UndefinedWorldCorePlugin.instance 가 아직
     * null 일 수 있고, 그 상태로 만들어진 BuffManager 는 스케줄러 등록에서 실패한다.
     * shutdown() 에서 비워지므로 플러그인 재활성화 시 살아있는 plugin 으로 다시 만들어진다.
     */
    private static BuffManager buffManager;
    private static IMobModule mobModule;
    private static IItemModule itemModule;

    public static void registerModule(IModule module) {
        if (module instanceof IMobModule mModule) {
            mobModule = mModule;
            UndefinedWorldCorePlugin.sendLog("MobModule registered: " + mModule.getClass().getSimpleName());
        } else if (module instanceof IItemModule iModule) {
            itemModule = iModule;
            UndefinedWorldCorePlugin.sendLog("ItemModule registered: " + iModule.getClass().getSimpleName());
        } else {
            // 어느 모듈 타입에도 속하지 않으면 조용히 무실행되므로 원인 추적용 로그를 남긴다
            UndefinedWorldCorePlugin.sendLog("Unknown module type, ignored: " + module.getClass().getName());
        }
    }

    /**
     * 모듈 등록 해제. 각 모듈 플러그인이 비활성화될 때 스스로 호출한다.
     *
     * 해제하지 않으면 mob/item 만 따로 비활성화했을 때 죽은 모듈 참조가 core 에 남아,
     * 이미 내려간 외부 플러그인(MythicBukkit/Nexo)을 호출하게 된다.
     * 등록된 것과 다른 인스턴스가 넘어오면 무시한다 (재활성화로 새 모듈이 등록된 뒤
     * 예전 인스턴스의 뒤늦은 해제가 살아있는 등록을 지우는 것을 막는다).
     */
    public static void unregisterModule(IModule module) {
        if (module == mobModule) {
            mobModule = null;
            UndefinedWorldCorePlugin.sendLog("MobModule unregistered: " + module.getClass().getSimpleName());
        } else if (module == itemModule) {
            itemModule = null;
            UndefinedWorldCorePlugin.sendLog("ItemModule unregistered: " + module.getClass().getSimpleName());
        }
    }

    /**
     * 플러그인 비활성화 시 정리. 활성 버프 태스크를 종료하고 모듈 등록을 해제한다.
     * (BuffManager 를 비워 다음 활성화 때 살아있는 plugin 인스턴스로 다시 생성되게 한다)
     */
    public static void shutdown() {
        if (buffManager != null) {
            buffManager.shutdown();
            buffManager = null;
        }
        mobModule = null;
        itemModule = null;
    }

    /**
     * 미스틱몹인지 감지하는 함수 스킬도 true로 반환
     * @param entity 판단할 엔티티
     * @return 결과
     */
    public static boolean isMythicMob(A_Entity entity) {
        if (mobModule == null) return false; // mob 모듈 미등록 상태 (getItem 과 동일한 방어 정책)
        return mobModule.isMythicMob(entity);
    }

    public static boolean isMythicMob(Entity entity) {
        if (mobModule == null) return false;
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
        if (mobModule == null) return false;
        return mobModule.isDamageableMob(entity);
    }

    public static boolean isDamageableMob(Entity entity) {
        if (mobModule == null) return false;
        return mobModule.isDamageableMob(entity);
    }

    public static BuffManager getBuffManager() {
        if (buffManager == null) buffManager = new BuffManager(UndefinedWorldCorePlugin.instance);
        return buffManager;
    }

    /**
     * item 모듈이 등록되어 아이템 조회가 가능한 상태인지 확인한다.
     *
     * item 모듈은 Nexo 메커니즘 등록 이벤트 시점에 등록되므로 그보다 이른 시점에는 getItem 이 항상 null 이다.
     * 이때 "UW 아이템이 아님"과 "아직 조회 불가"를 구분하지 못하면 장비 재계산이 모든 장비를 미인식으로
     * 처리해 스탯을 0으로 밀어버린다. 재계산 전에 이 값을 먼저 확인해야 한다.
     */
    public static boolean hasItemModule() {
        return itemModule != null;
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

    /**
     * 최종 계산에 사용할 AttributeManager 를 mob 모듈에서 받아온다.
     * mob 모듈이 없으면 스탯이 전부 0 으로 조용히 계산되어 원인 추적이 불가능해지므로
     * 기본값 폴백 대신 명시적으로 실패시킨다.
     */
    public static AttributeManager getAttributeManager(A_Entity entity) {
        if (mobModule == null)
            throw new IllegalStateException("MobModule 미등록 상태에서 getAttributeManager 호출 (UndefinedWorldMob 플러그인 로드 여부 확인 필요)");

        AttributeManager result = mobModule.getAttributeHolder(entity);
        UndefinedWorldCorePlugin.sendLog(result.getClass().getSimpleName());
        return result;
    }
}
