package org.red.minecraft.uw.core.item.consume;

import org.red.minecraft.dellarte.library.entity.A_Player;
import org.red.minecraft.uw.core.item.U_Item;

/**
 * 실질 컨슘 처리는 nexo에서 할거다
 */
public interface ConsumeItem extends U_Item {
    void consume(A_Player player);
}
