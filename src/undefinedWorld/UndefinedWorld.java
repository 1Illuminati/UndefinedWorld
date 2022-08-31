package undefinedWorld;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import doublePlugin.plugin.PluginInfoMaps;
import doublePlugin.util.map.InfoMaps;
import undefinedWorld.command.MainCommand;
import undefinedWorld.event.MainEvent;
import undefinedWorld.item.event.ExpBook;
import undefinedWorld.entity.player.invenrtory.equipment.EquipmentInv;
import undefinedWorld.entity.player.invenrtory.stat.StatInv;
import undefinedWorld.skill.skillList.MagicSkill;
import undefinedWorld.skill.skillList.WarriorSkillSL;
import undefinedWorld.skill.skillList.thief.ThiefRight;
import undefinedWorld.skill.skillList.thief.ThiefShiftLeft;
import undefinedWorld.skill.weapon.ThiefWeapon;

public class UndefinedWorld extends JavaPlugin implements Listener {
	public static boolean reload;
	public static final String pluginName = "UndefinedWorld-1.16.5";
	private static Plugin plugin;
	private static InfoMaps infoMaps;
	
	@Override
	public void onEnable() {
		plugin = Bukkit.getPluginManager().getPlugin(pluginName);
		sendLog("Plugin Load");
		infoMaps = PluginInfoMaps.getPluginInfoMaps(plugin);
		sendLog("InfoMaps Load");
		registerCommand();
		sendLog("Command Load");
		registerEvent();
		sendLog("Event Load");
		setItemEvent();
		sendLog("ItemEvent Load");
		setInventoryAdmin();
		sendLog("InventoryAdmin Load");
		setSkill();
		sendLog("Skill Load");
		setSkillWeapon();
		sendLog("SkillWeapon Load");
		
		sendLog("§5-----------------------------");
		sendLog(""); 
		sendLog("");
		sendLog("§5UndefinedWorld 플러그인 적용완료");
		sendLog("");
		sendLog("");
		sendLog("§5-----------------------------");
		reload = false;
	}
	
	private static void setItemEvent() {
		new ExpBook();
	}
	
	private static void setInventoryAdmin() {
		new StatInv();
		new EquipmentInv();
	}
	
	private static void setSkill() {
		new WarriorSkillSL();
		new MagicSkill();
		new ThiefRight();
		new ThiefShiftLeft();
	}
	
	private static void setSkillWeapon() {
		new ThiefWeapon();
	}
	
	public static void sendLog(String log) {
		Bukkit.getConsoleSender().sendMessage("[" + pluginName + "] " + log);;
	}
	
	@Override
	public void onDisable() {
		reload = true;
		sendLog("§5-----------------------------");
		sendLog("");
		sendLog("");
		sendLog("§5UndefinedWorld 플러그인 적용해제");
		sendLog("");
		sendLog("");
		sendLog("§5-----------------------------");
	}
	
	public void registerEvent() {
		PluginManager pm = Bukkit.getServer().getPluginManager();
		pm.registerEvents(new MainEvent(), this);
	}
	
	public void setCommand(String commandName, MainCommand command) {
		this.getCommand(commandName).setExecutor(command);
	}
	public void registerCommand() {
		this.setCommand(MainCommand.STAT, new MainCommand());
		this.setCommand(MainCommand.ADMIN, new MainCommand());
	}
	
	public static Plugin getPlugin() {
		return plugin;
	}
	
	public static InfoMaps getInfoMaps() {
		return infoMaps;
	}
}
