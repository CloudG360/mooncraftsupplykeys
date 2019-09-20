package io.cg360.moon.supplykeys.managers;

import io.cg360.moon.supplykeys.entities.items.SerializableItem;
import io.cg360.moon.supplykeys.entities.loot.SupplyLoot;
import io.cg360.moon.supplykeys.exceptions.InvalidItemException;
import io.cg360.moon.supplykeys.exceptions.OverwriteDeniedException;

import java.util.HashMap;
import java.util.Optional;

public class LootPoolManager {

    private HashMap<String, SupplyLoot> lootpools;
    private HashMap<String, SerializableItem> customItems;


    public LootPoolManager(){
        this.lootpools = new HashMap<>();
        this.customItems = new HashMap<>();
        SupplyLoot loot = new SupplyLoot().setToDefault();
        this.lootpools.put(loot.getId().toLowerCase(), loot);
        SerializableItem item = new SerializableItem().setDefault();
        this.customItems.put(item.getUniqueID().toLowerCase(), item);
    }

    // --------------------

    public void registerLootPool(String id, SupplyLoot loot) throws OverwriteDeniedException {
        if(lootpools.containsKey(id.toLowerCase())){
            throw new OverwriteDeniedException(String.format("A loot table was registered with a duplicate id: %s", id.toLowerCase()));
        }
        lootpools.put(id.toLowerCase(), loot);
    }
    public void unregisterLootPool(String id){ lootpools.remove(id.toLowerCase()); }

    // --------------------

    public void registerCustomItem(String id, SerializableItem item) throws OverwriteDeniedException {
        item.verifyIntegrity();
        if(item.getId().equals("DEFAULT")){
            throw new InvalidItemException(InvalidItemException.ExceptionType.ITEM_ID, id, "No uid field was detected in json definition.");
        }
        if(customItems.containsKey(id.toLowerCase())){
            throw new OverwriteDeniedException(String.format("A custom item was registered with a duplicate id: %s", id.toLowerCase()));
        }
        customItems.put(id.toLowerCase(), item);
    }
    public void unregisterCustomItem(String id){ lootpools.remove(id.toLowerCase()); }

    // --------------------

    public void clearPoolDatabase(){
        lootpools.clear();
        SupplyLoot loot = new SupplyLoot().setToDefault();
        this.lootpools.put(loot.getId().toLowerCase(), loot);
    }

    public void clearItemDatabase(){
        customItems.clear();
        SerializableItem item = new SerializableItem().setDefault();
        this.customItems.put("default_custom", item);
    }

    // --------------------

    public Optional<SupplyLoot> getPool (String id){ return lootpools.containsKey(id.toLowerCase())? Optional.of(lootpools.get(id.toLowerCase())) : Optional.empty(); }
    public Optional<SerializableItem> getItem (String id){ return customItems.containsKey(id.toLowerCase())? Optional.of(customItems.get(id.toLowerCase())) : Optional.empty(); }
    public String[] getPools() {
        return lootpools.keySet().toArray(new String[0]);
    }
    public String[] getItems() {
        return customItems.keySet().toArray(new String[0]);
    }
}
