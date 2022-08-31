package undefinedWorld.entity.player.job;

import java.util.HashMap;

import doublePlugin.entity.player.NewPlayer;
import undefinedWorld.skill.AbstractSkill;

public abstract class AbstractJob {
	protected static final String SKILL1 = "skill1";
	protected static final String SKILL2 = "skill2";
	protected static final String SKILL3 = "skill3";
	protected static final String SKILL4 = "skill4";
	protected static final String SKILL5 = "skill5";
	protected NewPlayer player;
	
	public AbstractJob(NewPlayer player) {
		this.player = player;
	}
	
	public abstract String getName();
	
	public abstract HashMap<Integer, AbstractSkill> skillMap();
	
	public AbstractSkill skill1() {
		return AbstractSkill.getSkill(player.getStringValue(SKILL1));
	}
	public AbstractSkill skill2() {
		return AbstractSkill.getSkill(player.getStringValue(SKILL2));
	}
	public AbstractSkill skill3() {
		return AbstractSkill.getSkill(player.getStringValue(SKILL3));
	}
	public AbstractSkill skill4() {
		return AbstractSkill.getSkill(player.getStringValue(SKILL4));
	}
	public AbstractSkill skill5() {
		return AbstractSkill.getSkill(player.getStringValue(SKILL5));
	}
}
