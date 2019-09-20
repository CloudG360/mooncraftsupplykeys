package io.cg360.moon.supplykeys.entities;

import io.cg360.moon.supplykeys.entities.loot.SupplyLoot;

public class SupplykeysConfiguration {

    private String[] random_drops;
    private Integer random_event_time; // In Minutes
    private Double random_drop_interval;
    private Integer random_announce_interval;

    private Double key_drop_interval;
    private Integer key_announce_interval;

    public void verifyIngereity(){
        if(random_drops == null) random_drops = new String[]{new SupplyLoot().setToDefault().getId() };
        if(random_event_time == null) random_event_time = 60;
        if(random_drop_interval == null) random_drop_interval = 0.001;
        if(random_announce_interval == null) random_announce_interval = 35;

        if(key_drop_interval == null) key_drop_interval = 0.005;
        if(key_announce_interval == null) key_announce_interval = 25;
    }

    public SupplykeysConfiguration setDefaults(){
        random_drops = new String[]{new SupplyLoot().setToDefault().getId() };
        random_event_time = 60;
        random_drop_interval = 0.001;
        random_announce_interval = 35;

        key_drop_interval = 0.005;
        key_announce_interval = 25;
        return this;
    }

    public String[] getRandomDrops() { return random_drops; }
    public Integer getRandomEventTime() { return random_event_time; }
    public Double getRandomDropInterval() { return random_drop_interval; }
    public Integer getRandomAnnounceInterval() { return random_announce_interval; }

    public Double getKeyDropInterval() { return key_drop_interval; }
    public Integer getKeyAnnounceInterval() { return key_announce_interval; }
}
