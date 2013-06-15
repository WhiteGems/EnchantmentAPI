package com.rit.sucy;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

/**
 * Contains methods to register and access custom enchantments
 *
 * @author  Steven Sucy
 * @version 1.5
 */
public class EnchantmentAPI extends JavaPlugin implements CommandExecutor {

    /**
     * A table of the custom enchantments that are registered
     */
    private static Hashtable<String, CustomEnchantment> enchantments = new Hashtable<String, CustomEnchantment>();

    /**
     * Singleton instance of the API
     */
    private static EnchantmentAPI instance;

    /**
     * Enables the plugin and calls for all custom enchantments from any plugins
     * that extend the EnchantPlugin class
     */
    @Override
    public void onEnable(){

        instance = this;

        // Listeners
        new EListener(this);
        new EAnvil(this);
        getCommand("enchantlist").setExecutor(this);
        getCommand("addenchant").setExecutor(this);

        // Get custom enchantments from other plugins
        for (Plugin plugin : getServer().getPluginManager().getPlugins()) {
            if (plugin instanceof EnchantPlugin) ((EnchantPlugin) plugin).registerEnchantments();
        }
        for (Player player : getServer().getOnlinePlayers()) {
            EEquip.loadPlayer(player);
        }

        loadVanillaEnchantments();
        saveConfig();
    }

