package undefinedWorld.skill.skillList.thief;

import org.bukkit.Particle;

import doublePlugin.entity.player.NewPlayer;
import doublePlugin.scheduler.RunnableEx;
import doublePlugin.scheduler.Scheduler;
import undefinedWorld.skill.AbstractSkill;

public class ThiefShiftLeft extends AbstractSkill {

	@Override
	public void lessCoolEvent(NewPlayer player) {
		double lessCool = player.getLessCoolTime(getName());
		player.sendMessage("§8[§f도적-SL§8] §f" + lessCool + "초 남았습니다");
	}

	@Override
	protected void skill(NewPlayer player) {
		this.hidePlayer(player);
		this.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, player.getLocation().add(0, 1, 0), 1000, 0.3, 0.3, 0.3, 0.02);
		
		Scheduler.delayScheduler(new RunnableEx() {

			@Override
			public void function() {
				player.sendMessage("§8[§f도적-SL§8] §f은신이 해제되었습니다");
				showPlayer(player);
			}
			
		}, 140);
	}

	@Override
	public double getMana() {
		return 100;
	}

	@Override
	public double getCoolTime() {
		return 25;
	}

	@Override
	public String getName() {
		return "도적쉬좌";
	}

}
