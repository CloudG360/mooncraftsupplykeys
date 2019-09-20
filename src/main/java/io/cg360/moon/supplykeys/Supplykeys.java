package io.cg360.moon.supplykeys;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.inject.Inject;
import io.cg360.moon.supplykeys.commands.admin.CommandListCustomItems;
import io.cg360.moon.supplykeys.commands.admin.CommandListPools;
import io.cg360.moon.supplykeys.commands.admin.CommandReloadPools;
import io.cg360.moon.supplykeys.commands.debug.CommandTestDrop;
import io.cg360.moon.supplykeys.commands.debug.CommandTestLootpool;
import io.cg360.moon.supplykeys.commands.debug.CommandWriteDefaults;
import io.cg360.moon.supplykeys.commands.shop.CommandGiveKey;
import io.cg360.moon.supplykeys.entities.SupplyCrate;
import io.cg360.moon.supplykeys.entities.SupplykeysConfiguration;
import io.cg360.moon.supplykeys.entities.items.SerializableItem;
import io.cg360.moon.supplykeys.entities.loot.SupplyLoot;
import io.cg360.moon.supplykeys.exceptions.MalformedLootPoolException;
import io.cg360.moon.supplykeys.managers.LootPoolManager;
import io.cg360.moon.supplykeys.managers.SupplyDropManager;
import io.cg360.moon.supplykeys.tasks.random.RunnableRandomDropHandler;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.event.world.chunk.LoadChunkEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Plugin(
        id = "supplykeys",
        name = "Supplykeys",
        version = "1.0-SNAPSHOT",
        description = "Handles the supply drops for Mooncraft",
        authors = {
                "CloudGamer360"
        }
)
public class Supplykeys {

    private static Supplykeys skplugin;

    private LootPoolManager poolManager;
    private SupplyDropManager supplyDropManager;

    private SupplykeysConfiguration configuration;

    @Inject
    private Logger logger;

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        skplugin = this;
        poolManager = new LootPoolManager();
        supplyDropManager = new SupplyDropManager();

        resetPools();
        resetItemTemplates();

        loadConfig();

