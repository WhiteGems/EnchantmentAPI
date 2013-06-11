package com.rit.sucy;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Diemex
 */
public class VanillaEnchantment extends CustomEnchantment
{
    /**
     * The Enchantment which this represents
     */
    Enchantment vanilla;
    /**
     * Map holding the weight for
     */
    Map<MaterialClass, Integer> weight;
    /**
     * Allow this enchantment to be more prevalent on certain Material types
     */
    Map<MaterialClass, Integer> enchantability;
    /**
     * Value at index is the required level for that level of the enchant
     */
    int [] expLevels;

    public VanillaEnchantment(Enchantment vanilla, int weight, int [] expLevels, String name) {
        super(name, new String []{}, weight); //we override the method
        this.vanilla = vanilla;
        this.expLevels = expLevels;

        this.weight = new HashMap<MaterialClass, Integer>();
        this.weight.put(MaterialClass.DEFAULT, weight);

        this.enchantability = new HashMap<MaterialClass, Integer>();
    }

    public Enchantment getVanillaEnchant() {
        return vanilla;
    }

    /**
     * Pipe the call to the Vanilla Enchant "API"
     *
     * @param  item the item to check for
     *
     * @return if can be applied
     */
    @Override
    public boolean canEnchantOnto(ItemStack item) {
        return vanilla.canEnchantItem(item) || item.getType() == Material.BOOK;
    }

    /**
     * Gets the level of the vanilla enchantment with the given index
     *
     * @param expLevel modified exp level
     * @return         level of enchantment
     */
    @Override
    public int getEnchantmentLevel(int expLevel) {
        for (int i = expLevels.length - 1; i >= 0; i--) {
            if (expLevel >= expLevels[i]) return i + 1;
        }
        return 1;
    }

    @Override
    public boolean conflictsWith(CustomEnchantment enchantment) {
        return  this.name() .equalsIgnoreCase       (enchantment.name()) ||
                this.name() .contains("PROTECTION") && enchantment.name().contains("PROTECTION") ||
                this.name() .contains("SILK")       && enchantment.name().contains("LOOT") ||
                this.name() .contains("LOOT")       && enchantment.name().contains("SILK") ||
                this.name() .contains("DAMAGE")     && enchantment.name().contains("DAMAGE") ;
    }
}
