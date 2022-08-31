package undefinedWorld.entity.player.attribute;

import org.bukkit.NamespacedKey;

import doublePlugin.entity.player.NewPlayer;
import undefinedWorld.UndefinedWorld;

public class PlayerAttribute {
	NewPlayer player;
	
	public PlayerAttribute(NewPlayer player) {
		this.player = player;
	}
	
	public void addAttribute(Attributes attribute, double value) {
		player.addDoubleValue(attribute.name(), value);
	}
	
	public void setAttribute(Attributes attribute, double value) {
		player.setDoubleValue(attribute.name(), value);
	}
	
	public double getAttribute(Attributes attribute) {
		return player.getDoubleValue(attribute.name());
	}
	
	public void clearAttribute() {
		for(Attributes attribute : Attributes.values()) {   
			setAttribute(attribute, 0);
		}
	}
	
	public enum Attributes {
		PHYSICAL_POWER("물리 공격력"),
		MAGIC_POWER("마법 공격력"),
		PHYSICAL_DEFENSE("물리 방어력"),
		MAGIC_DEFENSE("마법 방어력"),
		HEALTH("체력"),
		HEALTH_REGEN("체력 회복량"),
		SPEED("이속"),
		DODGE("회피"),
		CRITICAL_DAMAGE("크리티컬 데미지"),
		CRITICAL_PERCENTAGE("크리티컬 확률"),
		MANA("마나"),
		MANA_MAX("최대마나"),
		MANA_REGEN("마나 회복량"),
		ITEM_DROP("아이템 드랍"),
		UPGRADE_PERCENTAGE("강화 성공 확률"),
		BREWING_PERCENTAGE("양조 성공 확률"),
		VAMFIRE("흡혈량"),
		COOLTIME_DOWN("스킬 쿨 감소"),
		EXP_UP("추가 경험치");
		
		private final NamespacedKey key;
		private final String name;
		Attributes(String name) {
			this.name = name;
			this.key = new NamespacedKey(UndefinedWorld.getPlugin(), name().toLowerCase());
		}
		
		public NamespacedKey getKey() {
			return this.key;
		}
		
		public String getName() {
			return this.name;
		}
	}
}
