package undefinedWorld.entity.player.invenrtory;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import doublePlugin.inventory.InventoryManager;
import undefinedWorld.entity.player.invenrtory.equipment.EquipmentInv;
import undefinedWorld.entity.player.invenrtory.stat.StatInv;

public abstract class AbstractPlayerInvEvent extends InventoryManager {
	@Override
	public void click(InventoryClickEvent event) {
		ItemStack item = event.getCurrentItem();
		UndefinedPlayer player = UndefinedPlayer.getUndefinedPlayer((Player) event.getWhoClicked());
		if(item != null) {
			boolean check = false;
			if(item.equals(PlayerInvMaker.CHEST)) {
				StatInv.openInv(player.getNewPlayer());
				check = true;
			}
			
			if(item.equals(PlayerInvMaker.EQUIPMENT)) {
				EquipmentInv.openInv(player.getNewPlayer());
				check = true;
			}
			
			if(item.equals(PlayerInvMaker.MENU)) {
				StatInv.openInv(player.getNewPlayer());
				check = true;
			}
			
			if(item.equals(PlayerInvMaker.QUEST)) {
				StatInv.openInv(player.getNewPlayer());
				check = true;
			}
			
			if(item.equals(PlayerInvMaker.SKILL)) {
				StatInv.openInv(player.getNewPlayer());
				check = true;
			}
			
			if(item.equals(PlayerInvMaker.STAT)) {
				StatInv.openInv(player.getNewPlayer());
				check = true;
			}
			
			if(check) {
				event.setCancelled(true);
				return;
			}
		}
		
		invClick(event);
	}
	
	public abstract void invClick(InventoryClickEvent event);
}
