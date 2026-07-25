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

    @Override
    public @Nullable U_Item getItem(String itemCode) {
        return factory.getMechanic(itemCode);
    }

    @Override
    public @Nullable U_Item getItem(ItemStack itemStack) {
        return factory.getMechanic(itemStack);
    }
}
