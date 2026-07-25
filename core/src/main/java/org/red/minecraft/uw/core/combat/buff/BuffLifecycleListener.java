package org.red.minecraft.uw.core.combat.buff;

import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.red.minecraft.dellarte.library.CommediaDellarte;
import org.red.minecraft.dellarte.library.event.listener.A_Listener;
import org.red.minecraft.uw.core.UndefinedWorldCore;

/**
 * 버프 종료/복원 흐름을 Bukkit 이벤트와 연결하는 리스너.
 * BuffManager 주석의 설계대로 매니저의 onDeath / onQuit / onJoin 훅만 호출한다.
 * (시간 종료는 BuffData 내부 타이머가 자체 처리)
 */
public class BuffLifecycleListener extends A_Listener {

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        UndefinedWorldCore.getBuffManager().onDeath(CommediaDellarte.getAEntity(event.getEntity()));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UndefinedWorldCore.getBuffManager().onQuit(CommediaDellarte.getAPlayer(event.getPlayer()));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        UndefinedWorldCore.getBuffManager().onJoin(CommediaDellarte.getAPlayer(event.getPlayer()));
    }
}
