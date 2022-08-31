package undefinedWorld.entity.player.invenrtory.equipment;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import doublePlugin.entity.player.NewPlayer;
import doublePlugin.inventory.InventoryManager;
import undefinedWorld.entity.player.equipment.PlayerEquipment;
import undefinedWorld.entity.player.UndefinedPlayer;
import undefinedWorld.entity.player.invenrtory.AbstractPlayerInvEvent;

public class EquipmentInv extends AbstractPlayerInvEvent {
	public static final String INV_CODE = "장비";

	
	public static void openInv(NewPlayer player) {
		player.openInventory(InventoryManager.getInventoryEvent(INV_CODE).getInv(player));
	}

	@Override
	public void close(InventoryCloseEvent event) {
		UndefinedPlayer.getUndefinedPlayer((Player) event.getPlayer()).saveEquipment();;
	}

	@Override
	public void open(InventoryOpenEvent event) {}

	@Override
	public void invClick(InventoryClickEvent event) {
		ItemStack clickItem = event.getCurrentItem();
		
		if(clickItem == null) {
			return;
		}
		
		if(clickItem.equals(PlayerEquipment.GLASS)) {
			event.setCancelled(true);
			return;
		}
	}

	@Override
	public String getCode() {
		return INV_CODE;
	}

	@Override
	public Inventory getInv(NewPlayer newPlayer) {
		UndefinedPlayer player = UndefinedPlayer.getUndefinedPlayer(newPlayer);
		return player.getEquipmentInvMaker().getInv();
	}

}