    /**
     * Disables the plugin and clears all custom enchantments
     */
    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        enchantments.clear();
        EEquip.clear();
    }

    /**
     * Will load Vanilla Enchantments as CustomEnchantments
     * Idea: Plugins modifying the probability of vanilla enchants
     */
    private void loadVanillaEnchantments(){
        for (VanillaData defaults : VanillaData.values())
        {
            VanillaEnchantment vanilla = new VanillaEnchantment(defaults.getEnchantment(), defaults.getEnchantWeight(), defaults.getLevels(), defaults.name());
            registerCustomEnchantment(vanilla);
        }
    }

    /**
     * Displays the list of registered enchantments when the command /enchantlist is executed
     *
     * @param sender the sender of the command that will receive the list
     * @param cmd    the command (not used because it can only be one thing)
     * @param label  the command label (not used)
     * @param args   arguments (not used)
     * @return       true
     */
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("enchantList")) {
            String message = ChatColor.GREEN + "Registered enchantments: ";
            if (enchantments.size() > 0) {
                for (CustomEnchantment enchantment : enchantments.values())
                    if (!(enchantment instanceof VanillaEnchantment))
                        message += ChatColor.GOLD + enchantment.name() + ChatColor.GRAY + ", ";
                message = message.substring(0, message.length() - 2);
            }
            sender.sendMessage(message);
        }
        else if (cmd.getName().equalsIgnoreCase("reloadenchants")) {
            getServer().getPluginManager().disablePlugin(this);
            getServer().getPluginManager().enablePlugin(this);
            sender.sendMessage(ChatColor.GREEN + "Enchantments have been reloaded!");
        }
        else if (sender instanceof Player) {
            if (args.length == 0)
                sender.sendMessage(ChatColor.DARK_RED + "Invalid number of arguments: expected at least 1");
            else {
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
                Player player = (Player)sender;
                CustomEnchantment enchantment = getEnchantment(name);
                if (enchantment == null) {
                    sender.sendMessage(ChatColor.DARK_RED + name + " is not a registered enchantment!");
                }
                else {
                    player.setItemInHand(enchantment.addToItem(player.getItemInHand(), level));
                    player.sendMessage(ChatColor.GREEN + "Enchantment has been applied.");
                }
            }
        }
        else sender.sendMessage(ChatColor.DARK_RED + "That is a player-only command!");
        return true;
    }

    /**
     * Checks if the enchantment with the given name is currently registered
     *
     * @param enchantmentName name of the enchantment
     * @return true if it is registered, false otherwise
     */
    public static boolean isRegistered(String enchantmentName) {
        return enchantments.containsKey(enchantmentName.toUpperCase());
    }

    /**
     * Retrieves the custom enchantment with the given name
     *
     * @param  name the name of the enchantment
     * @return      the enchantment with the given name, null if not found
     */
    public static CustomEnchantment getEnchantment(String name) {
        return enchantments.get(name.toUpperCase());
    }

    /**
     * Retrieves the names of all enchantments that have been registered
     *
     * @return set of custom enchantment names
     */
    public static Set<String> getEnchantmentNames() {
        return enchantments.keySet();
    }

    /**
     * Retrieves all custom enchantments
     *
     * @return the list of all custom enchantments
     */
    public static Collection<CustomEnchantment> getEnchantments() {
        return enchantments.values();
    }

    /**
     * Registers the given custom enchantment for the plugin
     *
     * @param  enchantment the enchantment to register
     * @return             true if it was registered, false otherwise
     */
    public static boolean registerCustomEnchantment(CustomEnchantment enchantment) {
        if (enchantments.containsKey(enchantment.name().toUpperCase())) return false;
        if (!enchantment.isEnabled()) return false;
        enchantments.put(enchantment.name().toUpperCase(), enchantment);
        return true;
    }

    /**
     * Unregisters the enchantment with the given name
     *
     * @param enchantmentName name of the enchantment to unregister
     * @return                true if it was removed, false if it didn't exist
     */
    public static boolean unregisterCustomEnchantment(String enchantmentName) {
        if (enchantments.containsKey(enchantmentName.toUpperCase())) {
            enchantments.remove(enchantmentName.toUpperCase());
            return true;
        }
        else return false;
    }

    /**
     * Returns the list of custom enchantments applied to the item
     *
     * @param item the item that's being checked for enchantments
     * @return     the list of attached enchantments
     */
    public static Map<CustomEnchantment, Integer> getEnchantments(ItemStack item) {
        HashMap<CustomEnchantment, Integer> list = new HashMap<CustomEnchantment, Integer>();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return list;
        if (!meta.hasLore()) return list;
        for (String lore : meta.getLore()) {
            String name = ENameParser.parseName(lore);
            int level = ENameParser.parseLevel(lore);
            if (name == null) continue;
            if (level == 0) continue;
            if (EnchantmentAPI.isRegistered(name)) {
                list.put(EnchantmentAPI.getEnchantment(name), level);
            }
        }
        return list;
    }

    /**
     * Gets every enchantment on an item, vanilla and custom
     * @param item item to retrieve the enchantments of
     * @return     all enchantments on the item
     */
    public static Map<CustomEnchantment, Integer> getAllEnchantments(ItemStack item) {
        Map<CustomEnchantment, Integer> map = getEnchantments(item);
        if (item.hasItemMeta() && item.getItemMeta().hasEnchants()) {
            for (Map.Entry<Enchantment, Integer> entry : item.getEnchantments().entrySet()) {
                map.put(getEnchantment(entry.getKey().getName()), entry.getValue());
            }
        }
        if (item.getType() == Material.ENCHANTED_BOOK) {
            EnchantmentStorageMeta meta = (EnchantmentStorageMeta)item.getItemMeta();
            for (Map.Entry<Enchantment, Integer> entry : meta.getStoredEnchants().entrySet())
                map.put(getEnchantment(entry.getKey().getName()), entry.getValue());
        }
        return map;
    }

    /**
     * Checks if the given item has an enchantment with the given name
     *
     * @param item            item to check
     * @param enchantmentName name of enchantment
     * @return                true if it has the enchantment, false otherwise
     */
    public static boolean itemHasEnchantment(ItemStack item, String enchantmentName) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        if (!meta.hasLore()) return false;
        for (String lore : meta.getLore()) {
            if (lore.contains(enchantmentName) && ENameParser.parseLevel(lore) > 0)
                return true;
        }
        return false;
    }

    /**
     * Removes all enchantments from the given item
     *
     * @param item item to clear enchantments from
     * @return     the item without enchantments
     */
    public static ItemStack removeEnchantments(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        if (!meta.hasLore()) return item;
        List<String> lore = meta.getLore();
        for (Map.Entry<CustomEnchantment, Integer> entry : getEnchantments(item).entrySet()) {
            lore.remove(ChatColor.GRAY + entry.getKey().name() + " " + ERomanNumeral.numeralOf(entry.getValue()));
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Gets the config file
     *
     * @return config file
     */
    static FileConfiguration config() {
        return instance.getConfig();
    }

    // Does nothing when run as .jar
    public static void main(String[] args) {}
}
