package io.cg360.moon.supplykeys.commands.debug;

import com.google.gson.Gson;
import io.cg360.moon.supplykeys.entities.items.SerializableItem;
import io.cg360.moon.supplykeys.entities.loot.SupplyLoot;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class CommandWriteDefaults implements CommandExecutor {

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {

        SupplyLoot supplyLoot = new SupplyLoot();
        supplyLoot.setToDefault();

        SerializableItem item = new SerializableItem();
        item.setDefault();

        Gson gson = new Gson();

        try {
            File f = new File("./generated_loottable.json");
            f.createNewFile();

            FileWriter w = new FileWriter(f);
            BufferedWriter write = new BufferedWriter(w);
            String string = gson.toJson(supplyLoot);
            write.write(string);
            write.close();
            src.sendMessage(Text.of(TextColors.GOLD, TextStyles.BOLD, "DEBUG ", TextStyles.RESET, "Created file at "+f.getAbsolutePath()));

            File fi = new File("./generated_item.json");
            fi.createNewFile();

            FileWriter wi = new FileWriter(fi);
            BufferedWriter writei = new BufferedWriter(wi);
            String stringi = gson.toJson(item);
            writei.write(stringi);
            writei.close();
            src.sendMessage(Text.of(TextColors.GOLD, TextStyles.BOLD, "DEBUG ", TextStyles.RESET, "Created file at "+fi.getAbsolutePath()));
        } catch (Exception err){
            err.printStackTrace();
        }

        return CommandResult.success();
    }
}
