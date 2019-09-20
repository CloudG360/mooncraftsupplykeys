package io.cg360.moon.supplykeys.entities.items;

public class SerializableItemEnchantment {

    private String id;
    private int level;

    public SerializableItemEnchantment setEmpty(){
        this.id = "IGNORE";
        this.level = 1;
        return this;
    }

    public SerializableItemEnchantment setConfig(String id, int level){
        this.id = id;
        this.level = level;
        this.verifyIntegrity();
        return this;
    }

    public void verifyIntegrity(){
        if (this.level < 1) {
            level = 1;
        }
    }

    @Override
    public String toString() {
        return id + " | " + level + " ";
    }

    public String getId() { return id; }
    public int getLevel() { return level; }
}
