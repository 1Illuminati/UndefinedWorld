package undefinedWorld.entity.player.invenrtory.stat;

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import doublePlugin.entity.player.NewPlayer;
import doublePlugin.inventory.InventoryManager;
import doublePlugin.item.ItemBuilder;
import doublePlugin.item.MakeItem;
import undefinedWorld.entity.player.attribute.PlayerAttribute.Attributes;
import undefinedWorld.entity.player.invenrtory.AbstractPlayerInvEvent;
import undefinedWorld.entity.player.invenrtory.PlayerInvMaker;
import undefinedWorld.entity.player.stat.Stats;

public class StatInv extends AbstractPlayerInvEvent {
	private static final int MAX_STAT = 100;
	public static final String INV_CODE = "스텟";

	
	public static void openInv(NewPlayer player) {
		player.openInventory(InventoryManager.getInventoryEvent(INV_CODE).getInv(player));
	}

	@Override
	public void close(InventoryCloseEvent event) {
		UndefinedPlayer player = UndefinedPlayer.getUndefinedPlayer((Player) event.getPlayer());
		player.setStatAttribute();
	}

	@Override
	public void open(InventoryOpenEvent event) {}

	@Override
	public void invClick(InventoryClickEvent event) {
		ItemStack item = event.getCurrentItem();
		
		if(item == null || item.getType() == Material.AIR)
			return;
		
		event.setCancelled(true);
		
		if(!item.getItemMeta().hasDisplayName())
			return;
		
		String display = item.getItemMeta().getDisplayName();
		for(Stats stat : Stats.values()) {
			if(display.equals(stat.getDisplayName())) {
				UndefinedPlayer player = UndefinedPlayer.getUndefinedPlayer((Player) event.getWhoClicked());
				if(player.getStat(Stats.STATPOINT) <= 0) {
					player.sendMessage("§c스텟 포인트가 부족합니다.");
					return;
				}
				
				if(player.getStat(stat) >= MAX_STAT) {
					player.sendMessage("§c한계에 도달하셨습니다.");
					return;
				}
				
				player.addStat(stat, 1);
				player.addStat(Stats.STATPOINT, -1);
				openInv(player.getNewPlayer());
			}
		}
	}

	@Override
	public String getCode() {
		return INV_CODE;
	}

