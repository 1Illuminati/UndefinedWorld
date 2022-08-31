package undefinedWorld.entity.player.invenrtory;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import doublePlugin.entity.player.NewPlayer;
import doublePlugin.item.ItemBuilder;
import doublePlugin.item.MakeItem;

public class PlayerInvMaker {
	public static final ItemStack EQUIPMENT = new ItemBuilder(Material.IRON_INGOT).setCustomModelData(1).setDisplayName("장비").make();
	public static final ItemStack STAT = new ItemBuilder(Material.IRON_INGOT).setCustomModelData(1).setDisplayName("스텟").make();
	public static final ItemStack CHEST = new ItemBuilder(Material.IRON_INGOT).setCustomModelData(1).setDisplayName("창고").make();
	public static final ItemStack SKILL = new ItemBuilder(Material.IRON_INGOT).setCustomModelData(1).setDisplayName("스킬").make();
	public static final ItemStack QUEST = new ItemBuilder(Material.IRON_INGOT).setCustomModelData(1).setDisplayName("퀘스트").make();
	public static final ItemStack MENU = new ItemBuilder(Material.IRON_INGOT).setCustomModelData(1).setDisplayName("메뉴").make();
	private Inventory inventory;
    
    public PlayerInvMaker(NewPlayer player, String invName) {
        this.inventory = Bukkit.createInventory(player.getPlayer(), 54, invName);
        inventory.setItem(8, EQUIPMENT);
        inventory.setItem(17, STAT);
        inventory.setItem(26, CHEST);
        inventory.setItem(35, SKILL);    
        inventory.setItem(44, QUEST);
        inventory.setItem(53, MENU);
    }
    
    public Inventory getInv() {
    	return this.inventory;
    }
    
    public int size() {
    	return 42;
    }
    
    private int getSlot(int slot) {
    	if(slot >= 42) {
    		throw new IllegalArgumentException();
    	}
    	
    	slot += (int) (slot / 7) * 2;
    	
    	return slot;
    }
    
    public void filledItem(ItemStack itemStack) {
    	for(int i = 0; i < 42; i++) {
    		this.setItem(i, itemStack);
    	}
    }
    
    public void setAir(int slot) {
    	this.setItem(slot, MakeItem.getAir());
    }
    
    public ItemStack getItem(int slot) {
    	return this.getInv().getItem(this.getSlot(slot));
    }
    
    public void setItem(int slot, ItemStack itemStack) {
    	this.getInv().setItem(this.getSlot(slot), itemStack);
    }
    
    public void addItem(ItemStack... itemStacks) {
    	this.getInv().addItem(itemStacks);
    }
}
