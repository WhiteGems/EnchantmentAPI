package com.rit.sucy;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

/**
 * Handles selecting enchantments when enchanting items
 */
class EEnchantTable {

    /**
     * Enchantment weights for vanilla enchantments
     */
    static final int[] WEIGHTS = new int[] { 10, 5, 5, 5, 2, 2, 2, 1, 10, 5, 5, 5, 2, 2, 10, 5, 2, 1, 10, 2, 2, 1 };

    /**
     * Maximum tries before the enchantment stops adding enchantments
     */
    static final int MAX_TRIES = 10;

    /**
     * Vanilla enchantments
     */
    static final Enchantment[] ENCHANTS = new Enchantment[] { Enchantment.PROTECTION_ENVIRONMENTAL,
        Enchantment.PROTECTION_FALL, Enchantment.PROTECTION_FIRE, Enchantment.PROTECTION_PROJECTILE,
        Enchantment.WATER_WORKER, Enchantment.PROTECTION_EXPLOSIONS, Enchantment.OXYGEN, Enchantment.THORNS,
        Enchantment.DAMAGE_ALL, Enchantment.DAMAGE_ARTHROPODS, Enchantment.KNOCKBACK, Enchantment.DAMAGE_UNDEAD,
        Enchantment.FIRE_ASPECT, Enchantment.LOOT_BONUS_MOBS, Enchantment.DIG_SPEED, Enchantment.DURABILITY,
        Enchantment.LOOT_BONUS_BLOCKS, Enchantment.SILK_TOUCH, Enchantment.ARROW_DAMAGE, Enchantment.ARROW_FIRE,
        Enchantment.ARROW_KNOCKBACK, Enchantment.ARROW_INFINITE };

    /**
     * Minimum levels for various tiers of vanilla enchantments
     */
    static final int[][] LEVELS = new int[][] {
            {1, 15, 25, 35}, {1, 15, 20, 25}, {1, 20, 30, 35}, {1, 15, 20, 25}, {1}, {1, 15, 25, 30},
            {1, 30, 40}, {1, 45, 65}, {1, 15, 25, 40, 50}, {1, 20, 25, 35, 50}, {1, 40}, {1, 20, 25, 35, 50},
            {1, 50}, {1, 30, 60}, {1, 30, 40, 50, 60}, {1, 30, 40}, {1, 40, 50}, {1}, {1, 15, 25, 35, 45},
            {1}, {1, 35}, {1}};

    /**
     * Enchants an item
     *
     * @param item         item to enchant
     * @param enchantLevel experience level used
     * @param event        event details
     * @return             the enchanted item
     */
    public static ItemStack enchant(ItemStack item, int enchantLevel, EnchantItemEvent event) {

        // Don't use the normal enchantments
        event.getEnchantsToAdd().clear();

        // Find the total weight of all applicable enchantments
        int totalWeight = vanillaWeight(item) + customWeight(item.getType().name());

        // Get a modified enchantment level (between 1 and 49)
        enchantLevel = modifiedLevel(enchantLevel, enchantability(item.getType().name()));

        boolean chooseEnchantment = true;
        ArrayList<Object> enchants = new ArrayList<Object>();
        ArrayList<Integer> levels = new ArrayList<Integer>();
        int level = 1;

        // Keep choosing enchantments as long as needed
        while (chooseEnchantment) {
            chooseEnchantment = false;

            // Choose an enchantment
            Object enchant = null;
            int tries = 0;
            do {
                double roll = Math.random() * totalWeight;
                int count = 0;
                for (CustomEnchantment c : EnchantmentAPI.getEnchantments()) {
                    if (c.canEnchantOnto(item)) {
                        count += c.weight;
                        if (count > roll) {
                            enchant = c;
                            level = c.getEnchantmentLevel(enchantLevel);
                            if (level < 1) level = 1;
                            break;
                        }
                    }
                }
                if (enchant == null) {
                    for (int i = 0; i < WEIGHTS.length; i++) {
                        boolean valid = false;
                        if (ENCHANTS[i].canEnchantItem(item) || item.getType() == Material.BOOK) valid = true;
                        if (valid) {
                            count += WEIGHTS[i];
                            if (count > roll) {
                                enchant = ENCHANTS[i];
                                level = getLevel(i, enchantLevel);
                                break;
                            }
                        }
                    }
                }
                tries++;
            }
            while(!validEnchant(enchant, enchants) && tries < MAX_TRIES);

            if (!validEnchant(enchant, enchants)) break;

            // Add the enchantment to the list
            enchants.add(enchant);
            levels.add(level);

            // Reduce the chance of getting another one along with the power of the next one
            enchantLevel /= 2;
            if (Math.random() < (enchantLevel + 1) / 50.0) chooseEnchantment = true;

            // Books can only have a single enchantment
            if (item.getType() == Material.BOOK) chooseEnchantment = false;
        }

        // Apply the enchantments
        for (Object o : enchants) {
            if (o == null) return item;
            else if (o instanceof CustomEnchantment) ((CustomEnchantment)o).addToItem(item, levels.get(enchants.indexOf(o)));
            else if (o instanceof Enchantment) item.addUnsafeEnchantment((Enchantment)o, levels.get(enchants.indexOf(o)));
        }

        return item;
    }

