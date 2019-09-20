package io.cg360.moon.supplykeys.entities.loot;

import io.cg360.moon.supplykeys.entities.items.SerializableItem;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.Arrays;
import java.util.Random;

public class SupplyRoll {

    private int weight = 5; //Example: 500 will put it in the loot pool 500 times. All get removed from the lootpool if it hits the max stack amount

    private int minStackAmount = 1;
    private int maxStackAmount = 1;

    private Boolean forcePresent = true; // Ensures there's always at least one stack
    private int maxAmount = 1;

    //TODO: Replace with entities.items.SerializableItem

    private SerializableItem item;

    public final void verifyIntegrity(){
        if(item == null){
            item = new SerializableItem().setDefault();
        } else {
            this.item.verifyIntegrity();
        }

        if(forcePresent == null) forcePresent = false;
        if(weight < 1) this.weight = 1;
        if (maxAmount > 27) this.maxAmount = 27;
        if(minStackAmount < 1) this.minStackAmount = 1;
        if(maxStackAmount > 64) this.maxStackAmount = 64;
    }

    public ItemStack getItemStack(){
        verifyIntegrity();

        Random r = new Random();
        int quantity = r.nextInt((maxStackAmount - minStackAmount) + 1 ) + minStackAmount;

        return item.generateStack(quantity);
    }

    public void copyFrom(SupplyRoll roll){
        this.weight = roll.getWeight();
        this.minStackAmount = roll.getMinStackAmount();
        this.maxStackAmount = roll.getMaxStackAmount();
        this.forcePresent = roll.getForcePresent();
        this.maxAmount = roll.getMaxAmount();
        this.item = roll.getItem();
        this.verifyIntegrity();
    }

    public void setDefaults(){
        this.weight = 20;
        this.minStackAmount = 1;
        this.maxStackAmount = 1;
        this.forcePresent = false;
        this.maxAmount = 27;
        this.item = new SerializableItem().setDefault();
        this.verifyIntegrity();
    }

    public SupplyRoll setConfig(int weight, int minStackAmount, int maxStackAmount, boolean forcePresent, int maxAmount, SerializableItem item){
        this.weight = weight;
        this.minStackAmount = minStackAmount;
        this.maxStackAmount = maxStackAmount;
        this.forcePresent = forcePresent;
        this.maxAmount = maxAmount;
        this.item = item;
        this.verifyIntegrity();
        return this;
    }

    @Override
    public String toString() {
        return item.getId() + " | " + weight + " | " + minStackAmount + " | " + maxStackAmount + " | " + forcePresent + " | " + maxAmount + " | " + item.getDisplayName() + " | " + Arrays.toString(item.getLore()) + " | "+ Arrays.toString(item.getEnchantments());
    }
    public int getWeight() { return weight; }
    public int getMinStackAmount() { return minStackAmount; }
    public int getMaxStackAmount() { return maxStackAmount; }
    public boolean getForcePresent() { return forcePresent; }
    public int getMaxAmount() { return maxAmount; }
    public SerializableItem getItem() { return item; }
}
