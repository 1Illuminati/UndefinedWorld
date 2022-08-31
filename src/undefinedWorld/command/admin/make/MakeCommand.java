package undefinedWorld.command.admin.make;

import java.util.List;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.md_5.bungee.api.ChatColor;
import undefinedWorld.item.Item;
import undefinedWorld.item.Item.ItemGrade;
import undefinedWorld.item.equipment.Equipment;
import undefinedWorld.entity.player.attribute.PlayerAttribute.Attributes;
import undefinedWorld.entity.player.stat.Stats;

public class MakeCommand {
	private static final Item item = new Item();
	private static final Equipment equipment = new Equipment();
	private static final String CREATE = "create";
	private static final String ATTRIBUTE = "attribute";
	private static final String LIMIT = "limit";
	private static final String EXPLAIN = "explain";
	private static final String LORE = "lore";
	private static final String DISPLAY = "display";
	private static final String GRADE = "grade";
	private static final String UPGRADE = "upgrade";
	private static final String EXCHANGE = "exchange";
	private static final String DETAIL = "detail";
	private static final String DATA = "data";
	public void makeCommand(UndefinedPlayer sender, List<String> args) {
		String command = args.get(0);
		String value = args.get(1);
		args.remove(0);
		args.remove(0);
		ItemStack itemStack = sender.getItemInHand();
		if(itemStack == null) {
			return;
		}
		switch(command) {
			case CREATE :
				switch(value) {
					case "무기" :
					case "갑옷" :
					case "장신구" :
					case "보조무기" :
						itemStack = equipment.setNormalData(itemStack, value);
					break;
					default :
						itemStack = item.setNormalData(itemStack, value);
				}
			break;
			case GRADE :
				itemStack = item.setItemGrade(itemStack, ItemGrade.valueOf(value));
			break;
			case ATTRIBUTE :
				itemStack = equipment.setAddAttribute(itemStack, Attributes.valueOf(value), Integer.valueOf(args.get(0)));
			break;
			case LIMIT :
				if(value.equals("LEVEL")) {
					itemStack = equipment.setLevelLimit(itemStack, Integer.valueOf(args.get(0)));
				} else {
					itemStack = equipment.setLimit(itemStack, Stats.valueOf(value), Integer.valueOf(args.get(0)));
				}
			break;
			case EXPLAIN :
				StringBuilder builder1 = new StringBuilder(value);
				for(String str : args) {
					builder1.append(" " + str);
				} 
				itemStack = item.setItemExplain(itemStack, builder1.toString());
			break;
			case LORE :
				if(equipment.checkEquipment(itemStack)) {
					itemStack = equipment.setLore(itemStack);
				} else {
					itemStack = item.setLore(itemStack);
				}
			break;
			case DISPLAY :
				StringBuilder builder2 = new StringBuilder(value);
				for(String str : args) {
					builder2.append(" " + str);
				} 
 				ItemMeta meta = itemStack.getItemMeta();
				meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', builder2.toString()));
				itemStack.setItemMeta(meta);
			break;
			case UPGRADE :
				itemStack = equipment.setUpgrade(itemStack, Integer.valueOf(value));
			break;
			case EXCHANGE :
				itemStack = item.setItemExchnage(itemStack, Boolean.valueOf(value));
			break;
			case DETAIL :
				itemStack = item.setItemDetailType(itemStack, value);
			break;
			case DATA : 
				ItemMeta meta2 = itemStack.getItemMeta();
				meta2.setCustomModelData(Integer.valueOf(value));
				itemStack.setItemMeta(meta2);
			break;
			default :
				
		}
	}
}