	@Override
	public Inventory getInv(NewPlayer newplayer) {
		UndefinedPlayer player = UndefinedPlayer.getUndefinedPlayer(newplayer);
		PlayerInvMaker invMaker = new PlayerInvMaker(player.getNewPlayer(), INV_CODE);
		
		int str = player.getStat(Stats.STR);
		int agi = player.getStat(Stats.AGI);
		int hel = player.getStat(Stats.HEL);
		int imt = player.getStat(Stats.INT);
		int cri = player.getStat(Stats.CRI);
		int lck = player.getStat(Stats.LCK);
		int vam = player.getStat(Stats.VAM);
		int spi = player.getStat(Stats.SPI);
		
		invMaker.setItem(3, MakeItem.makeSkullItem("§f" + player.getName(), "79cbb4d794da4c600eac20fdd662f421cd456a39167af7bb9e054674e0856d16", 
				Arrays.asList("","§f현재 보유한 스텟 포인트 : " + player.getStat(Stats.STATPOINT),"","",
						"§f  물리 공격력 총 증가량 : " + player.getAttribute(Attributes.PHYSICAL_POWER),
						"§f  마법 공격력 총 증가량 : " + player.getAttribute(Attributes.MAGIC_POWER),
						"§f  물리 방어력 총 증가량 : " + player.getAttribute(Attributes.PHYSICAL_DEFENSE),
						"§f  마법 방어력 총 증가량 : " + player.getAttribute(Attributes.MAGIC_DEFENSE),
						"§f  이동 속도 총 증가량 : " + player.getAttribute(Attributes.SPEED) + "%",
						"§f  회피 확률 총 증가량 : " + player.getAttribute(Attributes.DODGE) + "%",
						"§f  체력 총 증가량 : " + player.getAttribute(Attributes.HEALTH),
						"§f  크리티컬 확률 총 증가량 : " + player.getAttribute(Attributes.CRITICAL_PERCENTAGE) + "%",
						"§f  크리티컬 데미지 총 증가량 : " + player.getAttribute(Attributes.CRITICAL_DAMAGE) + "%",
						"§f  최대 마나 총 증가량 : " + (player.getAttribute(Attributes.MANA_MAX) - 100),
						"§f  초당 마나 회복 총 증가량 : " + (player.getAttribute(Attributes.MANA_REGEN) - 2),
						"§f  흡혈 총 증가량 : " + player.getAttribute(Attributes.VAMFIRE) + "%",
						"§f  아이템 드랍 총 증가량 : " + player.getAttribute(Attributes.ITEM_DROP) + "%")));
		
		invMaker.setItem(7, new ItemBuilder(Material.IRON_INGOT).setDisplayName(Stats.STR.getDisplayName()).setLore(Arrays.asList(
				"",
	            "§f현재 보유한 근력 스텟포인트 : " + str + "/" + MAX_STAT, 
	            "",
	            "§f스텟 증가량", 
	            "§7 + 물리 공격력 3", 
	            "§7 + 체력 10", 
	            "",
	            "§f상승한 스텟 증가량", 
	            "§7 + 물리 공격력 " + (str * 3),
	            "§7 + 체력 " + (str * 10))
				).setCustomModelData(1).make());
		
		invMaker.setItem(9, new ItemBuilder(Material.IRON_INGOT).setDisplayName(Stats.AGI.getDisplayName()).setLore(Arrays.asList(
				"",
	            "§f현재 보유한 민첩 스텟포인트 : " + agi + "/" + MAX_STAT, 
	            "",
	            "§f스텟 증가량", 
	            "§7 + 이동 속도 1%", 
	            "§7 + 회피 확률 0.5%", 
	            "",
	            "§f상승한 스텟 증가량", 
	            "§7 + 이동 속도 " + (agi * 1) + "%",
	            "§7 + 회피 확률 " + (agi * 0.5) + "%")
				).setCustomModelData(2).make());
		
		invMaker.setItem(11, new ItemBuilder(Material.IRON_INGOT).setDisplayName(Stats.HEL.getDisplayName()).setLore(Arrays.asList(
				"",
	            "§f현재 보유한 체력 스텟포인트 : " + hel + "/" + MAX_STAT, 
	            "",
	            "§f스텟 증가량", 
	            "§7 + 체력 40", 
	            "§7 + 물리 방어력 2", 
	            "§7 + 마법 방어력 2", 
	            "",
	            "§f상승한 스텟 증가량", 
	            "§7 + 체력 " + hel * 40,
	            "§7 + 물리 방어력 " + (hel * 2),
	            "§7 + 마법 방어력 " + (hel * 2))
				).setCustomModelData(3).make());
		
		invMaker.setItem(13, new ItemBuilder(Material.IRON_INGOT).setDisplayName(Stats.INT.getDisplayName()).setLore(Arrays.asList(
				"",
	            "§f현재 보유한 지능 스텟포인트 : " + imt + "/" + MAX_STAT, 
	            "",
	            "§f스텟 증가량", 
	            "§7 + 마법 공격력 3", 
	            "§7 + 최대 마나 20", 
	            "",
	            "§f상승한 스텟 증가량", 
	            "§7 + 마법 공격력 " + (imt * 3),
	            "§7 + 최대 마나" + (imt * 20))
				).setCustomModelData(4).make());
		
		invMaker.setItem(21, new ItemBuilder(Material.IRON_INGOT).setDisplayName(Stats.CRI.getDisplayName()).setLore(Arrays.asList(
				"",
	            "§f현재 보유한 집중 스텟포인트 : " + cri + "/" + MAX_STAT, 
	            "",
	            "§f스텟 증가량", 
	            "§7 + 크리티컬 확률 1%", 
	            "§7 + 크리티컬 데미지 3%", 
	            "",
	            "§f상승한 스텟 증가량", 
	            "§7 + 크리티컬 확률 " + (cri) + "%",
	            "§7 + 크리티컬 데미지 " + (cri * 3) + "%")
				).setCustomModelData(5).make());
		
		invMaker.setItem(23, new ItemBuilder(Material.IRON_INGOT).setDisplayName(Stats.LCK.getDisplayName()).setLore(Arrays.asList(
				"",
	            "§f현재 보유한 행운 스텟포인트 : " + lck + "/" + MAX_STAT, 
	            "",
	            "§f스텟 증가량",   
	            "§7 + 아이템 드랍 1%", 
	            "§7 + 회피 확률 0.1%", 
	            "§7 + 크리티컬 확률 0.3%", 
	            "",
	            "§f상승한 스텟 증가량", 
	            "§7 + 마법 공격력 " + (lck) + "%",
				"§7 + 회피 확률 " + (lck * 0.2) + "%",
				"§7 + 크리티컬 확률 " + (lck * 0.4) + "%")
				).setCustomModelData(6).make());
		
		invMaker.setItem(25, new ItemBuilder(Material.IRON_INGOT).setDisplayName(Stats.VAM.getDisplayName()).setLore(Arrays.asList(
				"",
	            "§f현재 보유한 흡혈 스텟포인트 : " + vam + "/" + MAX_STAT, 
	            "",
	            "§f스텟 증가량", 
	            "§7 + 흡혈량 0.03%", 
	            "",
	            "§f상승한 스텟 증가량", 
	            "§7 + 흡혈량 " + (vam * 0.03) + "%")
				).setCustomModelData(7).make());
		
		invMaker.setItem(27, new ItemBuilder(Material.IRON_INGOT).setDisplayName(Stats.SPI.getDisplayName()).setLore(Arrays.asList(
				"",
	            "§f현재 보유한 지능 스텟포인트 : " + spi + "/" + MAX_STAT, 
	            "",
	            "§f스텟 증가량", 
	            "§7 + 초당 마나 회복량 1", 
	            "§7 + 최대 마나 30", 
	            "",
	            "§f상승한 스텟 증가량", 
	            "§7 + 초당 마나 회복량 " + (spi),
	            "§7 + 최대 마나 " + (spi * 30))
				).setCustomModelData(8).make());
		
		return invMaker.getInv();
	}
}
