package undefinedWorld.skill.skillList;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;

import doublePlugin.entity.player.NewPlayer;
import doublePlugin.scheduler.RunnableEx;
import doublePlugin.scheduler.Scheduler;
import undefinedWorld.skill.AbstractSkill;
import undefinedWorld.skill.damage.DamageType;
import undefinedWorld.skill.damage.GiveDamage;

public class WarriorSkillSL extends AbstractSkill {

	@Override
	public void lessCoolEvent(NewPlayer player) {
		double lessCool = player.getLessCoolTime(getName());
		player.sendMessage("§8[§f전사-SL§8] §f" + lessCool + "초 남았습니다");
	}
	
	public List<Location> getCircleLoc(Location center, int startAng, int endAng, int angSize, double xSize, double yHeight, double zSize) {
        List<Location> list = new ArrayList<>();
        
        if(angSize == 0)
        	angSize++;

        for(int index = startAng; index <= endAng; index++) {
            double x = center.getX() + xSize * Math.cos(Math.toRadians(index / angSize));
            double y = center.getY() + yHeight;
            double z = center.getZ() + zSize * Math.sin(Math.toRadians(index / angSize));

            Location loc = new Location(center.getWorld(), x, y, z);
            list.add(loc);
        }

        return list;
    }
	
	public List<Location> getCircleLoc(Location center, double size) {
        return getCircleLoc(center, 0, 360, 1, size, 0, size);
    }

	@Override
	protected void skill(NewPlayer player) {
		Location loc = player.getLocation();
		player.setVelocity(this.getVec(0, -3, 0, loc.getPitch(), loc.getYaw()));
		
		Scheduler.delayScheduler(new RunnableEx() {

			@Override
			public void function() {
				player.setVelocity(getVec(0, 3, 0, loc.getPitch(), loc.getYaw()));
				for(Location loc : getCircleLoc(loc, 5)) {
					spawnParticle(Particle.EXPLOSION_HUGE, loc);
				}
				for(LivingEntity livingEntity : getTarget(loc, 5)) {
					if(!livingEntity.equals(player.getPlayer())) {
						GiveDamage.giveDamage(player.getPlayer(), livingEntity, DamageType.MAGIC, 4);
					}
				}
			}
			
		}, 40);
	}

	@Override
	public double getMana() {
		return 100;
	}

	@Override
	public double getCoolTime() {
		return 10;
	}

	@Override
	public String getName() {
		return "전사쉬좌";
	}

}
