package org.red.minecraft.uw.item;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.red.minecraft.uw.core.item.U_Item;
import org.red.minecraft.uw.core.module.IItemModule;
import org.red.minecraft.uw.item.mechanic.factory.U_ItemMechanicFactory;

/**
 * core의 IItemModule 구현체. Nexo 메커니즘 팩토리를 통해 아이템 데이터를 조회한다.
 * 팩토리 등록 시점(NexoMechanicsRegisteredEvent)에 함께 core에 등록된다.
 */
public class ItemModule implements IItemModule {

    private final U_ItemMechanicFactory factory;

    public ItemModule(U_ItemMechanicFactory factory) {
        this.factory = factory;
    }

    // core 전체가 이 창구로 아이템을 조회하므로 빈 입력은 여기서 걸러 호출부 방어 부담을 줄인다
    @Override
    public @Nullable U_Item getItem(String itemCode) {
        if (itemCode == null || itemCode.isEmpty()) return null;
        return factory.getMechanic(itemCode);
    }

    @Override
    public @Nullable U_Item getItem(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) return null;
        return factory.getMechanic(itemStack);
    }
}
