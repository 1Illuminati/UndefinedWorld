package undefinedWorld.skill.skillList;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;

import doublePlugin.entity.player.NewPlayer;
import undefinedWorld.skill.AbstractSkill;
import undefinedWorld.skill.damage.DamageType;
import undefinedWorld.skill.damage.GiveDamage;

public class MagicSkill extends AbstractSkill {

	@Override
	public void lessCoolEvent(NewPlayer player) {
		double lessCool = player.getLessCoolTime("마법");
		player.sendMessage("§8[§f전사-SL§8] §f" + lessCool + "초 남았습니다");
	}

	@Override
	protected void skill(NewPlayer player) {
		Location loc = player.getLocation();
		for(LivingEntity livingEntity : getTarget(loc, 30)) {
			if(!livingEntity.equals(player.getPlayer())) {
				spawnParticle(Particle.EXPLOSION_HUGE, livingEntity.getLocation(), 1);
				GiveDamage.giveDamage(player.getPlayer(), livingEntity, DamageType.MAGIC, 10);
			}
		}
	}

	@Override
	public double getMana() {
		return 100;
	}

	@Override
	public double getCoolTime() {
		return 20;
	}

	@Override
	public String getName() {
		return "마법";
	}

}
