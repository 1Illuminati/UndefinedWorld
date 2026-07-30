package org.red.minecraft.uw.core.item.gear;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.red.minecraft.uw.core.skill.gear.Gear;

public interface GearItem {
    Gear toGear();

    /**
     * 이 기어의 실제 아이템 1개를 만든다.
     * 스킬 수정 GUI가 스킬 아이템에 저장된 기어 코드를 다시 아이템으로 꺼내줄 때 쓴다.
     * (§2.6 확정: 제작 시 기어 소모, 수정 GUI에서 기어로 회수 가능)
     *
     * @return 아이템을 만들 수 없으면 null
     */
    @Nullable
    ItemStack createItemStack();
}
