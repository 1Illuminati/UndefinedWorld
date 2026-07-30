package org.red.minecraft.uw.core.skill;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityMountEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.red.minecraft.dellarte.library.CommediaDellarte;
import org.red.minecraft.dellarte.library.event.listener.A_Listener;

/**
 * 캐스팅 중 이동 관련 제약. (확정 규칙 Process.md 2.5 / 2.6)
 *
 * <ul>
 *     <li>이동: 차단 (구속버프 방식이 아닌 MoveEvent 취소). 시선 회전은 허용.</li>
 *     <li>텔레포트: <b>차단하지 않고 캐스팅만 취소</b> (2.6 스킬 코어 4)</li>
 *     <li>탈것 탑승: 캐스팅 취소 (2.6 스킬 코어 3). 탑승 중 스킬 사용 차단은 SkillEngine.runSkill 진입부.</li>
 * </ul>
 *
 * <p>탈것 탑승 중에는 PlayerMoveEvent가 발생하지 않아 이동 차단이 우회된다.
 * 그래서 "탑승 중 스킬 사용 금지 + 캐스팅 중 탑승 시 취소" 두 규칙으로 막는다.
 */
public class CastingMoveListener extends A_Listener {

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        // PlayerTeleportEvent는 PlayerMoveEvent의 하위 타입이다. 현재 API에서는 자체 HandlerList를 갖고 있어
        // 이 핸들러로 들어오지 않지만, 들어오더라도 "차단"이 아니라 "취소"가 되도록 명시적으로 넘긴다.
        if (event instanceof PlayerTeleportEvent) return;

        // 모든 이동 이벤트를 타므로 가장 좁은 조건(캐스팅 중인 플레이어)을 먼저 걸러낸다
        if (!CastingManager.isCasting(event.getPlayer().getUniqueId())) return;

        // 위치 변화 없는 순수 시선 회전은 허용 (월드 변경도 위치 변화로 판정된다)
        if (!event.hasChangedPosition()) return;

        event.setCancelled(true);
    }

    /**
     * 캐스팅 중 텔레포트: 텔레포트는 통과시키고 캐스팅만 끊는다.
     * <p>다른 플러그인이 취소한 텔레포트는 실제로 일어나지 않으므로 캐스팅도 끊지 않는다
     * → MONITOR + ignoreCancelled 로 "확정된 텔레포트"만 본다.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (!CastingManager.isCasting(player.getUniqueId())) return;

        CastingManager.onTeleported(CommediaDellarte.getAPlayer(player));
    }

    /**
     * 캐스팅 중 탈것 탑승: 캐스팅 취소.
     * <p>VehicleEnterEvent가 아니라 EntityMountEvent를 쓴다 — 보트/말 같은 Vehicle 뿐 아니라
     * 다른 엔티티에 올라타는 경우(강제 addPassenger 포함)까지 전부 포함되기 때문이다.
     * <p>취소된 탑승은 실제로 일어나지 않으므로 캐스팅도 끊지 않는다.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMount(EntityMountEvent event) {
        if (!CastingManager.isCasting(event.getEntity().getUniqueId())) return;

        CastingManager.onMounted(CommediaDellarte.getAEntity(event.getEntity()));
    }
}
