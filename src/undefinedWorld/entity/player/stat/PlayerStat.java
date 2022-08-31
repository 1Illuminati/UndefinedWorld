package undefinedWorld.entity.player.stat;

import doublePlugin.entity.player.NewPlayer;

public class PlayerStat {
	NewPlayer player;
	
	public PlayerStat(NewPlayer player) {
		this.player = player;
	}
	
	public void addStat(Stats stat, int value) {
		player.addIntegerValue(stat.getName(), value);
	}
	
	public void setStat(Stats stat, int value) {
		player.setIntegerValue(stat.getName(), value);
	}
	
	public int getStat(Stats stat) {
		return player.getIntegerValue(stat.getName());
	}
	
	public void clearStat() {
		int allPoint = 0;
		
		for(Stats stat : Stats.values()) {
			allPoint += getStat(stat);
			setStat(stat, 0);
		}
		
		setStat(Stats.STATPOINT, allPoint);
	}
}
