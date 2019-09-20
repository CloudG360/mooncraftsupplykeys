package io.cg360.moon.supplykeys.entities.loot;

import io.cg360.moon.supplykeys.Supplykeys;
import io.cg360.moon.supplykeys.Utils;
import io.cg360.moon.supplykeys.entities.items.SerializableItem;
import io.cg360.moon.supplykeys.entities.items.SerializableItemEnchantment;
import io.cg360.moon.supplykeys.entities.items.SerializableItemKeys;
import io.cg360.moon.supplykeys.exceptions.MalformedLootPoolException;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;

import java.util.*;

public class SupplyLoot {

    private String id;
    private String title;
    private SupplyRoll[] lootpool;

    private transient ItemStack[] lastInventory; // Create air item stacks for gaps. Must have a length of 27.

    public ItemStack[] rollLootPool(){
        if(id == null) throw new MalformedLootPoolException("A Loot Pool was registered without an id. Check all Pool files to make sure they have their ID listed in the file.");
        if(lootpool == null) throw new MalformedLootPoolException("Loot Pool @ "+id+"missing content.");
        List<SupplyRoll> currentpool = new ArrayList<>();
        List<SupplyRoll> requiredRolls = new ArrayList<>();

        List<SupplyRoll> inventoryPrs = new ArrayList<>();

        // Populate

        boolean fillerPresent = false;

        for(SupplyRoll roll : lootpool){
            roll.verifyIntegrity();
            if(roll.getForcePresent()) requiredRolls.add(roll);
            if(roll.getItem().getId().equals("minecraft:air")) fillerPresent = true;
            for(int i = 0; i < roll.getWeight(); i++){
                currentpool.add(roll);
            }

        }

        if(!fillerPresent) {
            // Populates the pool with a filler with a default weight of 20;
            Supplykeys.getSKPlugin().getLogger().warn("Loot Pool @ "+id+" was missing a filler (minecraft:air) entry. Using default with a weight of 20");
            SupplyRoll roll = new SupplyRoll();
            roll.setDefaults();
            for(int i = 0; i < roll.getWeight(); i++){
                currentpool.add(roll);
            }
        }

        if(requiredRolls.size() > 27) {

            Supplykeys.getSKPlugin().getLogger().warn("Loot Pool @ "+id+" | Pool has more than 27 required item stacks. Purging any extra items from the pool.");
            Iterator<SupplyRoll> r = requiredRolls.iterator();
            for(int i = 0; i < 27; i++) r.next();
            while (r.hasNext()) {
                SupplyRoll roll = r.next();
                requiredRolls.remove(roll);
            }

            inventoryPrs.addAll(requiredRolls);

        } else {
            for(SupplyRoll roll: requiredRolls){ addRolls(roll, currentpool, inventoryPrs); }
            Random random = new Random();
            for(int i = requiredRolls.size(); i < 27; i++){
                SupplyRoll roll = currentpool.get(random.nextInt(currentpool.size()));
                addRolls(roll, currentpool, inventoryPrs);
            }
        }
        List<ItemStack> items = new ArrayList<>();
        for(SupplyRoll r: inventoryPrs){
            ItemStack itemStack = r.getItemStack();
            items.add(itemStack);
        }
        Collections.shuffle(items);
        ItemStack[] itemSt = items.toArray(new ItemStack[0]);
        lastInventory = itemSt;
        return itemSt;
    }

    private void addRolls(SupplyRoll roll, List<SupplyRoll> pool, List<SupplyRoll> inventory){
        inventory.add(roll);
        List<SupplyRoll> rollremove = new ArrayList<>();
        rollremove.add(roll);
        int i = 0;
        for(SupplyRoll r : inventory){
            if (r == roll) i++;
        }
        if(i >= roll.getMaxAmount()){
            pool.removeAll(rollremove);
        }
    }

    public SupplyLoot setToDefault(){
        this.id = "default_pool";
        this.title = "&7&lCrate";
        List<SupplyRoll> dr = new ArrayList<>();
        dr.add(new SupplyRoll().setConfig( 100, 1, 1, false, 27,
                new SerializableItem().setConfig(null, "minecraft:air", null, null, null,null)));
        dr.add(new SupplyRoll().setConfig( 40, 27, 48, false, 15,
                new SerializableItem().setConfig(null,"minecraft:dirt", null, null, null,null)));
        dr.add(new SupplyRoll().setConfig(70, 1, 64, false, 15,
                new SerializableItem().setConfig(null,"minecraft:stone", "&c&lShiny rock", null, null,null)));
        dr.add(new SupplyRoll().setConfig( 1, 1, 1, true, 20,
                new SerializableItem().setConfig(null,"minecraft:web", null, null, new SerializableItemKeys().setConfig(null, true, false), new SerializableItemEnchantment[]{new SerializableItemEnchantment().setConfig("minecraft:fire_aspect", 7)})));
        this.lootpool = dr.toArray(new SupplyRoll[0]);
        return this;
    }

    public ItemStack[] getLastInventory() { return lastInventory; }
    public SupplyRoll[] getLootpool() { return lootpool; }
    public String getId() { return id; }
    public String getTitle() {
        if(title == null) title = "&7&lCrate";
        return title;
    }
    public Text getTitleText() { return Text.of(Utils.parseToSpongeString(getTitle())); }
}
