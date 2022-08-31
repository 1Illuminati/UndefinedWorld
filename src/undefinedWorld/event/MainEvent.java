package undefinedWorld.event;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import undefinedWorld.event.damage.DamageEvent;
import undefinedWorld.event.player.PlayerEvent;

public class MainEvent implements Listener {
	private final DamageEvent damageEvent = new DamageEvent();
	private final PlayerEvent playerEvent = new PlayerEvent();
	
	@EventHandler
    public void playerQuitEvent(PlayerQuitEvent event) {
    	this.playerEvent.newPlayerQuitEvent(event);
    }
    
    @EventHandler
    public void playerjoinEvent(PlayerJoinEvent event) {
    	this.playerEvent.newPlayerJoinEvent(event);
    }

    @EventHandler
    public void entityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        this.damageEvent.newEntityDamageByEntityEvent(event);
    }
   
}
