package com.rit.sucy;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Custom anvil inventory handler
 * - still needs a lot of work -
 *
 * Known problems:
 * - Placing items with quantities of more than one will end up in losing items
 * - More materials for repairs are consumed than needed at times
 * - Haven't added the level 40 job cap of the anvil yet
 * - Can't rename items (I can't fix this with the current set-up)
 */
public class EAnvil implements Listener {

    /**
     * Description item for the left side
     */
    static final ItemStack COMPONENT = new ItemStack(Material.BOOK);

    /**
     * Description item for the right side
     */
    static final ItemStack RESULT = new ItemStack(Material.BOOK);

    /**
     * Description item for the center
     */
    static final ItemStack MIDDLE = new ItemStack(Material.BOOK);

    /**
     * Name prefix for the inventory
     */
    static final String NAME = "Anvil - ";

    /**
     * Prefix for the cost lore
     */
    static final String COST = ChatColor.DARK_RED + "Cost - ";

    /**
     * Initial contents of a custom anvil inventory
     */
    static final ItemStack[] CONTENTS = new ItemStack[] { COMPONENT, null, null, COMPONENT, MIDDLE, MIDDLE, RESULT, null, RESULT };

    /**
     * Plugin reference
     */
    Plugin plugin;

    /**
     * Constructor
     *
     * @param plugin plugin reference
     */
    public EAnvil(Plugin plugin) {

        // Register listeners
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        // Create the data for the descriptor books

        ArrayList<String> lore = new ArrayList<String>();
        lore.add(ChatColor.LIGHT_PURPLE + "Place components");
        lore.add(ChatColor.LIGHT_PURPLE + "over here!");

        ItemMeta meta = COMPONENT.getItemMeta();
        meta.setDisplayName(ChatColor.DARK_GREEN + "Anvil Components");
        meta.setLore(lore);
        COMPONENT.setItemMeta(meta);

        lore.clear();
        lore.add(ChatColor.LIGHT_PURPLE + "<- Components");
        lore.add(ChatColor.DARK_GRAY + "-----------");
        lore.add(ChatColor.LIGHT_PURPLE + "Results ->");

        meta = MIDDLE.getItemMeta();
        meta.setDisplayName(ChatColor.DARK_GREEN + "Anvil Book");
        meta.setLore(lore);
        MIDDLE.setItemMeta(meta);

        lore.clear();
        lore.add(ChatColor.LIGHT_PURPLE + "Results will");
        lore.add(ChatColor.LIGHT_PURPLE + "show up over");
        lore.add(ChatColor.LIGHT_PURPLE + "here!");

        meta = RESULT.getItemMeta();
        meta.setDisplayName(ChatColor.DARK_GREEN + "Anvil Result");
        meta.setLore(lore);
        RESULT.setItemMeta(meta);
    }

