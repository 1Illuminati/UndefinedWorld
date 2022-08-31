package undefinedWorld.entity.player.level;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import doublePlugin.item.MakeItem;

public class Level {
	private static final List<Integer> maxExpList = new ArrayList<>();
	private static final int MAX_LEVEL = 100;
	
	static {
		int repeatNum = 5, plus = 5;
		maxExpList.add(0);
        for(int i = 0; i < (MAX_LEVEL / 5); i++) {
            for(int j = 0; j < 5; j++) {
                int level = i * 5 + j + 1;
                maxExpList.add(repeatNum * level * 5);
                repeatNum += plus;
            }
            plus +=5;
        }
	}
	
	public static int getMaxExp(int level) {
		return maxExpList.get(level);
	}
	
	public static ItemStack getExpBook(int exp) {
		return MakeItem.makeItem("§5" + exp + " 경험치북", Material.BOOK, "§f우클릭시 경험치가 증가한다.");
	}
}
