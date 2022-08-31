package undefinedWorld.skill.skillList.thief;


import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import doublePlugin.entity.player.NewPlayer;
import undefinedWorld.entity.player.attribute.PlayerAttribute.Attributes;
import undefinedWorld.skill.AbstractSkill;

public class ThiefRight extends AbstractSkill {

	@Override
	public void lessCoolEvent(NewPlayer player) {
		double lessCool = player.getLessCoolTime(getName());
		player.sendMessage("§8[§f도적-R§8] §f" + lessCool + "초 남았습니다");
	}

	@Override
	protected void skill(NewPlayer newplayer) {
		UndefinedPlayer player = UndefinedPlayer.getUndefinedPlayer(newplayer);
		Location loc = player.getLocation();
		boolean temp = false;
		for(int i = 1; i < 7; i++) {
			Location check = loc.clone().add(this.getVec(i, i, i, loc.getPitch(), loc.getYaw()));
			for(LivingEntity livingEntity : getTarget(check, 1)) {
				if(livingEntity != player.getPlayer()) {
					player.teleport(this.getBehindLocation(livingEntity.getLocation()));
					livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 20, false, false, false));
					temp = true;
					break;
				}
			}
		}
		
		if(!temp) {
			player.addAttribute(Attributes.MANA, getMana());
			player.removeCoolTime(getName());
			player.sendMessage("§c타겟이 없습니다!");
		}
	}

	@Override
	public double getMana() {
		return 100;
	}

	@Override
	public double getCoolTime() {
		return 30;
	}

	@Override
	public String getName() {
		return "도적우클";
	}
}
