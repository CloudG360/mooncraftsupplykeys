package io.cg360.moon.supplykeys.commands.shop;

import io.cg360.moon.supplykeys.Supplykeys;
import io.cg360.moon.supplykeys.Utils;
import io.cg360.moon.supplykeys.entities.items.SerializableItem;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;

import java.util.Optional;

public class CommandGiveKey implements CommandExecutor {

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {

        Optional<String> pname = args.getOne(Text.of("player"));
        Optional<String> itemplate = args.getOne(Text.of("item template"));
        Optional<Integer> quantity = args.getOne(Text.of("quantity"));


        Optional<UserStorageService> userStorage = Sponge.getServiceManager().provide(UserStorageService.class);
        Optional<User> u =  userStorage.get().get(pname.get());

        Optional<SerializableItem> item = Supplykeys.getSKPlugin().getPoolManager().getItem(itemplate.get());

        if(!u.isPresent()){
            Supplykeys.getSKPlugin().getLogger().error(String.format("Player %s was not found when applying item '%s' x %s", pname.get(), itemplate.get(), quantity.get()) );
            throw new CommandException(Text.of("Player not found."));
        }
        if(!item.isPresent()){
            Supplykeys.getSKPlugin().getLogger().error(String.format("Player %s didn't recieve the item '%s' x %s as it didn't exist.", pname.get(), itemplate.get(), quantity.get()) );
            throw new CommandException(Text.of("Item not found."));
        }

        for(int i = 0; i < quantity.get(); i++)
        Utils.givePlayerItem(u.get().getUniqueId(), item.get().generateStack(1).createSnapshot());

        return CommandResult.success();
    }
}
