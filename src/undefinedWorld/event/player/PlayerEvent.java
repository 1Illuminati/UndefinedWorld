package undefinedWorld.event.player;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import doublePlugin.entity.player.NewPlayer;
import undefinedWorld.entity.player.stat.Stats;

public class PlayerEvent {
	public void newPlayerJoinEvent(PlayerJoinEvent event) {
		Player minecraftPlayer = event.getPlayer();
		if(UndefinedPlayer.containsUndefinedPlayer(minecraftPlayer)) {
			UndefinedPlayer player = UndefinedPlayer.getUndefinedPlayer(minecraftPlayer);
			player.removeUndefinedPlayer();
		}
		
		UndefinedPlayer player = UndefinedPlayer.getUndefinedPlayer(minecraftPlayer);
		if(player.getIntegerValue(NewPlayer.PLAYER_JOIN_COUNT) <= 1) {
			player.setStat(Stats.STATPOINT, 3);
			player.setUndefinedLevel(1);
			player.setStatAttribute();
		}
		
		player.setHealthScaled(true);
		player.setHealthScale(20);
	}
	
	public void newPlayerQuitEvent(PlayerQuitEvent event) {
		UndefinedPlayer player = UndefinedPlayer.getUndefinedPlayer(event.getPlayer());
		player.removeUndefinedPlayer();
	}
}
