package com.rit.sucy;

import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

// Custom enchantment class (to be extended)
public abstract class CustomEnchantment {

    // Name of enchantment
    protected String enchantName;

    // The items that can be enchanted with the enchanting table to get this enchantment
    protected String[] naturalItems;

    // Constructor
    public CustomEnchantment(String name, String[] naturalItems) {
        this.enchantName = name;
        this.naturalItems = naturalItems;
    }

    // Calculates a level of enchantment based on the exp level used
    // Return less than 1 for no enchantment
    public int getEnchantmentLevel(int expLevel) {
        return 0;
    }

    // Checks if this enchantment can normally go on the provided item
    public boolean canEnchantOnto(ItemStack item) {
        for (String validItem : naturalItems) {
            if (item.getType().name().equalsIgnoreCase(validItem)) return true;
        }
        return false;
    }

    // Adds this enchantment to the given item
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

    // Apply the enchantment effect
    public abstract void applyEffect(LivingEntity user, LivingEntity target, int enchantLevel);
}
