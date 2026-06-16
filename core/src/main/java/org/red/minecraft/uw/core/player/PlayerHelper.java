package org.red.minecraft.uw.core.player;

import org.red.minecraft.dellarte.library.entity.A_Player;
import org.red.minecraft.uw.core.attribute.AttributeManager;

/**
 * 플레이어용 함수 모아두는 클래스
 */
public final class PlayerHelper extends AttributeManager {
    public PlayerHelper(A_Player player) {
        super(player);
    }

    public A_Player getPlayer() {
        return this.getEntity();
    }

    @Override
    public A_Player getEntity() {
        return (A_Player) super.getEntity();
    }
}
