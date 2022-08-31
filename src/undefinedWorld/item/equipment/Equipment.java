package undefinedWorld.item.equipment;

import java.lang.IllegalArgumentException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import undefinedWorld.UndefinedWorld;
import undefinedWorld.item.Item;
import undefinedWorld.entity.player.attribute.PlayerAttribute.Attributes;
import undefinedWorld.entity.player.stat.Stats;

public class Equipment extends Item {
	protected static final String NOT_EQUIPMENT_MESSAGE = "ItemStack not have equipment Key in PersistentData";
	public static final NamespacedKey EQUIPMENT = new NamespacedKey(UndefinedWorld.getPlugin(), "equipment");
	public static final NamespacedKey LEVEL_LIMIT = new NamespacedKey(UndefinedWorld.getPlugin(), "level_limit");
	public static final NamespacedKey UPGRADE = new NamespacedKey(UndefinedWorld.getPlugin(), "upgrade");

	public boolean checkEquipment(ItemStack itemStack) {
		PersistentDataContainer data = itemStack.getItemMeta().getPersistentDataContainer();
		if(!data.has(EQUIPMENT, PersistentDataType.STRING)) {
			return false;
		}
		
		return true;
	}
	
	public int getLimit(ItemStack itemStack, Stats stat) {
		PersistentDataContainer data = itemStack.getItemMeta().getPersistentDataContainer();
		if(!this.checkEquipment(itemStack)) {
			throw new IllegalArgumentException(NOT_EQUIPMENT_MESSAGE);
		} else if(!data.has(stat.getKey(), PersistentDataType.INTEGER)) {
			throw new NullPointerException(NOT_KEY);
		}
		
		return data.get(stat.getKey(), PersistentDataType.INTEGER);
	}
	
	public int getLevelLimit(ItemStack itemStack) {
		PersistentDataContainer data = itemStack.getItemMeta().getPersistentDataContainer();
		if(!this.checkEquipment(itemStack)) {
			throw new IllegalArgumentException(NOT_EQUIPMENT_MESSAGE);
		} else if(!data.has(LEVEL_LIMIT, PersistentDataType.INTEGER)) {
			throw new NullPointerException(NOT_KEY);
		}
		
		return data.get(LEVEL_LIMIT, PersistentDataType.INTEGER);
	}
	
	public double getAddAttribute(ItemStack itemStack, Attributes abbribute) {
		PersistentDataContainer data = itemStack.getItemMeta().getPersistentDataContainer();
		if(!this.checkEquipment(itemStack)) {
			throw new IllegalArgumentException(NOT_EQUIPMENT_MESSAGE);
		} else if(!data.has(abbribute.getKey(), PersistentDataType.DOUBLE)) {
			throw new NullPointerException(NOT_KEY);
		}
		
		return data.get(abbribute.getKey(), PersistentDataType.DOUBLE);
	}
	
	public int getUpgrade(ItemStack itemStack) {
		PersistentDataContainer data = itemStack.getItemMeta().getPersistentDataContainer();
		if(!this.checkEquipment(itemStack)) {
			throw new IllegalArgumentException(NOT_EQUIPMENT_MESSAGE);
		} else if(!data.has(UPGRADE, PersistentDataType.INTEGER)) {
			throw new NullPointerException(NOT_KEY);
		}
		
		return data.get(UPGRADE, PersistentDataType.INTEGER);
	}
	
	public ItemStack setUpgrade(ItemStack itemStack, int value) {
		ItemMeta itemMeta = itemStack.getItemMeta();
		PersistentDataContainer data = itemMeta.getPersistentDataContainer();
		if(!this.checkEquipment(itemStack)) {
			throw new IllegalArgumentException(NOT_EQUIPMENT_MESSAGE);
		}
		
		data.set(UPGRADE, PersistentDataType.INTEGER, value);
		itemStack.setItemMeta(itemMeta);
		
		return itemStack;
	}
	
	public ItemStack setLevelLimit(ItemStack itemStack, int value) {
		ItemMeta itemMeta = itemStack.getItemMeta();
		PersistentDataContainer data = itemMeta.getPersistentDataContainer();
		if(!this.checkEquipment(itemStack)) {
			throw new IllegalArgumentException(NOT_EQUIPMENT_MESSAGE);
		}
		
		data.set(LEVEL_LIMIT, PersistentDataType.INTEGER, value);
		itemStack.setItemMeta(itemMeta);
		
		return itemStack;
	}
	
