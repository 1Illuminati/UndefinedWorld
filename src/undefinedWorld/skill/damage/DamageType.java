package undefinedWorld.skill.damage;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataType;

import undefinedWorld.UndefinedWorld;

public enum DamageType {
	PHYSICAL(),
	MAGIC(),
	FIXED_PHYSICAL(),
	FIXED_MAGIC(),
	THRON(),
	
	NULL();
	
	private static final NamespacedKey key = new NamespacedKey(UndefinedWorld.getPlugin(), "DamageType");
	DamageType() {
		
	}
	
	public static void setType(Entity entity, DamageType type) {
		entity.getPersistentDataContainer().set(key, PersistentDataType.STRING, type.name());
	}
	
	public static DamageType getType(Entity entity) {
		if(!entity.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
			return DamageType.NULL;
		}
		
		return DamageType.valueOf(entity.getPersistentDataContainer().get(key, PersistentDataType.STRING));
	}
	
	public static void clearType(Entity entity) {
		entity.getPersistentDataContainer().set(key, PersistentDataType.STRING, DamageType.NULL.name());
	}
}
