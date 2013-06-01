package com.rit.sucy;

import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Hashtable;

/**
 * Contains methods to register and access custom enchantments
 */
public class EnchantmentAPI extends JavaPlugin {

    static Hashtable<String, CustomEnchantment> enchantments = new Hashtable<String, CustomEnchantment>();

    /**
     * Enables the plugin and calls for all custom enchantments from any plugins
     * that extend the EnchantPlugin class
     */
    @Override
    public void onEnable(){

        // Listeners
        new EListener(this);

        // Get custom enchantments from other plugins
        for (Plugin plugin : getServer().getPluginManager().getPlugins()) {
            if (plugin instanceof EnchantPlugin) ((EnchantPlugin) plugin).registerEnchantments();
        }
    }

    /**
     * Disables the plugin and clears all custom enchantments
     */
    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        enchantments.clear();
    }

    /**
     * Retrieves the custom enchantment with the given name
     *
     * @param  name the name of the enchantment
     * @return      the enchantment with the given name, null if not found
     */
    public static CustomEnchantment getEnchantment(String name) {
        return enchantments.get(name);
    }

    /**
     * Registers the given custom enchantment for the plugin
     *
     * @param  enchantment the enchantment to register
     * @return             true if it was registered, false otherwise
     */
    public static boolean registerCustomEnchantment(CustomEnchantment enchantment) {
        if (enchantments.containsKey(enchantment.enchantName)) return false;
        enchantments.put(enchantment.enchantName, enchantment);
        return true;
    }

    // Does nothing when run as .jar
    public static void main(String[] args) {}
}
