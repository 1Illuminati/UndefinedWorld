package undefinedWorld.item.event;

import org.bukkit.inventory.ItemStack;

import doublePlugin.entity.player.NewPlayer;
import doublePlugin.item.ItemEvent;
import net.md_5.bungee.api.ChatColor;

public class ExpBook extends ItemEvent {
	@Override
	public boolean checkItem(ItemStack item) {
		if(item == null) 
			return false;
		
		if(!item.getItemMeta().hasDisplayName())
			return false;
		
		return ChatColor.stripColor(item.getItemMeta().getDisplayName()).endsWith(" 경험치북");
	}

	@Override
	public boolean dropItem(NewPlayer arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean leftClick(NewPlayer arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean rightClick(NewPlayer player) {
		UndefinedPlayer undefinedPlayer = UndefinedPlayer.getUndefinedPlayer(player);
		int exp = Integer.valueOf(ChatColor.stripColor(player.getItemInHand().getItemMeta().getDisplayName()).split(" ")[0]);
		undefinedPlayer.addUndeinfedExp(exp);
		undefinedPlayer.sendMessage("§2경험치 +" + exp);
		undefinedPlayer.getItemInHand().setAmount(player.getItemInHand().getAmount() - 1);
		undefinedPlayer.checkLevelUp();
		return true;
	}

	@Override
	public boolean shiftDropItem(NewPlayer arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean shiftLeftClick(NewPlayer arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean shiftRightClick(NewPlayer arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean shiftSwapHand(NewPlayer arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean swapHand(NewPlayer arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getCode() {
		return "경험치북";
	}

}
