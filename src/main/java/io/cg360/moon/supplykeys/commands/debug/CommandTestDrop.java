package io.cg360.moon.supplykeys.commands.debug;

import io.cg360.moon.supplykeys.Supplykeys;
import io.cg360.moon.supplykeys.entities.SupplyCrate;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.util.Optional;

public class CommandTestDrop implements CommandExecutor {


    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {

        if(!(src instanceof Player)){
            return CommandResult.success();
        }

        Optional<Boolean> a =  args.getOne(Text.of("Announce Player"));
        boolean attachToOwner = false;

        if(a.isPresent()) {
            attachToOwner = a.get();
        }

        Player p = (Player) src;

        SupplyCrate crate = new SupplyCrate(
                Supplykeys.getSKPlugin().getPoolManager().getPool("default_pool").get(),
                p.getLocation(),
                attachToOwner ? p : null,
                attachToOwner ? p.getUniqueId() : null,
                null,
                0.1f,
                3
        );

        crate.initialize();

        return CommandResult.success();
    }
}
