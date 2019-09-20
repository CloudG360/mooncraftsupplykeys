package io.cg360.moon.supplykeys.tasks.random;

import io.cg360.moon.supplykeys.Supplykeys;
import io.cg360.moon.supplykeys.entities.SupplyCrate;
import io.cg360.moon.supplykeys.entities.SupplykeysConfiguration;
import io.cg360.moon.supplykeys.entities.loot.SupplyLoot;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.DimensionTypes;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;

public class RunnableRandomDropHandler implements Consumer<Task> {

    private String supplyDropID;

    public RunnableRandomDropHandler(String supplyDropID) {
        this.supplyDropID = supplyDropID;
    }

    @Override
    public void accept(Task task) {
        Optional<SupplyLoot> loot = Supplykeys.getSKPlugin().getPoolManager().getPool(supplyDropID);
        if(!loot.isPresent()){
            task.cancel();
            return;
        }
        boolean re = false;
        int x = 0;
        int z = 0;
        Player p = getRandomPlayer();
        while (!p.getWorld().getDimension().getType().equals(DimensionTypes.OVERWORLD)){
            p = getRandomPlayer();
        }
        World w = p.getWorld();
        while(!re) {
            x = getSinglecoordxy(w);
            z = getSinglecoordxy(w);

            ResultCheckLocation result = new ResultCheckLocation(w, x, z);
            try {
                re = Sponge.getScheduler().createSyncExecutor(Supplykeys.getSKPlugin()).submit(result).get();
            } catch (Exception err){
                err.printStackTrace();
                return;
            }
        }
        int y = w.getHighestYAt(x,z);
        SupplykeysConfiguration c = Supplykeys.getSKPlugin().getPluginConfiguration();
        SupplyCrate crate = new SupplyCrate(loot.get(), new Location<>(w, x, y, z), null, null, null, c.getRandomDropInterval(), c.getRandomAnnounceInterval());
        boolean b = crate.initialize();
    }

    private Player getRandomPlayer(){
        Player[] p = Sponge.getServer().getOnlinePlayers().toArray(new Player[0]);
        Random r = new Random();
        int i = r.nextInt(p.length);
        return p[i];
    }

    private int getSinglecoordxy(World w){
        int d = (int) Math.floor(w.getWorldBorder().getDiameter());
        int d2 = (int) Math.floor(((double) d) / 2);

        Random r = new Random();
        int rand = r.nextInt(d);
        return rand - d2;
    }

}
