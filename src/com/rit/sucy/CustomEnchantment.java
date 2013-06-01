package com.rit.sucy;

import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for custom enchantments
 */
public abstract class CustomEnchantment {

    // Name of enchantment
    protected String enchantName;

    // The items that can be enchanted with the enchanting table to get this enchantment
    protected String[] naturalItems;

    /**
     * Creates a new custom enchantment with the given name that can be
     * enchanted onto the items using an enchantment table with names
     * given in the array.
     *
     * @param name         the unique name of the enchantment
     * @param naturalItems the names of items that can normally have this enchantment
     */
    public CustomEnchantment(String name, String[] naturalItems) {
        this.enchantName = name;
        this.naturalItems = naturalItems;
    }

    /**
     * Calculates an enchantment level for this enchantment depending
     * on the experience level the player used during an enchantment.
     *
     * @param  expLevel the experience level the player used
     * @return          returns the enchantment level; returns < 1 if the enchantment should not be applied
     */
    public int getEnchantmentLevel(int expLevel) {
        return 0;
    }

    /**
     * Checks if this enchantment can be normally applied to the item.
     *
     * @param  item the item to check for
     * @return      true if the enchantment can be normally applied, false otherwise
     */
    public boolean canEnchantOnto(ItemStack item) {
        for (String validItem : naturalItems) {
            if (item.getType().name().equalsIgnoreCase(validItem)) return true;
        }
        return false;
    }

    /**
     * Adds this enchantment onto the given item with the enchantment level provided
     *
     * @param  item         the item being enchanted
     * @param  enchantLevel the level of enchantment
     * @return              the enchanted item
     */
    public ItemStack addToItem(ItemStack item, int enchantLevel) {
        ItemMeta meta = item.getItemMeta();
        List<String> metaLore = meta.getLore() == null ? new ArrayList<String>() : meta.getLore();

        // Make sure the enchantment doesn't already exist on the item
        for (String lore : metaLore) {
            if (lore.contains(enchantName)) {

                // Confirm that the enchanting name is the same
                String loreName = ENameParser.parseName(lore);
                if (loreName == null) continue;
                if (!enchantName.equalsIgnoreCase(loreName)) continue;

                // Compare the enchantment levels
                String[] pieces = lore.split(" ");
                int level = ERomanNumeral.getValueOf(pieces[pieces.length - 1]);

                // Leave higher enchantments alone
                if (level >= enchantLevel) return item;

                // Replace lower enchantments
                else meta.getLore().remove(lore);
                break;
            }
        }

        // Add the enchantment
        metaLore.add(0, ChatColor.GRAY + enchantName + " " + ERomanNumeral.numeralOf(enchantLevel));
        meta.setLore(metaLore);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Applies the enchantment affect between the two combatants
     *
     * @param user         the entity that has the enchantment
     * @param target       the entity that was struck by the enchantment
     * @param enchantLevel the level of the used enchantment
     */
    public abstract void applyEffect(LivingEntity user, LivingEntity target, int enchantLevel);
}