	public ItemStack setLimit(ItemStack itemStack, Stats stat, int value) {
		ItemMeta itemMeta = itemStack.getItemMeta();
		PersistentDataContainer data = itemMeta.getPersistentDataContainer();
		if(!this.checkEquipment(itemStack)) {
			throw new IllegalArgumentException(NOT_EQUIPMENT_MESSAGE);
		}
		
		data.set(stat.getKey(), PersistentDataType.INTEGER, value);
		itemStack.setItemMeta(itemMeta);
		
		return itemStack;
	}
	
	public ItemStack setAddAttribute(ItemStack itemStack, Attributes abbribute, double value) {
		ItemMeta itemMeta = itemStack.getItemMeta();
		PersistentDataContainer data = itemMeta.getPersistentDataContainer();
		if(!this.checkEquipment(itemStack)) {
			throw new IllegalArgumentException(NOT_EQUIPMENT_MESSAGE);
		}
		
		data.set(abbribute.getKey(), PersistentDataType.DOUBLE, value);
		itemStack.setItemMeta(itemMeta);
		
		return itemStack;
	}
	
	public ItemStack setNormalData(ItemStack itemStack, String itemType) {
		ItemMeta itemMeta = itemStack.getItemMeta();
		PersistentDataContainer data = itemMeta.getPersistentDataContainer();
		data.set(ITEM_TYPE, PersistentDataType.STRING, itemType);
		data.set(EQUIPMENT, PersistentDataType.STRING, itemType);
		data.set(ITEM_GRADE, PersistentDataType.STRING, "?");
		data.set(ITEM_EXPLAIN, PersistentDataType.STRING, "");
		data.set(LEVEL_LIMIT, PersistentDataType.INTEGER, 1);
		data.set(UPGRADE, PersistentDataType.INTEGER, 0);
		data.set(ITEM_EXCHANGE, PersistentDataType.STRING, "true");
		for(Stats stat : Stats.values()) {
			data.set(stat.getKey(), PersistentDataType.INTEGER, 0);
		}
		
		for(Attributes abbribute : Attributes.values()) {
			data.set(abbribute.getKey(), PersistentDataType.DOUBLE, 0D);
		}
		itemStack.setItemMeta(itemMeta);
		
		return itemStack;
	}
	
	public ItemStack setLore(ItemStack itemStack) {
		ItemMeta itemMeta = itemStack.getItemMeta();
		itemMeta.setUnbreakable(true);
		itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
		itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		PersistentDataContainer data = itemMeta.getPersistentDataContainer();
		String type = data.get(ITEM_TYPE, PersistentDataType.STRING);
		String detailType = data.get(ITEM_DETAIL_TYPE, PersistentDataType.STRING);
		String grade = data.get(ITEM_GRADE, PersistentDataType.STRING);
		String explain = data.get(ITEM_EXPLAIN, PersistentDataType.STRING);
		int levelLimit = data.get(LEVEL_LIMIT, PersistentDataType.INTEGER);
		int upgrade = data.get(UPGRADE, PersistentDataType.INTEGER);
		boolean exchange = Boolean.valueOf(data.get(ITEM_EXCHANGE, PersistentDataType.STRING));
		List<String> lore = new ArrayList<>();
		lore.add("§8§m                           ");
		lore.add("§f" + loreHelper(type, "§7< " + ItemGrade.valueOf(grade).color + " §7>", 29));
		lore.add("§f" + loreHelper(detailType, "" + upgrade + "강", 23));
		lore.add("§8§m                           ");
		for(Attributes abbribute : Attributes.values()) {
			double value = data.get(abbribute.getKey(), PersistentDataType.DOUBLE);
			if(value != 0) {
				lore.add("§f" + abbribute.getName() + " +" + value);
			}
		}
		lore.add("§8§m                           ");
		lore.add("§f레벨 " + levelLimit);
		for(Stats stat : Stats.values()) {
			int value = data.get(stat.getKey(), PersistentDataType.INTEGER);
			if(value != 0) {
				lore.add("§f" + stat.getName() + " " + value);
			}
		}
		lore.add("§8§m                           ");
		lore.add("§f" + explain);
		lore.add(exchange ? "" : "§4§l교환불가");
		itemStack.setItemMeta(itemMeta);
		itemMeta.setLore(lore);
		itemStack.setItemMeta(itemMeta);
		return itemStack;
	}
}
