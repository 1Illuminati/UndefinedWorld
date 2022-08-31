package undefinedWorld.world;

import java.util.Random;

import javax.annotation.Nonnull;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;

public class EmptyChunkGenerator extends ChunkGenerator {

    @Override
    @Nonnull
    public ChunkData generateChunkData(@Nonnull World world, @Nonnull Random random, int x, int z, @Nonnull BiomeGrid biome) {
    	if(x == 0 && z ==0) {
    		ChunkData chunkData = createChunkData(world);
    		for(int locX = 0; locX < 16; locX++) {
    			for(int locZ = 0; locZ < 16; locZ++) {
    				chunkData.setBlock(locX, 0, locZ, Material.STONE);
        		}
    		}
    		return chunkData;
    	}
    	
        return createChunkData(world);
    }
}
