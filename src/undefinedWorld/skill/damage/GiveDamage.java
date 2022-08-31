package undefinedWorld.skill.damage;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class GiveDamage {
	public static void giveDamage(Entity atkentity, LivingEntity defEntity, DamageType type, double damage) {
		DamageType.setType(atkentity, type);
		defEntity.damage(damage, atkentity);
	}
}
