package undefinedWorld.event.damage;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import doublePlugin.util.DoubleMath;
import undefinedWorld.entity.player.attribute.PlayerAttribute.Attributes;
import undefinedWorld.skill.damage.DamageType;

public class DamageEvent {
	public void newEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
		double damage = event.getDamage();
		Entity atkEntity = event.getDamager();
		Entity defEntity = event.getEntity();
		DamageType type = DamageType.getType(atkEntity);
		
		if(atkEntity instanceof Player) {
			UndefinedPlayer atkPlayer = UndefinedPlayer.getUndefinedPlayer((Player) atkEntity);
			String color;
			switch(type) {
				case NULL :
				case PHYSICAL :
				case FIXED_PHYSICAL :
					damage += atkPlayer.getAttribute(Attributes.PHYSICAL_POWER);
					color = "§c";
				break;
				case MAGIC :
				case FIXED_MAGIC :
					damage += atkPlayer.getAttribute(Attributes.MAGIC_POWER);
					color = "§e";
				break;
				case THRON :
					color = "§a";
				break;
				default :
					color = "§8";
			}
			
			boolean criCheck = false;
			if(DoubleMath.per(atkPlayer.getAttribute(Attributes.CRITICAL_PERCENTAGE))) {
				damage += damage * (atkPlayer.getAttribute(Attributes.CRITICAL_DAMAGE) * 0.01D) + 0.2D;
				criCheck = true;
			}
			
			double maxHealth = atkPlayer.getMaxHealth();
			
			double heal = atkPlayer.getHealth() + (maxHealth * atkPlayer.getAttribute(Attributes.VAMFIRE) * 0.01);
			heal = Math.round(heal * 1000) / 1000;
			
			if(heal <= maxHealth) {
				atkPlayer.setHealth(heal);
			} else {
				atkPlayer.setHealth(maxHealth);
			}
			
			damage = Math.round(damage * 1000) / 1000;
			atkPlayer.sendTitle("   ", "                      " + color + (criCheck ? "Critical" : "Damage") + " §7" + damage);
			DamageType.clearType(atkEntity);
		}
		
		if(defEntity instanceof Player) {
			UndefinedPlayer defPlayer = UndefinedPlayer.getUndefinedPlayer((Player) defEntity);
			switch(type) {
				case NULL :
				case PHYSICAL :
					damage -= defPlayer.getAttribute(Attributes.MAGIC_DEFENSE);
				break;
				case MAGIC :
					damage -= defPlayer.getAttribute(Attributes.PHYSICAL_DEFENSE);
				break;
				default:
			}
			
			if(DoubleMath.per(defPlayer.getAttribute(Attributes.DODGE))) {
				defPlayer.sendActionBar("공격을 회피하였습니다!");
				damage = 0;
			}
		}
		
		damage = Math.round(damage * 1000) / 1000;
		event.setDamage(damage);
	}
}
