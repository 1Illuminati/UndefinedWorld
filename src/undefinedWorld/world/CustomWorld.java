package undefinedWorld.world;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;

public class CustomWorld {
	public static void makeWorld(int chunkSize, String name) {
		WorldCreator worldCreater = new WorldCreator(name);
		worldCreater.type(WorldType.FLAT);
		worldCreater.generator(new EmptyChunkGenerator());
		worldCreater.generateStructures(false);
		
		World world = Bukkit.createWorld(worldCreater);
		WorldBorder worldBorder = world.getWorldBorder();
		worldBorder.setSize(chunkSize * 16);
	}
	
	
}