        CommandSpec testSupplyLoot = CommandSpec.builder()
                .description(Text.of("Debug command which shows loot table."))
                .arguments(
                        GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.string(Text.of("Loot Pool"))))
                )
                .permission("supplykeys.debug.loottest")
                .executor(new CommandTestLootpool())
                .build();
        CommandSpec testJsonExport = CommandSpec.builder()
                .description(Text.of("Debug command which exports the default json loottable + item."))
                .permission("supplykeys.debug.writedefaultjson")
                .executor(new CommandWriteDefaults())
                .build();
        CommandSpec testSupplyDrop = CommandSpec.builder()
                .description(Text.of("Spawns a supply drop to test using the default pool"))
                .arguments(
                        GenericArguments.onlyOne(GenericArguments.bool(Text.of("Announce Player")))
                )
                .permission("supplykeys.debug.testsupplydrop")
                .executor(new CommandTestDrop())
                .build();
        // --------------------------
        CommandSpec adminReloadPools = CommandSpec.builder()
                .description(Text.of("Reloads all pools. Can be used to update pools without resetting the server."))
                .permission("supplykeys.admin.reload")
                .executor(new CommandReloadPools())
                .build();
        CommandSpec adminListPools = CommandSpec.builder()
                .description(Text.of("Lists all active pools"))
                .permission("supplykeys.admin.list.pools")
                .executor(new CommandListPools())
                .build();
        CommandSpec adminListItems = CommandSpec.builder()
                .description(Text.of("Lists all active custom items"))
                .permission("supplykeys.admin.list.items")
                .executor(new CommandListCustomItems())
                .build();
        // --------------------------
        CommandSpec shopGiveKey = CommandSpec.builder()
                .description(Text.of("Gives a player a custom item (Designed for a key) "))
                .arguments(
                        GenericArguments.onlyOne(GenericArguments.string(Text.of("player"))),
                        GenericArguments.onlyOne(GenericArguments.string(Text.of("item template"))),
                        GenericArguments.onlyOne(GenericArguments.integer(Text.of("quantity")))
                )
                .permission("supplykeys.shop.items")
                .executor(new CommandGiveKey())
                .build();

        Sponge.getCommandManager().register(this, testSupplyLoot, "testlootpool");
        Sponge.getCommandManager().register(this, testJsonExport, "exportjsondefaults");
        Sponge.getCommandManager().register(this, testSupplyDrop, "testsupplydrop");
        Sponge.getCommandManager().register(this, adminReloadPools, "reloadpools");
        Sponge.getCommandManager().register(this, adminListPools, "listpools");
        Sponge.getCommandManager().register(this, adminListItems, "listitems");
        Sponge.getCommandManager().register(this, shopGiveKey, "givecustomitem");

        String s = Utils.pickRandomFromList(configuration.getRandomDrops());

        if(!getPoolManager().getPool(s).isPresent()) {
            s = new SupplyLoot().setToDefault().getId();
        }

        Task.builder().interval(configuration.getRandomEventTime(), TimeUnit.MINUTES).delay(configuration.getRandomEventTime(), TimeUnit.MINUTES).execute(new RunnableRandomDropHandler(s)).async().submit(this);
    }

    @Listener
    public void onChunkLoad(LoadChunkEvent event){
        /*
        Collection<Entity> entities = event.getTargetChunk().getEntities();
        for (Entity entity:entities){
            if(entity.getType().equals(EntityTypes.ARMOR_STAND)) {
                ArmorStand s = (ArmorStand) entity;
                Optional<Value<Boolean>> invis =  s.getValue(Keys.INVISIBLE);
                Optional<Value<Text>> name = s.getValue(Keys.DISPLAY_NAME);

                if(invis.isPresent() && name.isPresent()){
                    Value<Boolean> invisible = invis.get();
                    Value<Text> displayname = name.get();
                    if(invisible.exists() && displayname.exists()){
                        if(invisible.get()){

                        }
                    }
                }

            }
        }

         */

        //TODO: Implement recovery if the server starts having issues.
    }

    @Listener
    public void onItemUse(InteractItemEvent.Secondary event){
        ItemStackSnapshot s = event.getItemStack();
        Player p = event.getCause().last(Player.class).get();
        if(s.get(Keys.ITEM_LORE).isPresent()){
            List<Text> t = s.get(Keys.ITEM_LORE).get();
            String code = "";
            String keyid = "";
            try {
                if (t.get(0).toPlain().contains("drop-key")) {
                    code = t.get(1).toPlain();
                    keyid = t.get(2).toPlain();
                }
            } catch (Exception err){
                return;
            }
            Optional<SupplyLoot> loot = Supplykeys.getSKPlugin().getPoolManager().getPool(code);
            if(!loot.isPresent()){
                p.sendMessage(Text.of(TextColors.DARK_RED, TextStyles.BOLD, "ERROR ", TextStyles.RESET, TextColors.RED, String.format("It appears your key is broken or it no longer supported. send this to a dev %s-pool_missing", code)));
                return;
            }
            Optional<SerializableItem> i = Supplykeys.getSKPlugin().getPoolManager().getItem(keyid);
            if(!i.isPresent()) {
                p.sendMessage(Text.of(TextColors.DARK_RED, TextStyles.BOLD, "ERROR ", TextStyles.RESET, TextColors.RED, String.format("It appears your key is very broken? Ask a dev about %s", code)));
                return;
            }
            event.setCancelled(true);
            SupplyCrate crate = new SupplyCrate(loot.get(), p.getLocation(), p, p.getUniqueId(), i.get(), configuration.getKeyDropInterval(), configuration.getKeyAnnounceInterval());
            boolean result = crate.initialize();
            if(result){
                if(event.getItemStack().getQuantity() > 1) {
                    p.setItemInHand(event.getHandType(), ItemStack.builder().fromSnapshot(event.getItemStack()).quantity(event.getItemStack().getQuantity() - 1).build());
                } else {
                    p.setItemInHand(event.getHandType(), ItemStack.builder().itemType(ItemTypes.AIR).build());
                }
            }
        }
    }

    public void loadConfig(){
        File dir = new File("./config");
        if (!dir.isDirectory()) dir.mkdirs();

        File f = new File("./config/supplykeys.json");
        if(!f.exists()){
            configuration = new SupplykeysConfiguration().setDefaults();
            try {
                f.createNewFile();
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                String json = gson.toJson(new SupplykeysConfiguration().setDefaults());
                FileWriter w = new FileWriter(f);
                BufferedWriter write = new BufferedWriter(w);
                write.write(json);
                write.close();
            } catch (Exception err){
                getLogger().info("Unable to load config. Setting to defaults...");
            }
        }

        String json = "";
        // Stage: Read
        try {
            FileReader reader = new FileReader(f);
            BufferedReader r = new BufferedReader(reader);
            Iterator<String> i = r.lines().iterator();

            while(i.hasNext()){
                String next = i.next();
                json = json.concat(next);
            }
            r.close();
        } catch (Exception err){
            getLogger().info("An error occured while opening the config at "+f.getAbsolutePath());
            configuration = new SupplykeysConfiguration().setDefaults();
            err.printStackTrace();
            return;
        }
        // Stage: Parse

        Gson gson = new Gson();
        try {
            configuration = gson.fromJson(json, SupplykeysConfiguration.class);
            if(configuration == null) throw new MalformedLootPoolException("The config was empty? Actually write something in the json file.");
        } catch(JsonSyntaxException err){
            getLogger().info("Malformed json in config at "+f.getAbsolutePath());
            configuration = new SupplykeysConfiguration().setDefaults();
        } catch (Exception err){
            getLogger().info("An error occured while processing the config at "+f.getAbsolutePath());
            configuration = new SupplykeysConfiguration().setDefaults();
        }
    }

    public void resetPools(){
        getPoolManager().clearPoolDatabase();
        File lootDirectory = new File("./lootpools");
        if (!lootDirectory.isDirectory()) lootDirectory.mkdirs();

        File[] pools = lootDirectory.listFiles();

        if(!(pools == null)) {
            for (File pool : pools) {
                String json = "";
                // Stage: Read
                try {
                    FileReader reader = new FileReader(pool);
                    BufferedReader r = new BufferedReader(reader);
                    Iterator<String> i = r.lines().iterator();

                    while(i.hasNext()){
                        String next = i.next();
                        json = json.concat(next);
                    }
                    r.close();
                } catch (Exception err){
                    getLogger().info("An error occured while opening the loottable at "+pool.getAbsolutePath());
                    err.printStackTrace();
                    continue;
                }
                // Stage: Parse

                Gson gson = new Gson();
                SupplyLoot loot;
                try {
                    loot = gson.fromJson(json, SupplyLoot.class);
                    if(loot == null) throw new MalformedLootPoolException("The pool was empty? Actually write something in the json file.");
                } catch(JsonSyntaxException err){
                    getLogger().info("Malformed json in loottable at "+pool.getAbsolutePath());
                    continue;
                } catch (Exception err){
                    getLogger().info("An error occured while processing the loottable at "+pool.getAbsolutePath());
                    continue;
                }

                // Stage: Validate & Register

                try {
                    getPoolManager().registerLootPool(loot.getId(), loot);
                } catch (Exception err) {
                    err.printStackTrace();
                }

                //TODO: Validate rolls on register rather than on roll

            }
        }
    }

    public void resetItemTemplates(){
        getPoolManager().clearItemDatabase();
        File lootDirectory = new File("./itemtemplates");
        if (!lootDirectory.isDirectory()) lootDirectory.mkdirs();

        File[] pools = lootDirectory.listFiles();

        if(!(pools == null)) {
            for (File pool : pools) {
                String json = "";
                // Stage: Read
                try {
                    FileReader reader = new FileReader(pool);
                    BufferedReader r = new BufferedReader(reader);
                    Iterator<String> i = r.lines().iterator();

                    while(i.hasNext()){
                        String next = i.next();
                        json = json.concat(next);
                    }
                    r.close();
                } catch (Exception err){
                    getLogger().info("An error occured while opening the custom item at "+pool.getAbsolutePath());
                    err.printStackTrace();
                    continue;
                }
                // Stage: Parse

                Gson gson = new Gson();
                SerializableItem loot;
                try {
                    loot = gson.fromJson(json, SerializableItem.class);
                    if(loot == null) throw new MalformedLootPoolException("The custom item was empty? Actually write something in the json file.");
                } catch(JsonSyntaxException err){
                    getLogger().info("Malformed json in custom item at "+pool.getAbsolutePath());
                    continue;
                } catch (Exception err){
                    getLogger().info("An error occured while processing the custom item at "+pool.getAbsolutePath());
                    continue;
                }

                // Stage: Validate & Register

                try {
                    getPoolManager().registerCustomItem(loot.getUniqueID(), loot);
                } catch (Exception err) {
                    err.printStackTrace();
                }

            }
        }
    }

    public Logger getLogger() { return logger; }
    public LootPoolManager getPoolManager() { return poolManager; }
    public SupplyDropManager getSupplyDropManager() { return supplyDropManager; }
    public SupplykeysConfiguration getPluginConfiguration() { return configuration; }

    public static Supplykeys getSKPlugin() { return skplugin; }

}
