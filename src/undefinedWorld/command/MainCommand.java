package undefinedWorld.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import undefinedWorld.command.admin.AdminCommand;
import undefinedWorld.entity.player.invenrtory.stat.StatInv;

public class MainCommand implements CommandExecutor {
	public static final String STAT = "stat";
	public static final String ADMIN = "admin";
	private final AdminCommand admin = new AdminCommand();
	@Override
	public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
		if(!(commandSender instanceof Player)) {
			return true;
		}
		UndefinedPlayer sender = UndefinedPlayer.getUndefinedPlayer((Player) commandSender);
		
		switch(command.getName()) {
			case STAT :
				StatInv.openInv(sender.getNewPlayer());
			break;
			case ADMIN :
				admin.adminCommand(sender, args);
			break;
		}
		
		
		return true;
	}
}
