package io.cg360.moon.supplykeys.entities;

import io.cg360.moon.supplykeys.Supplykeys;
import io.cg360.moon.supplykeys.Utils;
import io.cg360.moon.supplykeys.entities.items.SerializableItem;
import io.cg360.moon.supplykeys.entities.loot.SupplyLoot;
import io.cg360.moon.supplykeys.tasks.drop.RunnableCheckSupplyDrop;
import io.cg360.moon.supplykeys.tasks.drop.RunnableGenerateDrop;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;
import java.util.UUID;


public class SupplyCrate {

    private String id;
    private SupplyLoot lootpool;
    private Location<World> origin;
    private Player owner;
    private UUID ownerid;

    private SerializableItem key;

    private double increment;
    private int announceCount;

    private double completion;
    private int count;

    public SupplyCrate(SupplyLoot lootpool, Location<World> origin, Player owner, UUID ownerid, SerializableItem key, double increment, int announceCount) {
        this.lootpool = lootpool;
        this.origin = origin;
        this.owner = owner;
        this.ownerid = ownerid;

        this.key = key;

        this.increment = increment;
        this.announceCount = announceCount;

        this.completion = 0;
        this.count = 0;

    }

    public synchronized boolean process(){
        ItemStackSnapshot snapshot = key == null ? null : key.generateStack(1).createSnapshot();
        if(completion >= 1){
            Location<World> newOrigin = new Location<>(origin.getExtent(), Math.floor(origin.getX()), origin.getY(), (int) Math.floor(origin.getZ()));

            newOrigin.getExtent().loadChunk(newOrigin.getChunkPosition(), true);

            Optional<Chunk> chunk = newOrigin.getExtent().getChunk(newOrigin.getChunkPosition());

            int y = origin.getExtent().getHighestYAt((int) newOrigin.getX(), (int) newOrigin.getZ());
            if(y > 240){
                Utils.givePlayerItem(ownerid, snapshot);
                Supplykeys.getSKPlugin().getSupplyDropManager().unregisterSupplyDrop(id);
                Utils.messageToWorld(origin.getExtent(), Text.of(TextColors.DARK_RED, TextStyles.BOLD, "DROP ", TextStyles.RESET, TextColors.RED, String.format("Drop cancelled at %d, %d, %d (Too high in the world)", (int)Math.floor(origin.getX()),(int)Math.floor(origin.getY()),(int)Math.floor(origin.getZ()))));
                return true;
            }
            newOrigin = new Location<>(origin.getExtent(), newOrigin.getX(), y, newOrigin.getZ());

            RunnableGenerateDrop drop = new RunnableGenerateDrop(newOrigin, lootpool, ownerid, key);
            Sponge.getScheduler().createSyncExecutor(Supplykeys.getSKPlugin()).submit(drop);
            Supplykeys.getSKPlugin().getSupplyDropManager().unregisterSupplyDrop(id);
            return true;
        }

        if(count == announceCount){
            Utils.messageToWorld(origin.getExtent(), Text.of(TextColors.GREEN, TextStyles.BOLD, "DROP ", TextStyles.RESET, TextColors.GREEN, String.format("Supply Drop at %d, %d, %d is %s of the way there", (int)Math.floor(origin.getX()),(int)Math.floor(origin.getY()),(int)Math.floor(origin.getZ()), ((int) Math.floor(completion * 100))+"%")));
            count = 0;
        }
        count += 1;
        completion += increment;
        return false;

    }

    public boolean initialize(){
        id = Utils.generateLocationID(origin);
        if(Supplykeys.getSKPlugin().getSupplyDropManager().containsCrate(id)) return false;

        Supplykeys.getSKPlugin().getSupplyDropManager().registerSupplyDrop(this);
        if (owner == null) {
            Utils.messageToWorld(origin.getExtent(), Text.of(TextColors.GREEN, TextStyles.BOLD, "DROP ", TextStyles.RESET, TextColors.GREEN, "A Supply Drop of a ", lootpool.getTitleText(), String.format(" is landing at %d, %d, %d", (int) Math.floor(origin.getX()), (int) Math.floor(origin.getY()), (int) Math.floor(origin.getZ()))));
        } else {
            Utils.messageToWorld(origin.getExtent(), Text.of(TextColors.GREEN, TextStyles.BOLD, "DROP ", TextStyles.RESET, TextColors.GREEN, String.format("%s has triggered a Supply drop of a ", owner.getName()), lootpool.getTitleText(), String.format(" at %d, %d, %d", (int) Math.floor(origin.getX()), (int) Math.floor(origin.getY()), (int) Math.floor(origin.getZ()))));
        }
        Utils.soundToWorld(origin.getExtent(), SoundTypes.ENTITY_WITHER_DEATH, 0.5);
        Task.builder().name("SKDROP | "+id).execute(new RunnableCheckSupplyDrop(id)).intervalTicks(40).submit(Supplykeys.getSKPlugin());
        return true;
    }

    public SupplyLoot getLootPool() { return lootpool; }
    public Location<World> getOrigin(){ return origin; }
    public double getCompletion() { return completion; }
}
