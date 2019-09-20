package io.cg360.moon.supplykeys.commands.admin;

import io.cg360.moon.supplykeys.Supplykeys;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

public class CommandReloadPools implements CommandExecutor {

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Supplykeys.getSKPlugin().resetPools();
        Supplykeys.getSKPlugin().resetItemTemplates();
        src.sendMessage(Text.of(TextColors.LIGHT_PURPLE, TextStyles.BOLD, "POOLS ", TextStyles.RESET, "Reloaded Loot Pools. Any changes should be applied."));
        src.sendMessage(Text.of(TextColors.AQUA, TextStyles.BOLD, "POOLS ", TextStyles.RESET, "Reloaded Custom Items. Any changes should be applied."));
        return CommandResult.success();
    }
}
