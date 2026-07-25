package org.red.minecraft.uw.core.skill;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.red.minecraft.dellarte.library.event.listener.A_Listener;

/**
 * 캐스팅 중 이동 불가 제약. (확정 규칙: 구속버프 방식이 아닌 MoveEvent 취소)
 * 시선 회전은 허용하고 위치 이동만 차단한다.
 */
public class CastingMoveListener extends A_Listener {

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!CastingManager.isCasting(event.getPlayer().getUniqueId())) return;

        // 위치 변화 없는 순수 시선 회전은 허용
        if (event.getFrom().getX() == event.getTo().getX()
                && event.getFrom().getY() == event.getTo().getY()
                && event.getFrom().getZ() == event.getTo().getZ()) return;

        event.setCancelled(true);
    }
}
