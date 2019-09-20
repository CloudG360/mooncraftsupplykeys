package io.cg360.moon.supplykeys.managers;

import io.cg360.moon.supplykeys.Utils;
import io.cg360.moon.supplykeys.entities.SupplyCrate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SupplyDropManager {

    // Location based String IDs;
    private Map<String, SupplyCrate> activeSupplyDrops;

    public SupplyDropManager (){
        this.activeSupplyDrops = new HashMap<>();
    }

    public String registerSupplyDrop(SupplyCrate crate){
        String id = Utils.generateLocationID(crate.getOrigin());
        activeSupplyDrops.put(id, crate);
        return id;
    }

    public void unregisterSupplyDrop(String id){
        activeSupplyDrops.remove(id);
    }

    public Optional<SupplyCrate> getActiveCrate(String id){
        return activeSupplyDrops.containsKey(id) ? Optional.of(activeSupplyDrops.get(id)) : Optional.empty();
    }

    public boolean containsCrate(String id){
        return activeSupplyDrops.containsKey(id);
    }


}
