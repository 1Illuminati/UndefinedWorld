package undefinedWorld.item;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import undefinedWorld.UndefinedWorld;

public class Item {
	protected static final String NOT_ITEM_MESSAGE = "ItemStack not have item_type Key in PersistentData";
	protected static final String NOT_KEY = "하위버전의 장비 혹은 무언가 문제가 있는 장비입니다";
	public static final NamespacedKey ITEM_TYPE = new NamespacedKey(UndefinedWorld.getPlugin(), "item_type");
	public static final NamespacedKey ITEM_DETAIL_TYPE = new NamespacedKey(UndefinedWorld.getPlugin(), "item_detail_type");
	public static final NamespacedKey ITEM_GRADE = new NamespacedKey(UndefinedWorld.getPlugin(), "item_grade");
	public static final NamespacedKey ITEM_EXPLAIN = new NamespacedKey(UndefinedWorld.getPlugin(), "item_explain");
	public static final NamespacedKey ITEM_EXCHANGE = new NamespacedKey(UndefinedWorld.getPlugin(), "item_exchange");
	
	protected String loreHelper(String str1, String str2, int length) {
		StringBuilder builder = new StringBuilder(str1);
		for(int i = str1.length(); i < length; i++) {
			builder.append(" ");
		}
		int start = builder.length() - str2.length();
		for(int i = 0; i < str2.length(); i++) {
			builder.setCharAt(start + i, str2.charAt(i));
		}
		
		return builder.toString();
	}
	
	public ItemStack setNormalData(ItemStack itemStack, String itemType) {
		ItemMeta itemMeta = itemStack.getItemMeta();
		PersistentDataContainer data = itemMeta.getPersistentDataContainer();
		data.set(ITEM_TYPE, PersistentDataType.STRING, itemType);
		data.set(ITEM_DETAIL_TYPE, PersistentDataType.STRING, itemType);
		data.set(ITEM_GRADE, PersistentDataType.STRING, "?");
		data.set(ITEM_EXPLAIN, PersistentDataType.STRING, "");
		data.set(ITEM_EXCHANGE, PersistentDataType.STRING, "true");
		itemStack.setItemMeta(itemMeta);
		
		return itemStack;
	}
	
	public ItemStack setLore(ItemStack itemStack) {
		ItemMeta itemMeta = itemStack.getItemMeta();
		PersistentDataContainer data = itemMeta.getPersistentDataContainer();
		String type = data.get(ITEM_TYPE, PersistentDataType.STRING);
		String detailType = data.get(ITEM_DETAIL_TYPE, PersistentDataType.STRING);
		String grade = data.get(ITEM_GRADE, PersistentDataType.STRING);
		String explain = data.get(ITEM_EXPLAIN, PersistentDataType.STRING);
		boolean exchange = Boolean.valueOf(data.get(ITEM_EXCHANGE, PersistentDataType.STRING));
		
		List<String> lore = new ArrayList<>();
		lore.add("§8§m                    ");
		lore.add("§7" + type);
		lore.add("§7" + grade);
		lore.add("§8§m                    ");
		lore.add("§7" + explain);
		lore.add("");
		lore.add("");
		lore.add(exchange ? "" : "§4§l교환불가");
		
		itemMeta.setLore(lore);
		itemStack.setItemMeta(itemMeta);
		
		return itemStack;
	}
	
	public boolean checkItem(ItemStack itemStack) {
		PersistentDataContainer data = itemStack.getItemMeta().getPersistentDataContainer();
		if(data.has(ITEM_TYPE, PersistentDataType.STRING)) {
			return true;
		}
		
		return false;
	}
	
	public String getItemType(ItemStack itemStack) {
		PersistentDataContainer data = itemStack.getItemMeta().getPersistentDataContainer();
		if(!this.checkItem(itemStack)) {
			throw new IllegalArgumentException(NOT_ITEM_MESSAGE);
		} else if(!data.has(ITEM_TYPE, PersistentDataType.STRING)) {
			throw new NullPointerException(NOT_KEY);
		}
		
		return data.get(ITEM_TYPE, PersistentDataType.STRING);
	}
	
