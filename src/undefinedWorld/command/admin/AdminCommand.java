package undefinedWorld.command.admin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;

import undefinedWorld.command.admin.make.MakeCommand;
import undefinedWorld.entity.player.level.Level;
import undefinedWorld.world.CustomWorld;

public class AdminCommand {
	public static final String EXP = "exp";
	public static final String CLEAR = "clear";
	public static final String MAKE = "make";
	public static final String MOVE = "move";
	public static final String FLAT = "flat";
	private final MakeCommand makeCommand = new MakeCommand();
	public void adminCommand(UndefinedPlayer sender, String[] args) {
		if(args.length <= 0) {
			return;
		}
		
		String command = args[0];
		List<String> list = new ArrayList(Arrays.asList(args));
		list.remove(0);
		
		switch(command) {
			case MAKE :
				makeCommand.makeCommand(sender, list);
			break;
			case EXP :
				sender.getInventory().addItem(Level.getExpBook(Integer.parseInt(list.get(0))));
			break;
			case CLEAR :
				sender.clearStat();
			break;
			case MOVE :
				sender.teleport(Bukkit.getWorld(list.get(0)).getSpawnLocation());
			break;
			case FLAT :
				CustomWorld.makeWorld(Integer.parseInt(list.get(1)), list.get(0));
			break;
		}
	}
}
