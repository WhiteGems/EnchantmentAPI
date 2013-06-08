package com.rit.sucy;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;


/**
 * Parses lore names into enchantment names and levels
 */
//class ENameParser {

    /**
     * Parses an enchantment name from a lore string
     *
     * @param lore the lore string to parse
     * @return     enchantment name
     */
    //static String parseName(String lore) {
    //    if (!lore.contains(" ")) return null;
    //
    //    String[] pieces = lore.split(" ");
    //
    //    String name = "";
    //    for (int i = 0; i < pieces.length - 1; i++) {
    //        name += pieces[i] + (i < pieces.length - 2 ? " " : "");
    //    }
    //    name = ChatColor.stripColor(name);
    //    return name;
    //}

    /**
     * Parses an enchantment level from a lore string
     * @param lore the lore string to parse
     * @return     enchantment name
     */
    //static int parseLevel(String lore) {
    //    if (!lore.contains(" ")) return 0;
    //
    //    String[] pieces = lore.split(" ");
    //    return ERomanNumeral.getValueOf(pieces[pieces.length - 1]);
    //}


    /**
     * Gets the vanilla name of the item
     *
     * @param item item to get the name of
     * @return     normal display name
     */
    /*
    public static String getEnchantedName(ItemStack item) {
        if (item.hasItemMeta()) {
            if (item.getItemMeta().hasEnchants()) return null;
            if (item.getItemMeta().hasDisplayName()) return null;
        }

        String name = item.getType().name().toLowerCase().replace("spade", "shovel")
                .replace("leather_leggings", "leather_pants").replace("leather_chestplate", "leather_tunic")
                .replace("leather_helmet", "leather_cap").replace("chainmail", "chain").replace("wood_", "wooden_");
        String[] pieces = name.split("_");
        name = ChatColor.AQUA + "";
        for (int i = 0; i < pieces.length; i++) {
            name += pieces[i].substring(0, 1).toUpperCase() + pieces[i].substring(1);
            if (i < pieces.length - 1) name += " ";
        }
        return name;
    }
    */
//}
