package io.cg360.moon.supplykeys.entities.items;

import io.cg360.moon.supplykeys.Supplykeys;
import io.cg360.moon.supplykeys.Utils;
import io.cg360.moon.supplykeys.exceptions.InvalidItemException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.item.EnchantmentData;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.enchantment.EnchantmentType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SerializableItem {

    private String uid;
    private String id;

    private String display_name;
    private String[] lore;

    private SerializableItemKeys keys;
    private SerializableItemEnchantment[] enchantments;

    public SerializableItem setConfig(String uid, String id, String display_name, String[] lore, SerializableItemKeys keys, SerializableItemEnchantment[] enchantments) {
        this.uid = uid;
        this.id = id;
        this.display_name = display_name;
        this.lore = lore;
        this.keys = keys;
        this.enchantments = enchantments;
        this.verifyIntegrity();
        return this;
    }

    public SerializableItem setDefault(){
        this.uid = "server:default_item";
        this.id = "minecraft:paper";
        this.display_name = "IGNORE";
        this.lore = new String[] {"IGNORE"};
        this.keys = new SerializableItemKeys().setEmpty();
        this.enchantments = new SerializableItemEnchantment[] { new SerializableItemEnchantment().setEmpty() };
        this.verifyIntegrity();
        return this;
    }

    public void verifyIntegrity(){
        if(uid == null) this.uid = "DEFAULT";
        if(id == null) this.id = "minecraft:paper";
        if(!getType().isPresent()) throw new InvalidItemException(InvalidItemException.ExceptionType.ITEM_ID, id, "Invalid id specified in Supply roll of a loot table");

        if (display_name == null) this.display_name = "IGNORE";
        if (lore == null) this.lore = new String[] {"IGNORE"};
        if (keys == null) this.keys = new SerializableItemKeys().setEmpty();
        if (enchantments == null) this.enchantments = new SerializableItemEnchantment[] { new SerializableItemEnchantment().setEmpty() };
    }

    public ItemStack generateStack(int quantity){
        verifyIntegrity();
        if(!getType().isPresent()) throw new InvalidItemException(InvalidItemException.ExceptionType.ITEM_ID, id, "Invalid id specified in Supply roll of a loot table");
        ItemStack stack = ItemStack.builder().itemType(getType().get()).quantity(quantity).build();
        if(!display_name.equals("IGNORE")) stack.offer(Keys.DISPLAY_NAME, Text.of(Utils.parseToSpongeString(display_name)));
        if(!lore[0].equals("IGNORE")) {
            List<Text> loretext = new ArrayList<>();
            for(String ln: lore){
                loretext.add(Text.of(Utils.parseToSpongeString(ln)));
            }
            stack.offer(Keys.ITEM_LORE, loretext);
        }
        if(!enchantments[0].getId().equals("IGNORE")){
            EnchantmentData data = stack.getOrCreate(EnchantmentData.class).get();
            for(SerializableItemEnchantment e : enchantments) {
                Optional<EnchantmentType> type = Sponge.getRegistry().getType(EnchantmentType.class, e.getId());
                if(!type.isPresent()){
                    Supplykeys.getSKPlugin().getLogger().warn("Malformed enchantment ID: "+e.getId());
                    continue;
                }
                data.set(data.enchantments().add(Enchantment.of(type.get(), e.getLevel())));
            }
            stack.offer(data);
        }

        return keys.applyKeys(stack);
    }

    public Optional<ItemType> getType () { return Sponge.getRegistry().getType(ItemType.class, id); }
    public String getUniqueID() { return uid; }
    public String getId() { return id; }
    public String getDisplayName() { return display_name; }
    public String[] getLore() { return lore; }
    public SerializableItemEnchantment[] getEnchantments() { return enchantments; }
}
