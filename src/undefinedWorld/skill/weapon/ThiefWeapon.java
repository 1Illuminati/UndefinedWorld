package undefinedWorld.skill.weapon;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import doublePlugin.entity.player.NewPlayer;
import doublePluginSkill.skill.Skill;
import doublePluginSkill.skill.item.SkillWeapon;

public class ThiefWeapon extends SkillWeapon {
	public final static String CODE = "도적무기";

	@Override
	public Skill dropItemSkill(NewPlayer arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Skill leftClickSkill(NewPlayer arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Skill rightClickSkill(NewPlayer player) {
		return Skill.getSkill("도적우클");
	}

	@Override
	public Skill shiftDropItemSkill(NewPlayer arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Skill shiftLeftClickSkill(NewPlayer player) {
		return Skill.getSkill("도적쉬좌");
	}

	@Override
	public Skill shiftRightClickSkill(NewPlayer arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Skill shiftSwapHandSkill(NewPlayer arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Skill swapHandSkill(NewPlayer arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean checkItem(ItemStack item) {
		if(item.getType() == Material.STONE_SWORD) {
			if(item.getItemMeta().hasCustomModelData()) {
				if(item.getItemMeta().getCustomModelData() == 2) {
					return true;
				}
			}
		}
		
		return false;
	}

	@Override
	public String getCode() {
		return CODE;
	}
	
}
