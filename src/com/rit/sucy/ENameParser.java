package com.rit.sucy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class ENameParser {

    static String parseName(String lore) {
        if (!lore.contains(" ")) return null;

        String[] pieces = lore.split(" ");
        if (ERomanNumeral.getValueOf(pieces[pieces.length - 1]) == 0)
            return null;

        String name = "";
        for (int i = 0; i < pieces.length - 1; i++) {
            name += pieces[i] + (i < pieces.length - 2 ? " " : "");
        }
        name = ChatColor.stripColor(name);
        Bukkit.broadcastMessage(name);
        return name;
    }

    static int parseLevel(String lore) {
        if (!lore.contains(" ")) return 0;

        String[] pieces = lore.split(" ");
        return ERomanNumeral.getValueOf(pieces[pieces.length - 1]);
    }
}
