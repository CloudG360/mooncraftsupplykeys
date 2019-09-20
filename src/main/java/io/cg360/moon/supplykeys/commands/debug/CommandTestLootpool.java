package io.cg360.moon.supplykeys.commands.debug;

import io.cg360.moon.supplykeys.Supplykeys;
import io.cg360.moon.supplykeys.Utils;
import io.cg360.moon.supplykeys.entities.loot.SupplyLoot;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

public class CommandTestLootpool implements CommandExecutor {

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {

        if(src instanceof Player){

            Optional<String> a =  args.getOne(Text.of("Loot Pool"));

            SupplyLoot supplyLoot = new SupplyLoot().setToDefault();

            if(a.isPresent()) {
                Optional<SupplyLoot> p = Supplykeys.getSKPlugin().getPoolManager().getPool(a.get());
                if(p.isPresent()) supplyLoot = p.get();
            }

            Player player = (Player) src;
            Inventory i = Inventory.builder().of(InventoryArchetypes.CHEST)
                    .property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(Text.of("Debug Crate")))
                    .build(Supplykeys.getSKPlugin());
            ItemStack[] items = supplyLoot.rollLootPool();

            Utils.fillInventory(i, items);

            player.openInventory(i);
        } else {
            src.sendMessage(Text.of(TextColors.RED, "You're not a player. This command uses inventories."));
        }

        return CommandResult.success();
    }

}
