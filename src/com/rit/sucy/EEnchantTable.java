package com.rit.sucy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

class EEnchantTable {

    static final int[] WEIGHTS = new int[] { 10, 5, 5, 5, 2, 2, 2, 1, 10, 5, 5, 5, 2, 2, 10, 5, 2, 1, 10, 2, 2, 1 };
    static final Enchantment[] ENCHANTS = new Enchantment[] { Enchantment.PROTECTION_ENVIRONMENTAL,
        Enchantment.PROTECTION_FALL, Enchantment.PROTECTION_FIRE, Enchantment.PROTECTION_PROJECTILE,
        Enchantment.WATER_WORKER, Enchantment.PROTECTION_EXPLOSIONS, Enchantment.OXYGEN, Enchantment.THORNS,
        Enchantment.DAMAGE_ALL, Enchantment.DAMAGE_ARTHROPODS, Enchantment.KNOCKBACK, Enchantment.DAMAGE_UNDEAD,
        Enchantment.FIRE_ASPECT, Enchantment.LOOT_BONUS_MOBS, Enchantment.DIG_SPEED, Enchantment.DURABILITY,
        Enchantment.LOOT_BONUS_BLOCKS, Enchantment.SILK_TOUCH, Enchantment.ARROW_DAMAGE, Enchantment.ARROW_FIRE,
        Enchantment.ARROW_KNOCKBACK, Enchantment.ARROW_INFINITE };
    static final int[][] LEVELS = new int[][] {
            {1, 15, 25, 35}, {1, 15, 20, 25}, {1, 20, 30, 35}, {1, 15, 20, 25}, {1}, {1, 15, 25, 30},
            {1, 30, 40}, {1, 45, 65}, {1, 15, 25, 40, 50}, {1, 20, 25, 35, 50}, {1, 40}, {1, 20, 25, 35, 50},
            {1, 50}, {1, 30, 60}, {1, 30, 40, 50, 60}, {1, 30, 40}, {1, 40, 50}, {1}, {1, 15, 25, 35, 45},
            {1}, {1, 35}, {1}};

    public static ItemStack enchant(ItemStack item, int enchantLevel, EnchantItemEvent event) {
        event.getEnchantsToAdd().clear();
        int totalWeight = vanillaWeight(item) + customWeight(item.getType().name());
        enchantLevel = modifiedLevel(enchantLevel, enchantability(item.getType().name()));

        boolean chooseEnchantment = true;
        ArrayList<Object> enchants = new ArrayList<Object>();
        ArrayList<Integer> levels = new ArrayList<Integer>();
        int level = 1;
        while (chooseEnchantment) {
            chooseEnchantment = false;

            Object enchant = null;
            do {
                Bukkit.broadcastMessage("Enchant! " + enchants.size());
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
                        if (ENCHANTS[i].canEnchantItem(item)) valid = true;
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
            }
            while(!validEnchant(enchant, enchants));
            enchants.add(enchant);
            levels.add(level);

            enchantLevel /= 2;
            if (Math.random() < (enchantLevel + 1) / 50.0) chooseEnchantment = true;
            if (item.getType() == Material.BOOK) chooseEnchantment = false;
        }
        for (Object o : enchants) {
            if (o instanceof Enchantment) item.addEnchantment((Enchantment)o, levels.get(enchants.indexOf(o)));
            else ((CustomEnchantment)o).addToItem(item, levels.get(enchants.indexOf(o)));
        }
        return item;
    }

    static int getLevel(int index, int enchantLevel) {
        for (int i = LEVELS[index].length - 1; i >= 0; i--) {
            if (enchantLevel >= LEVELS[index][i]) return i + 1;
        }
        return 1;
    }

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

    static int modifiedLevel(int enchantLevel, int enchantability) {
        enchantLevel = enchantLevel + random(enchantability / 4 * 2) + 1;
        double bonus = random(0.3) + 0.85;
        return (int)(enchantLevel * bonus + 0.5);
    }

    static int random(int max) {
        return (int)(Math.random() * max / 2 + Math.random() * max / 2);
    }

    static double random(double max) {
        return Math.random() * max / 2 + Math.random() * max / 2;
    }

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

    static int customWeight(String itemName) {
        int count = 0;
        for (CustomEnchantment enchantment : EnchantmentAPI.getEnchantments()) {
            for (String s : enchantment.naturalItems) {
                if (itemName.equalsIgnoreCase(s)) count += enchantment.weight;
            }
        }
        return count;
    }

    static int vanillaWeight(ItemStack item) {
        int count = 0;
        for (int i = 0; i < WEIGHTS.length; i++) {
            if (!ENCHANTS[i].canEnchantItem(item)) continue;
            count += WEIGHTS[i];
        }
        return count;
    }
}
