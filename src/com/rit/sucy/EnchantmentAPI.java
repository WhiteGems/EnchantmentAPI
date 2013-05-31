package com.rit.sucy;

import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Hashtable;

public class EnchantmentAPI extends JavaPlugin {

    static Hashtable<String, CustomEnchantment> enchantments = new Hashtable<String, CustomEnchantment>();

    @Override
    public void onEnable(){

        // Listeners
        new EListener(this);

        // Get custom enchantments from other plugins
        for (Plugin plugin : getServer().getPluginManager().getPlugins()) {
            if (plugin instanceof EnchantPlugin) ((EnchantPlugin) plugin).registerEnchantments();
        }
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        enchantments.clear();
    }

    // Retrieves a custom enchantment with the given name
    public static CustomEnchantment getEnchantment(String name) {
        return enchantments.get(name);
    }

    // Registers the custom enchantment for activating and enchantment table use
    public static boolean registerCustomEnchantment(CustomEnchantment enchantment) {
        if (enchantments.containsKey(enchantment.enchantName)) return false;
        enchantments.put(enchantment.enchantName, enchantment);
        return true;
    }

    // Does nothing when run as .jar
    public static void main(String[] args) {}
}