    /**
     * Opens the custom inventory instead of the default anvil inventory
     *
     * @param event event details
     */
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getType() == Material.ANVIL) {
            event.setCancelled(true);
            Player player = event.getPlayer();
            Inventory inv = plugin.getServer().createInventory(null, 9, NAME + player.getName());
            inv.setContents(CONTENTS);
            player.openInventory(inv);
        }
    }

    /**
     * Gives back any items when the inventory is closed
     *
     * @param event event details
     */
    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getInventory().getName().equals(NAME + event.getPlayer().getName())) {
            if (event.getInventory().getItem(1) != null)
                event.getPlayer().getInventory().addItem(event.getInventory().getItem(1));
            if (event.getInventory().getItem(2) != null)
                event.getPlayer().getInventory().addItem(event.getInventory().getItem(2));
        }
    }

    /**
     * Handles anvil transactions
     *
     * @param event event details
     */
    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Player player = plugin.getServer().getPlayer(event.getWhoClicked().getName());

        // Make sure the inventory is the custom inventory
        if (event.getInventory().getName().equals(NAME + player.getName())) {
            Inventory inv = event.getInventory();
            Inventory anvil = event.getView().getTopInventory();
            boolean top = event.getRawSlot() < 9;
            if (event.getSlot() == -999) return;

            if (event.isShiftClick()) {

                // Shift-clicking out one of the components will clear the end-product if there was one
                if (event.getRawSlot() == 1 || event.getRawSlot() == 2)
                    inv.clear(7);

                // Shift-clicking out the end-product will cost the player and consume the components
                else if (event.getRawSlot() == 7 && inv.getItem(event.getSlot()) != null && inv.getItem(event.getSlot()).getType() != Material.AIR) {

                    int cost = getResultCost(inv.getItem(7));

                    // Player needs enough experience or be in creative mode
                    if (player.getLevel() < cost && player.getGameMode() != GameMode.CREATIVE)
                        event.setCancelled(true);
                    else {
                        inv.clear(1);

                        // Only clear the needed amount of materials if they were used
                        if (inv.getItem(2).getAmount() > 4)
                            inv.getItem(2).setAmount(inv.getItem(2).getAmount() - 4);
                        else
                            inv.clear(2);

                        // Remove the lore
                        clearCost(event.getCurrentItem());

                        // Deduct the cost if not in creative
                        if (player.getGameMode() != GameMode.CREATIVE)
                            player.setLevel(player.getLevel() - cost);
                    }
                }

                // Don't allow clicking in other slots in the anvil
                else if (top)
                    event.setCancelled(true);

                // Don't allow shift clicking into the product slot
                else if (anvil.getItem(1) != null && anvil.getItem(2) != null)
                    event.setCancelled(true);

                // Update the product slot when needed
                else if (anvil.getItem(1) != null)
                    setResult(inv, inv.getItem(1), event.getCurrentItem());
                else if (anvil.getItem(2) != null)
                    setResult(inv, event.getCurrentItem(), inv.getItem(2));
            }
            else if (event.isLeftClick()) {

                // update the product slot if the components are changed
                if (event.getRawSlot() == 1)
                    setResult(inv, event.getCursor(), inv.getItem(2));
                else if (event.getRawSlot() == 2)
                    setResult(inv, inv.getItem(1), event.getCursor());

                // Same as shift-clicking out the product
                // TODO make this a method
                else if (event.getRawSlot() == 7 && event.getCursor().getType() == Material.AIR
                        && inv.getItem(event.getSlot()) != null && inv.getItem(event.getSlot()).getType() != Material.AIR) {
                    int cost = getResultCost(anvil.getItem(7));
                    if (player.getLevel() < cost && player.getGameMode() != GameMode.CREATIVE)
                        event.setCancelled(true);
                    else {
                        inv.clear(1);
                        if (inv.getItem(2).getAmount() > 4)
                            inv.getItem(2).setAmount(inv.getItem(2).getAmount() - 4);
                        else
                            inv.clear(2);
                        clearCost(event.getCurrentItem());
                        if (player.getGameMode() != GameMode.CREATIVE)
                            player.setLevel(player.getLevel() - cost);
                    }
                }

                // Don't allow clicks in other slots of the anvil
                else if (top)
                    event.setCancelled(true);
            }

            // Update the inventory manually after the click has happened
            new EUpdateTask(plugin, player);
        }
    }

    /**
     * Clears the cost lore from an item
     *
     * @param item item to clear the lore from
     */
    void clearCost(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();
        lore.remove(lore.size() - 1);
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    /**
     * Gets the cost from the lore of an item
     *
     * @param item item to check
     * @return     experience cost
     */
    int getResultCost(ItemStack item) {
        List<String> lore = item.getItemMeta().getLore();
        return Integer.parseInt(lore.get(lore.size() - 1).replace(COST, ""));
    }

    /**
     * Updates the result slot of the anvil
     *
     * @param inv    anvil inventory
     * @param first  first component
     * @param second second component
     */
    void setResult(Inventory inv, ItemStack first, ItemStack second) {

        // No result if one is missing
        if (first == null || second == null || first.getType() == Material.AIR || second.getType() == Material.AIR)
            inv.clear(7);

        // If they are different items, only special cases work
        else if (first.getType() != second.getType()) {

            // Books adding to other items
            if (first.getType() == Material.BOOK || first.getType() == Material.ENCHANTED_BOOK)
                validateBook(inv, first, second);
            else if (second.getType() == Material.BOOK || second.getType() == Material.ENCHANTED_BOOK) {
                validateBook(inv, second, first);
            }

            // Materials repairing tools
            else if (first.getType().name().contains("DIAMOND_") && first.getDurability() < first.getType().getMaxDurability() && second.getType() == Material.DIAMOND)
                inv.setItem(7, makeItem(first, second.getAmount() > 4 ? 4 : second.getAmount(), 3));
            else if (first.getType().name().contains("WOOD_") && first.getDurability() < first.getType().getMaxDurability() && second.getType() == Material.WOOD)
                inv.setItem(7, makeItem(first, second.getAmount() > 4 ? 4 : second.getAmount(), 1));
            else if (first.getType().name().contains("STONE_") && first.getDurability() < first.getType().getMaxDurability() && second.getType() == Material.COBBLESTONE)
                inv.setItem(7, makeItem(first, second.getAmount() > 4 ? 4 : second.getAmount(), 1));
            else if (first.getType().name().contains("IRON_") && first.getDurability() < first.getType().getMaxDurability() && second.getType() == Material.IRON_INGOT)
                inv.setItem(7, makeItem(first, second.getAmount() > 4 ? 4 : second.getAmount(), 2));
            else if (first.getType().name().contains("GOLD_") && first.getDurability() < first.getType().getMaxDurability() && second.getType() == Material.GOLD_INGOT)
                inv.setItem(7, makeItem(first, second.getAmount() > 4 ? 4 : second.getAmount(), 2));

            // Not a special case otherwise
            else inv.clear(7);
        }

        // If they both have at least one enchantment, make the output combine them
        else if (EnchantmentAPI.getAllEnchantments(first).size() * EnchantmentAPI.getAllEnchantments(second).size() > 0)
            inv.setItem(7, makeItem(first, second));

        // If they are missing durability, make the output fix them
        // TODO make damaged items not do a full repair but rather the (firstDurability + secondDurability + 0.12 * maxDurability)
        else if (first.getDurability() < first.getType().getMaxDurability() && second.getDurability() < second.getType().getMaxDurability())
            inv.setItem(7, makeItem(first, second));

        // No result needed
        else
            inv.clear(7);
    }

    /**
     * Makes sure the book in the anvil can benefit the target item
     *
     * @param inv    anvil inventory
     * @param book   book component
     * @param target target item
     */
    void validateBook(Inventory inv, ItemStack book, ItemStack target) {
        Map<CustomEnchantment, Integer> enchants = EnchantmentAPI.getAllEnchantments(book);

        // Book must have at least one enchantment and they must be able to go on the target item
        boolean valid = enchants.size() > 0;
        if (target.getType() != Material.BOOK && target.getType() != Material.ENCHANTED_BOOK) {
            for (CustomEnchantment e : enchants.keySet())
                if (!e.canEnchantOnto(target))
                    valid = false;
        }

        // If the book passed the test, create a new result
        if (valid) inv.setItem(7, makeItem(target, book));

        // Otherwise leave no result
        else inv.clear(7);
    }

    /**
     * Creates a result item from a component and a material
     *
     * @param item   item to repair
     * @param amount amount of materials
     * @param cost   cost per material
     * @return       result item
     */
    ItemStack makeItem(ItemStack item, int amount, int cost) {
        ItemStack newItem = item.clone();

        // Repair the item
        if (item.getDurability() - item.getType().getMaxDurability() * 0.25 * amount < 0) {
            newItem = new ItemStack(item.getType());
            if (item.hasItemMeta()) newItem.setItemMeta(item.getItemMeta());
        }
        else newItem.setDurability((short)(item.getDurability() - item.getType().getMaxDurability() * amount * 0.25));

        // Add the cost
        ItemMeta meta = newItem.getItemMeta();
        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<String>();
        int total = 0;
        for (int i : EnchantmentAPI.getAllEnchantments(item).values())
            total += i;
        lore.add(COST + (3 * total + cost * amount));
        meta.setLore(lore);
        newItem.setItemMeta(meta);

        // Return the item
        return newItem;
    }

    /**
     * Makes a result item from two components
     *
     * @param primary   target item
     * @param secondary supplement item
     * @return          resulting item
     */
    ItemStack makeItem(ItemStack primary, ItemStack secondary) {

        // Take the type of the primary item
        ItemStack item = new ItemStack(primary.getType());
        if (primary.hasItemMeta()) item.setItemMeta(primary.getItemMeta());
        int cost = 0;

        // Get the base cost of the first item
        Set<Map.Entry<CustomEnchantment, Integer>> enchants = EnchantmentAPI.getAllEnchantments(primary).entrySet();
        for (Map.Entry<CustomEnchantment, Integer> entry : enchants)
            cost += 2 * entry.getValue();

        // Merge the enchantments
        for (Map.Entry<CustomEnchantment, Integer> entry : EnchantmentAPI.getAllEnchantments(secondary).entrySet()) {
            boolean conflict = false;
            for (Map.Entry<CustomEnchantment, Integer> e : enchants) {

                // If they share the same enchantment, use the higher one
                // If the levels are the same, raise it by one if it can go that high
                if (e.getKey().name().equals(entry.getKey().name())) {
                    e.getKey().removeFromItem(item);
                    if (e.getValue() > entry.getValue())
                        cost += entry.getValue() * 3 - e.getValue() * 2;
                    e.getKey().addToItem(item, e.getValue() > entry.getValue() ? e.getValue() : e.getValue().equals(entry.getValue()) ? Math.min(e.getValue() + 1, e.getKey().getEnchantmentLevel(50)) : entry.getValue());
                    conflict = true;
                }

                // If the enchantment can't be added onto the target, don't add it
                else if (e.getKey().conflictsWith(entry.getKey()))
                    conflict = true;
            }

            // Add the enchant if there were no problems
            if (!conflict)
                entry.getKey().addToItem(item, entry.getValue());

            // Add the cost
            // TODO make the ost match the enchant properly instead of just a flat 3
            cost += entry.getValue() * 3;
        }

        // Add the cost to the item
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<String>();
        lore.add(COST + cost);
        meta.setLore(lore);
        item.setItemMeta(meta);

        // Return the item
        return item;
    }
}
