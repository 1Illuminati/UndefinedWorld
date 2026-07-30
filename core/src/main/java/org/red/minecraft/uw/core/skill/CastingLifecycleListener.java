package org.red.minecraft.uw.core.skill;

import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.red.minecraft.dellarte.library.event.listener.A_Listener;

/**
 * 스킬 도메인이 플레이어별로 들고 있는 상태를 Bukkit 생명주기 이벤트와 연결해 정리한다.
 * (BuffLifecycleListener 와 동일한 역할 구조 — 매니저의 정리 훅만 호출한다)
 *
 * <ul>
 *     <li>캐스팅: 정리하지 않으면 CastingManager 의 casting 엔트리가 남은 캐스팅 시간 동안 유지되어,
 *         퇴장 후 재접속하면 그 시간만큼 이동이 막힌다 (CastingMoveListener 가 isCasting 을 보므로).</li>
 *     <li>디버그 모드: 정리하지 않으면 SkillDebugManager 의 정적 맵에 UUID 가 계속 쌓인다.
 *         사망은 정리 대상이 아니다 — 죽었다고 디버그가 꺼질 이유는 없다.</li>
 * </ul>
 */
public class CastingLifecycleListener extends A_Listener {

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        CastingManager.cancelCast(event.getPlayer().getUniqueId());
        SkillDebugManager.clear(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        CastingManager.cancelCast(event.getEntity().getUniqueId());
    }
}
