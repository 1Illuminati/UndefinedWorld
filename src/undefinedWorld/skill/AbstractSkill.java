package undefinedWorld.skill;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import doublePlugin.entity.player.NewPlayer;
import doublePluginSkill.skill.Skill;
import undefinedWorld.entity.player.attribute.PlayerAttribute.Attributes;

public abstract class AbstractSkill extends Skill {
	
	public static AbstractSkill getSkill(String code) {
		if(Skill.getSkill(code) instanceof AbstractSkill) {
			return (AbstractSkill) Skill.getSkill(code);
		}
		
		return null;
	}
	
	public abstract double getMana();
	
	public void useSkill(NewPlayer newplayer) {
		
		UndefinedPlayer player = UndefinedPlayer.getUndefinedPlayer(newplayer);
        if(!player.checkCoolTime(this.getName())) {
        	lessCoolEvent(newplayer);
            return;
        }
        
        double mana = player.getAttribute(Attributes.MANA);
		
		if(mana < this.getMana()) {
			player.sendMessage("§c마나가 부족합니다!");
			return;
		}
		
		player.addAttribute(Attributes.MANA, -this.getMana());

        player.setCoolTimeSecond(this.getName(), this.getCoolTime());
        skill(newplayer);
    }
	
	public List<LivingEntity> noPlayerTarget(NewPlayer player, Location loc, double distance) {
		List<LivingEntity> result = new ArrayList<>();
		for(LivingEntity livingEntity : getTarget(loc, distance)) {
			if(!livingEntity.equals(player.getPlayer())) {
				result.add(livingEntity);
			}
		}
		
		return result;
	}
}