	public String getItemExplain(ItemStack itemStack) {
		PersistentDataContainer data = itemStack.getItemMeta().getPersistentDataContainer();
		if(!this.checkItem(itemStack)) {
			throw new IllegalArgumentException(NOT_ITEM_MESSAGE);
		} else if(!data.has(ITEM_EXPLAIN, PersistentDataType.STRING)) {
			throw new NullPointerException(NOT_KEY);
		}
		
		return data.get(ITEM_EXPLAIN, PersistentDataType.STRING);
	}
	
	public ItemStack setItemDetailType(ItemStack itemStack, String explain) {
		ItemMeta itemMeta = itemStack.getItemMeta();
		PersistentDataContainer data = itemMeta.getPersistentDataContainer();
		if(!this.checkItem(itemStack)) {
			throw new IllegalArgumentException(NOT_ITEM_MESSAGE);
		}
		data.set(ITEM_DETAIL_TYPE, PersistentDataType.STRING, explain);
		itemStack.setItemMeta(itemMeta);
		
		return itemStack;
	}
	
	public String getItemDetailType(ItemStack itemStack) {
		PersistentDataContainer data = itemStack.getItemMeta().getPersistentDataContainer();
		if(!this.checkItem(itemStack)) {
			throw new IllegalArgumentException(NOT_ITEM_MESSAGE);
		} else if(!data.has(ITEM_DETAIL_TYPE, PersistentDataType.STRING)) {
			throw new NullPointerException(NOT_KEY);
		}
		
		return data.get(ITEM_DETAIL_TYPE, PersistentDataType.STRING);
	}
	
	public ItemStack setItemExplain(ItemStack itemStack, String explain) {
		ItemMeta itemMeta = itemStack.getItemMeta();
		PersistentDataContainer data = itemMeta.getPersistentDataContainer();
		if(!this.checkItem(itemStack)) {
			throw new IllegalArgumentException(NOT_ITEM_MESSAGE);
		}
		data.set(ITEM_EXPLAIN, PersistentDataType.STRING, explain);
		itemStack.setItemMeta(itemMeta);
		
		return itemStack;
	}
	
	public ItemGrade getItemGrade(ItemStack itemStack) {
		PersistentDataContainer data = itemStack.getItemMeta().getPersistentDataContainer();
		if(!this.checkItem(itemStack)) {
			throw new IllegalArgumentException(NOT_ITEM_MESSAGE);
		} else if(!data.has(ITEM_GRADE, PersistentDataType.STRING)) {
			throw new NullPointerException(NOT_KEY);
		}
		
		return ItemGrade.valueOf(data.get(ITEM_GRADE, PersistentDataType.STRING));
	}
	
	public ItemStack setItemGrade(ItemStack itemStack, ItemGrade grade) {
		ItemMeta itemMeta = itemStack.getItemMeta();
		PersistentDataContainer data = itemMeta.getPersistentDataContainer();
		if(!this.checkItem(itemStack)) {
			throw new IllegalArgumentException(NOT_ITEM_MESSAGE);
		}
		data.set(ITEM_GRADE, PersistentDataType.STRING, grade.name());
		itemStack.setItemMeta(itemMeta);
		
		return itemStack;
	}
	
	public boolean getItemExchnage(ItemStack itemStack) {
		PersistentDataContainer data = itemStack.getItemMeta().getPersistentDataContainer();
		if(!this.checkItem(itemStack)) {
			throw new IllegalArgumentException(NOT_ITEM_MESSAGE);
		} else if(!data.has(ITEM_EXCHANGE, PersistentDataType.STRING)) {
			throw new NullPointerException(NOT_KEY);
		}
		
		return Boolean.valueOf(data.get(ITEM_EXCHANGE, PersistentDataType.STRING));
	}
	
	public ItemStack setItemExchnage(ItemStack itemStack, Boolean value) {
		ItemMeta itemMeta = itemStack.getItemMeta();
		PersistentDataContainer data = itemMeta.getPersistentDataContainer();
		if(!this.checkItem(itemStack)) {
			throw new IllegalArgumentException(NOT_ITEM_MESSAGE);
		}
		data.set(ITEM_EXCHANGE, PersistentDataType.STRING, value.toString());
		itemStack.setItemMeta(itemMeta);
		
		return itemStack;
	}
	
	public enum ItemGrade {
		F("7"),
		E("f"),
		D("a"),
		C("b"),
		B("1"),
		A("5"),
		S("e"),
		L("6"),
		H("4");
		
		public final String color;
		ItemGrade(String color) {
			this.color = "§" + color + name();
		}
	}
}
