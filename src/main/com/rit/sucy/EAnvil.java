package com.rit.sucy;

import com.rit.sucy.config.LanguageNode;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.List;

/**
 * Custom anvil inventory handler
 * - still needs a lot of work -
 *
 * Known problems:
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

        List<String> component = plugin.getConfig().getStringList(LanguageNode.ANVIL_COMPONENT.getFullPath());
        List<String> separator = plugin.getConfig().getStringList(LanguageNode.ANVIL_SEPARATOR.getFullPath());
        List<String> result = plugin.getConfig().getStringList(LanguageNode.ANVIL_RESULT.getFullPath());

        ArrayList<String> lore = new ArrayList<String>();
        for (int i = 1; i < component.size(); i++)
            lore.add(component.get(i).replace('&', ChatColor.COLOR_CHAR));

        ItemMeta meta = COMPONENT.getItemMeta();
        meta.setDisplayName(component.get(0).replace('&', ChatColor.COLOR_CHAR));
        meta.setLore(lore);
        COMPONENT.setItemMeta(meta);

        lore.clear();
        for (int i = 1; i < component.size(); i++)
            lore.add(separator.get(i).replace('&', ChatColor.COLOR_CHAR));

        meta = MIDDLE.getItemMeta();
        meta.setDisplayName(separator.get(0).replace('&', ChatColor.COLOR_CHAR));
        meta.setLore(lore);
        MIDDLE.setItemMeta(meta);

        lore.clear();
        for (int i = 1; i < component.size(); i++)
            lore.add(result.get(i).replace('&', ChatColor.COLOR_CHAR));

        meta = RESULT.getItemMeta();
        meta.setDisplayName(result.get(0).replace('&', ChatColor.COLOR_CHAR));
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
                    getResult(anvil, player, event);
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
                else if (event.getRawSlot() == 7 && event.getCursor().getType() == Material.AIR
                        && inv.getItem(event.getSlot()) != null && inv.getItem(event.getSlot()).getType() != Material.AIR) {
                    getResult(anvil, player, event);
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
     * Tries to retrieve the anvil result for the player
     *
     * @param anvil  anvil inventory
     * @param player player reference
     * @param event  event details
     */
    void getResult(Inventory anvil, Player player, InventoryClickEvent event) {

        // Get the cost of the action
        int cost = getResultCost(anvil.getItem(7));

        // Refuse jobs of at least cost 40 unless in creative mode
        if ((cost >= 40 || player.getLevel() < cost) && player.getGameMode() != GameMode.CREATIVE)
            event.setCancelled(true);

        else {

            ItemStack first = anvil.getItem(1);
            ItemStack second = anvil.getItem(2);

            // Remove only the needed amount of materials
            if (first.getType().getMaxDurability() > 0 &&
                    second.getAmount() > 4 * first.getDurability() / first.getType().getMaxDurability() + 1) {
                second.setAmount(second.getAmount() - 4 * first.getDurability() / first.getType().getMaxDurability() - 1);
            }

            // If not a material or all were used, then just remove the item
            else anvil.clear(2);

            // Clear the other item as well
            anvil.clear(1);

            // Remove the cost lore
            clearCost(event.getCurrentItem());

            // Deduct levels if not in creative mode
            if (player.getGameMode() != GameMode.CREATIVE)
                player.setLevel(player.getLevel() - cost);
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
            if (isBook(first))
                validateBook(inv, first, second);
            else if (isBook(second)) {
                validateBook(inv, second, first);
            }

            // Materials repairing tools
            else if (first.getType().name().contains("DIAMOND_") && first.getDurability() < first.getType().getMaxDurability() && second.getType() == Material.DIAMOND)
                inv.setItem(7, makeItem(first, second.getAmount() > 4 ? 4 : second.getAmount()));
            else if (first.getType().name().contains("WOOD_") && first.getDurability() < first.getType().getMaxDurability() && second.getType() == Material.WOOD)
                inv.setItem(7, makeItem(first, second.getAmount() > 4 ? 4 : second.getAmount()));
            else if (first.getType().name().contains("STONE_") && first.getDurability() < first.getType().getMaxDurability() && second.getType() == Material.COBBLESTONE)
                inv.setItem(7, makeItem(first, second.getAmount() > 4 ? 4 : second.getAmount()));
            else if (first.getType().name().contains("IRON_") && first.getDurability() < first.getType().getMaxDurability() && second.getType() == Material.IRON_INGOT)
                inv.setItem(7, makeItem(first, second.getAmount() > 4 ? 4 : second.getAmount()));
            else if (first.getType().name().contains("GOLD_") && first.getDurability() < first.getType().getMaxDurability() && second.getType() == Material.GOLD_INGOT)
                inv.setItem(7, makeItem(first, second.getAmount() > 4 ? 4 : second.getAmount()));

            // Not a special case otherwise
            else inv.clear(7);
        }

        // If there's more than one item, don't allow it to be used
        else if (first.getAmount() > 1)
            inv.clear(7);
        else if (second.getAmount() > 1)
            inv.clear(7);

        // If they both have at least one enchantment, make the output combine them
        else if (EnchantmentAPI.getAllEnchantments(first).size() * EnchantmentAPI.getAllEnchantments(second).size() > 0)
            inv.setItem(7, makeItem(first, second));

        // If they are missing durability, make the output fix them
        else if (first.getDurability() > 0 && second.getDurability() > 0)
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
        boolean valid = false;
        if (target.getType() != Material.BOOK && target.getType() != Material.ENCHANTED_BOOK) {
            for (CustomEnchantment e : enchants.keySet())
                if (e.canEnchantOnto(target))
                    valid = true;
        }

        // If the book passed the test, create a new result
        if (valid && (enchants.size() > 0 && book.getAmount() == 1 && target.getAmount() == 1))
            inv.setItem(7, makeItem(target, book));

        // Otherwise leave no result
        else inv.clear(7);
    }

    /**
     * Creates a result item from a component and a material
     *
     * @param item   item to repair
     * @param amount amount of materials
     * @return       result item
     */
    ItemStack makeItem(ItemStack item, int amount) {
        ItemStack newItem = item.clone();

        int matCost;
        if (item.getType().name().contains("DIAMOND_")) {
            matCost = 12 * item.getDurability() / item.getType().getMaxDurability() + 2;
        }
        else matCost = 4 * item.getDurability() / item.getType().getMaxDurability() + 2;

        // Repair the item
        if (item.getDurability() - item.getType().getMaxDurability() * 0.25 * amount < 0)
            newItem.setDurability((short)0);
        else newItem.setDurability((short)(item.getDurability() - item.getType().getMaxDurability() * amount * 0.25));

        // Add the cost
        ItemMeta meta = newItem.getItemMeta();
        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<String>();
        Map<CustomEnchantment, Integer> enchants = EnchantmentAPI.getAllEnchantments(item);
        int total = matCost + (int)((enchants.size() + 1) * (enchants.size() / 2.0));
        for (Map.Entry<CustomEnchantment, Integer> enchant : enchants.entrySet())
            total += enchant.getKey().getCostPerLevel(false) * enchant.getValue();
        lore.add(COST + total);
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

        boolean book = isBook(secondary);
        double m = book ? 0.5 : 1;

        // Take the type of the primary item
        ItemStack item = new ItemStack(primary.getType());
        if (primary.hasItemMeta()) item.setItemMeta(primary.getItemMeta());
        int cost = 0;
        int extra = 0;

        // Get the base cost of the first item
        Set<Map.Entry<CustomEnchantment, Integer>> enchants = EnchantmentAPI.getAllEnchantments(primary).entrySet();
        for (Map.Entry<CustomEnchantment, Integer> entry : enchants)
            extra += entry.getKey().getCostPerLevel(book) * entry.getValue();
        extra += (int)((enchants.size() + 1) * (enchants.size() / 2.0));

        // Set the durability if applicable and add the corresponding cost
        if (primary.getDurability() > 0 && primary.getType() == secondary.getType())
            if (durability(item) + durability(secondary) < 0.88 * primary.getType().getMaxDurability()) {
                setDurability(item, (short)(durability(primary) + durability(secondary) + 0.12 * primary.getType().getMaxDurability()));
            int extraCost = (int)((durability(secondary) + 0.12 * primary.getType().getMaxDurability()) / 100);
            cost += extraCost > 0 ? extraCost : 1;
        }

        // Merge the enchantments
        int newEnchants = 0;
        for (Map.Entry<CustomEnchantment, Integer> entry : EnchantmentAPI.getAllEnchantments(secondary).entrySet()) {
            boolean conflict = false;
            boolean needsCost = false;
            if (entry.getKey().canEnchantOnto(item)) {
                for (Map.Entry<CustomEnchantment, Integer> e : enchants) {

                    // If they share the same enchantment, use the higher one
                    // If the levels are the same, raise it by one if it can go that high
                    if (e.getKey().name().equals(entry.getKey().name())) {
                        e.getKey().removeFromItem(item);
                        if (e.getValue() < entry.getValue()) {
                            cost += (entry.getValue() - e.getValue()) * entry.getKey().getCostPerLevel(book);
                            extra += (entry.getValue() - e.getValue()) * entry.getKey().getCostPerLevel(book);
                        }
                        else if (e.getValue().equals(entry.getValue())) {
                            cost += entry.getKey().getCostPerLevel(false);
                            extra += entry.getKey().getCostPerLevel(false);
                        }
                        else needsCost = true;
                        e.getKey().addToItem(item, e.getValue() > entry.getValue() ? e.getValue() : e.getValue().equals(entry.getValue()) ? Math.min(e.getValue() + 1, e.getKey().getEnchantmentLevel(50)) : entry.getValue());
                        conflict = true;
                    }

                    // If the enchantment can't be added onto the target, don't add it
                    else if (e.getKey().conflictsWith(entry.getKey())) {
                        conflict = true;
                        needsCost = true;
                    }
                }
            }
            else conflict = needsCost = true;

            // Add the enchant if there were no problems
            if (!conflict) {
                newEnchants++;
                entry.getKey().addToItem(item, entry.getValue());
                cost += entry.getValue() * entry.getKey().getCostPerLevel(book);
                extra += entry.getValue() * entry.getKey().getCostPerLevel(book);
            }

            // Add additional cost if necessary
            if (needsCost) cost += entry.getValue();
        }

        // Add additional cost for the enchantments added
        if (newEnchants > 0)
            extra += newEnchants * (enchants.size() + newEnchants - 1) + 1;

        // Add the cost to the item
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<String>();
        lore.add(COST + (int)(cost + extra * m));
        meta.setLore(lore);
        item.setItemMeta(meta);

        // Return the item
        return item;
    }

    /**
     * Checks if an item is a book
     *
     * @param item item to check
     * @return     true if book, false otherwise
     */
    boolean isBook(ItemStack item) {
        return item.getType() == Material.BOOK || item.getType() == Material.ENCHANTED_BOOK;
    }

    /**
     * Gets the durability of an item
     *
     * @param item item
     * @return     durability
     */
    short durability(ItemStack item) {
        return (short)(item.getType().getMaxDurability() - item.getDurability());
    }

    /**
     * Sets the durability of an item
     *
     * @param item  item to set the durability of
     * @param value durability remaining
     * @return      the item
     */
    ItemStack setDurability(ItemStack item, short value) {
        item.setDurability((short)(item.getType().getMaxDurability() - value));
        return item;
    }
}
