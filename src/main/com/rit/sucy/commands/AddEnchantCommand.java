package com.rit.sucy.commands;

import com.rit.sucy.CustomEnchantment;
import com.rit.sucy.EnchantmentAPI;
import com.rit.sucy.service.ICommand;
import com.rit.sucy.service.PermissionNode;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Adds an enchantment to an item
 */
public class AddEnchantCommand implements ICommand{
    @Override
    public boolean execute(EnchantmentAPI plugin, CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player && sender.hasPermission(PermissionNode.ADMIN.getNode()))
        {
            String name = args[0];
            int difference = 0;
            int level = 1;
            try {
                level = Integer.parseInt(args[args.length - 1]);
                difference = 1;
            }
            catch (Exception e) {
                // Level is not provided
            }

            for (int i = 1; i < args.length - difference; i++) name += " " + args[i];
            if (eNames.containsKey(name.toLowerCase()))
                name = eNames.get(name.toLowerCase());
            Player player = (Player)sender;
            CustomEnchantment enchantment = EnchantmentAPI.getEnchantment(name);
            if (enchantment == null) {
                sender.sendMessage(ChatColor.DARK_RED + name + " is not a registered enchantment!");
            }
            else {
                player.setItemInHand(enchantment.addToItem(player.getItemInHand(), level));
                player.sendMessage(ChatColor.GREEN + "Enchantment has been applied.");
            }
        }
        return true;
    }

    private static final Map<String,String> eNames = new HashMap<String,String>(){{
        put("knockback",             "KNOCKBACK");
        put("looting",               "LOOT_BONUS_MOBS");
        put("sharpness",             "DAMAGE_ALL");
        put("smite",                 "DAMAGE_UNDEAD");
        put("bane of arthropods",    "DAMAGE_ARTHROPODS");
        put("fire aspect",           "FIRE_ASPECT");
        put("infinity",              "ARROW_INFINITE");
        put("flame",                 "ARROW_FIRE");
        put("punch",                 "ARROW_KNOCKBACK");
        put("power",                 "ARROW_DAMAGE");
        put("respiration",           "OXYGEN");
        put("aqua affinity",         "WATER_WORKER");
        put("feather falling",       "PROTECTION_FALL");
        put("thorns",                "THORNS");
        put("protection",            "PROTECTION_ENVIRONMENTAL");
        put("fire protection",       "PROTECTION_FIRE");
        put("blast protection",      "PROTECTION_EXPLOSIONS");
        put("projectile protection", "PROTECTION_PROJECTILE");
        put("efficiency",            "DIG_SPEED");
        put("unbreaking",            "DURABILITY");
        put("fortune",               "LOOT_BONUS_BLOCKS");
        put("silk touch",            "SILK_TOUCH");
    }};
}