    /**
     * Gets the level of the vanilla enchantment with the given index
     *
     * @param index    enchantment index
     * @param expLevel modified exp level
     * @return         level of enchantment
     */
    static int getLevel(int index, int expLevel) {
        for (int i = LEVELS[index].length - 1; i >= 0; i--) {
            if (expLevel >= LEVELS[index][i]) return i + 1;
        }
        return 1;
    }

    /**
     * Checks if an enchantment is a valid addition to the current list
     *
     * @param enchant  enchantment to add
     * @param enchants current list of enchantments
     * @return         true if valid, false otherwise
     */
    static boolean validEnchant(Object enchant, ArrayList<Object> enchants) {
        if (enchants.contains(enchant)) return false;
        else if (enchant instanceof Enchantment) {
            Enchantment enchantment = (Enchantment)enchant;
            for (Object o : enchants) {
                if (o instanceof Enchantment) {
                    Enchantment e = (Enchantment)o;
                    if (e.getName().equalsIgnoreCase(enchantment.getName())) return false;
                    if (e.getName().contains("PROTECTION") && enchantment.getName().contains("PROTECTION")) return false;
                    if (e.getName().contains("SILK") && enchantment.getName().contains("LOOT")) return false;
                    if (e.getName().contains("LOOT") && enchantment.getName().contains("SILK")) return false;
                    if (e.getName().contains("DAMAGE") && enchantment.getName().contains("DAMAGE")) return false;
                }
            }
        }
        return true;
    }

    /**
     * Calcuates a modified experience level
     *
     * @param expLevel       chosen exp level
     * @param enchantability the enchantibility of the item
     * @return               modified exp level
     */
    static int modifiedLevel(int expLevel, int enchantability) {
        expLevel = expLevel + random(enchantability / 4 * 2) + 1;
        double bonus = random(0.3) + 0.85;
        return (int)(expLevel * bonus + 0.5);
    }

    /**
     * Chooses a random integer with triangular distribution
     *
     * @param max maximum value
     * @return    random integer
     */
    static int random(int max) {
        return (int)(Math.random() * max / 2 + Math.random() * max / 2);
    }

    /**
     * Chooses a random double with triangular distribution
     *
     * @param max maximum value
     * @return    random double
     */
    static double random(double max) {
        return Math.random() * max / 2 + Math.random() * max / 2;
    }

    /**
     * Gets the enchantability of an item
     *
     * @param itemName name of the item
     * @return         enchantability of the item
     */
    static int enchantability(String itemName) {
        itemName = itemName.toLowerCase();
        if (itemName.contains("wood_")) return 15;
        if (itemName.contains("leather_")) return 15;
        if (itemName.contains("stone_")) return 5;
        if (itemName.contains("iron_")) return 14;
        if (itemName.contains("chain")) return 12;
        if (itemName.contains("diamond_")) return 10;
        if (itemName.contains("gold_")) return 25;
        return 1;
    }

    /**
     * Gets the total weight of all custom enchantments applicable to the item
     *
     * @param itemName item name
     * @return         total custom enchantment weight
     */
    static int customWeight(String itemName) {
        int count = 0;
        for (CustomEnchantment enchantment : EnchantmentAPI.getEnchantments()) {
            for (String s : enchantment.naturalItems) {
                if (itemName.equalsIgnoreCase(s)) count += enchantment.weight;
            }
        }
        return count;
    }

    /**
     * Gets the total weight of all vanilla enchantments applicable to the item
     *
     * @param item item
     * @return     total vanilla enchantment weight
     */
    static int vanillaWeight(ItemStack item) {
        int count = 0;
        for (int i = 0; i < WEIGHTS.length; i++) {
            if (!ENCHANTS[i].canEnchantItem(item) && item.getType() != Material.BOOK) continue;
            count += WEIGHTS[i];
        }
        return count;
    }
}
