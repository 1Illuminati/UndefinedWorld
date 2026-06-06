package org.red.minecraft.uw.core.item;

import org.bukkit.inventory.ItemStack;

public interface U_Item {
    String getDisplay();
    String getDescription();
    String getItemCode();
    U_ItemType getType();
    U_ItemGrade getGrade();
    ItemStack getItemStack();
}
