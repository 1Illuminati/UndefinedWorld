package undefinedWorld.entity.player.stat;

import org.bukkit.NamespacedKey;

import net.md_5.bungee.api.ChatColor;
import undefinedWorld.UndefinedWorld;

public enum Stats {
	STR("§c근력"),
	AGI("§9민첩"),
	HEL("§2체력"),
	INT("§6지능"),
	CRI("§5집중"),
	LCK("§a행운"),
	VAM("§4흡혈"),
	SPI("§e정신"),
	STATPOINT("§f스텟포인트");
	
	private final String name;
	private final String displayName;
	private final NamespacedKey key;
	Stats(String name) {
		this.displayName = name;
		this.name = ChatColor.stripColor(name);
		this.key = new NamespacedKey(UndefinedWorld.getPlugin(), name().toLowerCase());
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getDisplayName() {
		return this.displayName;
	}
	
	public NamespacedKey getKey() {
		return this.key;
	}
}
