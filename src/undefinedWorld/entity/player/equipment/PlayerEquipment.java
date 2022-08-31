package undefinedWorld.entity.player.equipment;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import doublePlugin.entity.player.NewPlayer;
import doublePlugin.item.MakeItem;
import undefinedWorld.UndefinedWorld;
import undefinedWorld.entity.player.invenrtory.PlayerInvMaker;
import undefinedWorld.entity.player.invenrtory.equipment.EquipmentInv;

public class PlayerEquipment {
	public static final ItemStack GLASS = MakeItem.makeItem("§f장식", Material.WHITE_STAINED_GLASS_PANE);
	private NewPlayer player;
	private PlayerInvMaker inv;
	
	public PlayerEquipment(NewPlayer player) {
		this.player = player;
	}
	
	public void setEquipment(EquipmentSlot slot, ItemStack item) {
		player.setItemStackValue(slot.name(), item);
	}
	
	public ItemStack getEquipment(EquipmentSlot slot) {
		return player.getItemStackValue(slot.name());
	}
	
	public void save() {
		setEquipment(EquipmentSlot.HELMET, inv.getItem(8));
		setEquipment(EquipmentSlot.CHESTPLATE, inv.getItem(15));
		setEquipment(EquipmentSlot.LEGGINGS, inv.getItem(22));
		setEquipment(EquipmentSlot.BOOTS, inv.getItem(29));
		
		setEquipment(EquipmentSlot.WEAPON, inv.getItem(10));
		setEquipment(EquipmentSlot.SUB_WEAPON, inv.getItem(17));
		setEquipment(EquipmentSlot.NECKLACE, inv.getItem(24));
		setEquipment(EquipmentSlot.RING, inv.getItem(31));
		
		setEquipment(EquipmentSlot.RUNE1, inv.getItem(12));
		setEquipment(EquipmentSlot.RUNE2, inv.getItem(19));
		setEquipment(EquipmentSlot.RUNE3, inv.getItem(26));
	}
	
	public PlayerInvMaker getInvMaker() {
		if(inv == null) {
			PlayerInvMaker invMaker = new PlayerInvMaker(player, EquipmentInv.INV_CODE);
			invMaker.filledItem(GLASS);
			
			invMaker.setItem(8, getEquipment(EquipmentSlot.HELMET));
			invMaker.setItem(15, getEquipment(EquipmentSlot.CHESTPLATE));
			invMaker.setItem(22, getEquipment(EquipmentSlot.LEGGINGS));
			invMaker.setItem(29, getEquipment(EquipmentSlot.BOOTS));
			
			invMaker.setItem(10, getEquipment(EquipmentSlot.WEAPON));
			invMaker.setItem(17, getEquipment(EquipmentSlot.SUB_WEAPON));
			invMaker.setItem(24, getEquipment(EquipmentSlot.NECKLACE));
			invMaker.setItem(31, getEquipment(EquipmentSlot.RING));
			
			invMaker.setItem(12, getEquipment(EquipmentSlot.RUNE1));
			invMaker.setItem(19, getEquipment(EquipmentSlot.RUNE2));
			invMaker.setItem(26, getEquipment(EquipmentSlot.RUNE3));
			
			this.inv = invMaker;
		}
		
		return this.inv;
	}
	
	public enum EquipmentSlot {
		WEAPON("무기"),
		SUB_WEAPON("보조무기"),
		HELMET("투구"),
		CHESTPLATE("흉갑"),
		LEGGINGS("레깅스"),
		BOOTS("부츠"),
		RING("반지"),
		NECKLACE("목걸이"),
		RUNE1("룬"),
		RUNE2("룬"), 
		RUNE3("룬"),
		
		NULL("널");
		
		private final NamespacedKey key;
		private final String type;
		EquipmentSlot(String type) {
			this.type = type;
			this.key = new NamespacedKey(UndefinedWorld.getPlugin(), name().toLowerCase());
		}
		
		public String getType() {
			return this.type;
		}
		
		public NamespacedKey getKey() {
			return this.key;
		}
	}
}
