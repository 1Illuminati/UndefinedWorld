package org.red.minecraft.uw.core.module;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.red.minecraft.uw.core.item.U_Item;

/**
 * 아이템 데이터 조회 창구. (구조 결정 2.5 T22)
 * 각 아이템의 실질 데이터 구현(ex. GearItemMechanic)은 item 모듈이 담당하고,
 * core는 이 인터페이스를 통해서만 아이템을 가져온다.
 */
public interface IItemModule extends IModule {

    /**
     * 아이템 코드(namespacekey의 key 부분)로 U_Item 조회
     * @param itemCode 아이템 식별 코드
     * @return 없으면 null
     */
    @Nullable
    U_Item getItem(String itemCode);

    /**
     * ItemStack으로 U_Item 조회
     * @param itemStack 대상 아이템스택
     * @return U_Item이 아니면 null
     */
    @Nullable
    U_Item getItem(ItemStack itemStack);
}
