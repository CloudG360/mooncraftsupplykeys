package io.cg360.moon.supplykeys.tasks.drop;

import io.cg360.moon.supplykeys.Supplykeys;
import io.cg360.moon.supplykeys.Utils;
import io.cg360.moon.supplykeys.entities.items.SerializableItem;
import io.cg360.moon.supplykeys.entities.loot.SupplyLoot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.block.tileentity.TileEntityTypes;
import org.spongepowered.api.block.tileentity.carrier.TileEntityCarrier;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;
import java.util.UUID;

public class RunnableGenerateDrop implements Runnable {

    private Location<World> origin;
    private SupplyLoot loot;
    private UUID ownerid;
    private SerializableItem key;

    public RunnableGenerateDrop(Location<World> origin, SupplyLoot loot, UUID ownerid, SerializableItem key) {
        this.origin = origin;
        this.loot = loot;
        this.ownerid = ownerid;
        this.key = key;
    }

    @Override
    public void run() {
        ItemStackSnapshot snapshot = key == null ? null : key.generateStack(1).createSnapshot();
        origin.getExtent().loadChunk(origin.getChunkPosition(), true);
        Optional<Chunk> chunk = origin.getExtent().getChunk(origin.getChunkPosition());
        if(!chunk.isPresent()){
            Supplykeys.getSKPlugin().getLogger().error("Chunk not loaded @ "+origin.toString()+ " | !! Supply Drop Failed !!");
            Utils.messageToWorld(origin.getExtent(), Text.of(TextColors.DARK_RED, TextStyles.BOLD, "DROP ", TextStyles.RESET, TextColors.RED, String.format("Drop cancelled at %d, %d, %d (Error, chunk unloaded)", (int)Math.floor(origin.getX()),(int)Math.floor(origin.getY()),(int)Math.floor(origin.getZ()))));
            Utils.givePlayerItem(ownerid, snapshot);
            return;
        }
        origin.setBlock(BlockTypes.CHEST.getDefaultState());
        origin.offer(Keys.DISPLAY_NAME, loot.getTitleText());

        Optional<TileEntity> tt = origin.getTileEntity();

        if(!tt.isPresent()){
            Supplykeys.getSKPlugin().getLogger().error("Tile entity chest not found #1 @ "+origin.toString()+ " | !! Supply Drop Failed !!");
            Utils.messageToWorld(origin.getExtent(), Text.of(TextColors.DARK_RED, TextStyles.BOLD, "DROP ", TextStyles.RESET, TextColors.RED, String.format("Drop cancelled at %d, %d, %d (Error, chest could not be placed)", (int)Math.floor(origin.getX()),(int)Math.floor(origin.getY()),(int)Math.floor(origin.getZ()))));
            Utils.givePlayerItem(ownerid, snapshot);
            return;
        }
        TileEntity t = tt.get();
        if(t.getType() == TileEntityTypes.CHEST){
            TileEntityCarrier c = (TileEntityCarrier) t;
            Inventory i = c.getInventory();
            ItemStack[] items;
            try {
                items = loot.rollLootPool();
            } catch (Exception err){
                Supplykeys.getSKPlugin().getLogger().error(err.getMessage());
                origin.setBlockType(BlockTypes.AIR);
                Utils.messageToWorld(origin.getExtent(), Text.of(TextColors.DARK_RED, TextStyles.BOLD, "DROP ", TextStyles.RESET, TextColors.RED, String.format("Drop cancelled at %d, %d, %d (Unknown Error)", (int)Math.floor(origin.getX()),(int)Math.floor(origin.getY()),(int)Math.floor(origin.getZ()))));
                Utils.givePlayerItem(ownerid, snapshot);
                return;
            }
            Utils.fillInventory(i, items);
            Utils.messageToWorld(origin.getExtent(), Text.of(TextColors.GREEN, TextStyles.BOLD, "DROP ", TextStyles.RESET, TextColors.GREEN, String.format("Supply Drop landed at %d, %d, %d", (int)Math.floor(origin.getX()),(int)Math.floor(origin.getY()),(int)Math.floor(origin.getZ()))));
        } else {
            Supplykeys.getSKPlugin().getLogger().error("Tile entity chest not found #2 @ "+origin.toString()+ " | !! Supply Drop Failed !!");
            Utils.messageToWorld(origin.getExtent(), Text.of(TextColors.DARK_RED, TextStyles.BOLD, "DROP ", TextStyles.RESET, TextColors.RED, String.format("Drop cancelled at %d, %d, %d (Error, chest could not be placed)", (int)Math.floor(origin.getX()),(int)Math.floor(origin.getY()),(int)Math.floor(origin.getZ()))));
            Utils.givePlayerItem(ownerid, snapshot);
        }
    }
}
