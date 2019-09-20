package io.cg360.moon.supplykeys;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.*;

public class Utils {

    public static String pickRandomFromList(String[] list){
         Random random = new Random();
         return list[random.nextInt(list.length)];
    }

    public static void givePlayerItem(UUID uuid, ItemStackSnapshot snapshot){
        if(uuid == null) return;
        if(snapshot == null) return;
        Optional<UserStorageService> userStorage = Sponge.getServiceManager().provide(UserStorageService.class);
        Optional<User> u =  userStorage.get().get(uuid);
        if(!u.isPresent()){
            Supplykeys.getSKPlugin().getLogger().error("Player @ "+uuid.toString()+" doesn't exist");
            return;
        }

        User user = u.get();

        if(user.isOnline()){
            Optional<Player> p = user.getPlayer();
            if(!p.isPresent()){
                Supplykeys.getSKPlugin().getLogger().error("Player @ "+uuid.toString()+" is online but doesn't exist");
                return;
            }
            Player player = p.get();
            InventoryTransactionResult r = player.getInventory().offer(snapshot.createStack());
            if(r.getType() != InventoryTransactionResult.Type.SUCCESS){
                Supplykeys.getSKPlugin().getLogger().error("Player @ "+player.getName()+" had a full inventory. Replacing offhand");
                player.setItemInHand(HandTypes.OFF_HAND, snapshot.createStack());
            }
        } else {
            InventoryTransactionResult r = user.getInventory().offer(snapshot.createStack());
            if(r.getType() != InventoryTransactionResult.Type.SUCCESS){
                Supplykeys.getSKPlugin().getLogger().error("Player @ "+uuid.toString()+" had a full inventory. Replacing offhand");
                user.setItemInHand(HandTypes.OFF_HAND, snapshot.createStack());
            }
        }

    }

    public static void messageToWorld(World world, Text text){
        for(Player player:world.getPlayers()){
            player.sendMessage(text);
        }
    }

    public static void soundToWorld(World world, SoundType type, double volume){
        for(Player player:world.getPlayers()){
            player.playSound(type, player.getPosition(), volume);
        }
    }

    public static String generateLocationID(Location<World> loc){
        return String.format("%s:%f,%f,%f", loc.getExtent(), Math.floor(loc.getX()), Math.floor(loc.getY()), Math.floor(loc.getZ()));
    }

    public static void fillInventory(Inventory inventory, ItemStack[] items){
        Iterator<Inventory> in = inventory.slots().iterator();
        int iterate = 0;

        for(ItemStack is:items) {
            if(in.hasNext()){
                InventoryTransactionResult t = in.next().set(is);
            }
        }
    }

    public static Object[] parseToSpongeString(String string){
        ArrayList<Object> objs = new ArrayList<Object>();
        String[] codes = string.split("&");
        objs.add(codes[0]);
        for(int i = 1; i < codes.length; i++){
            if(codes[i] == null) continue;
            if(codes[i].equals("")) continue;
            switch (codes[i].toLowerCase().charAt(0)){
                case '0':
                    objs.add(TextColors.BLACK);
                    objs.add(codes[i].substring(1));
                    break;
                case '1':
                    objs.add(TextColors.DARK_BLUE);
                    objs.add(codes[i].substring(1));
                    break;
                case '2':
                    objs.add(TextColors.DARK_GREEN);
                    objs.add(codes[i].substring(1));
                    break;
                case '3':
                    objs.add(TextColors.DARK_AQUA);
                    objs.add(codes[i].substring(1));
                    break;
                case '4':
                    objs.add(TextColors.DARK_RED);
                    objs.add(codes[i].substring(1));
                    break;
                case '5':
                    objs.add(TextColors.DARK_PURPLE);
                    objs.add(codes[i].substring(1));
                    break;
                case '6':
                    objs.add(TextColors.GOLD);
                    objs.add(codes[i].substring(1));
                    break;
                case '7':
                    objs.add(TextColors.GRAY);
                    objs.add(codes[i].substring(1));
                    break;
                case '8':
                    objs.add(TextColors.DARK_GRAY);
                    objs.add(codes[i].substring(1));
                    break;
                case '9':
                    objs.add(TextColors.BLUE);
                    objs.add(codes[i].substring(1));
                    break;
                case 'a':
                    objs.add(TextColors.GREEN);
                    objs.add(codes[i].substring(1));
                    break;
                case 'b':
                    objs.add(TextColors.AQUA);
                    objs.add(codes[i].substring(1));
                    break;
                case 'c':
                    objs.add(TextColors.RED);
                    objs.add(codes[i].substring(1));
                    break;
                case 'd':
                    objs.add(TextColors.LIGHT_PURPLE);
                    objs.add(codes[i].substring(1));
                    break;
                case 'e':
                    objs.add(TextColors.YELLOW);
                    objs.add(codes[i].substring(1));
                    break;
                case 'f':
                    objs.add(TextColors.WHITE);
                    objs.add(codes[i].substring(1));
                    break;
                case 'k':
                    objs.add(TextStyles.OBFUSCATED);
                    objs.add(codes[i].substring(1));
                    break;
                case 'l':
                    objs.add(TextStyles.BOLD);
                    objs.add(codes[i].substring(1));
                    break;
                case 'm':
                    objs.add(TextStyles.STRIKETHROUGH);
                    objs.add(codes[i].substring(1));
                    break;
                case 'n':
                    objs.add(TextStyles.UNDERLINE);
                    objs.add(codes[i].substring(1));
                    break;
                case 'o':
                    objs.add(TextStyles.ITALIC);
                    objs.add(codes[i].substring(1));
                    break;
                case 'r':
                    objs.add(TextStyles.RESET);
                    objs.add(TextColors.RESET);
                    objs.add(codes[i].substring(1));
                    break;
                case 'x':
                    objs.add(String.valueOf(new Random().nextInt(1000000000)));
                    objs.add(codes[i].substring(1));
                    break;
                default:
                    objs.add(codes[i]);
                    break;
            }
        }
        return objs.toArray();
    }


}
