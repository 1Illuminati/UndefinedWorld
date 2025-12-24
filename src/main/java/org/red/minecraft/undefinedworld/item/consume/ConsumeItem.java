package org.red.minecraft.undefinedworld.item.consume;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.red.minecraft.undefinedworld.item.U_Item;

/**
 * 특수한 조건을 가진 아이템은 귀찮으니 소스코드에서 제작 -> canUse 오버라이드
 * canUse를 사용하여 조건 확인하고 사용가능하게 만듬
 */
public interface ConsumeItem extends U_Item {

    boolean canUse(Player player, ItemStack item);

    int getConsumeCount();

    /**
     * boolean은 사용 성공 여부를 반환
     */
    boolean use(Player player, ItemStack item);
}
