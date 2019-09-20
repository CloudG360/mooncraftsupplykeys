package io.cg360.moon.supplykeys.tasks.random;

import io.cg360.moon.supplykeys.Supplykeys;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

public class ResultCheckLocation implements Callable<Boolean> {


    private static List<BlockType> blacklist = new ArrayList<>();

    static {
        blacklist.add(BlockTypes.WATER);
        blacklist.add(BlockTypes.LAVA);
    }

    private World w;
    private int x;
    private int z;

    public ResultCheckLocation(World w, int x, int z) {
        this.w = w;
        this.x = x;
        this.z = z;
    }

    @Override
    public Boolean call() throws Exception {
        Supplykeys.getSKPlugin().getLogger().info("Call");
        w.loadChunk(new Location<World>(w, x, 64, z).getChunkPosition(), true);
        Optional<Chunk> c = w.getChunkAtBlock(x, 64, z);
        if(!c.isPresent()) {
            throw new NullPointerException("Chunk not found");
        }
        int y = w.getHighestYAt(x, z);

        return checkArea(x, y, z);
    }

    private boolean checkArea(int x, int y, int z){
        if(y-1 < 0){
            Supplykeys.getSKPlugin().getLogger().info("y = "+(y - 1));
            return false;
        }
        for(int xi = x-1; xi <= x+1; xi++){
            for(int yi = x-1; yi <= y+1; yi++){
                for(int zi = z-1; zi <= z+1; zi++){
                    BlockType t = w.getBlock(xi,yi,zi).getType();
                    if(blacklist.contains(t)) {
                        Supplykeys.getSKPlugin().getLogger().info("Found "+t.getName());
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
