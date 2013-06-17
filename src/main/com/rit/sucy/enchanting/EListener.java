package com.rit.sucy.enchanting;

import com.rit.sucy.CustomEnchantment;
import com.rit.sucy.EUpdateTask;
import com.rit.sucy.EnchantmentAPI;
import com.rit.sucy.config.LanguageNode;
import com.rit.sucy.service.ENameParser;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EnchantingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.*;

/**
 * Listens for events and passes them onto enchantments
 */
public class EListener implements Listener {

    /**
     * Lore for unenchantable items
     */
    final String cantEnchant;

    /**
     * Plugin reference
     */
    Plugin plugin;

    /**
     * Placeholder for enchantable items
     */
    final ItemStack placeholder = new ItemStack(Material.BOOK);

    /**
     * Placeholder for unenchantable items
     */
    final ItemStack placeholder2 = new ItemStack(Material.BOOK);

    /**
     * Items stored for enchanting tables
     */
    Hashtable<String, ItemStack> storedItems = new Hashtable<String, ItemStack>();

    /**
     * Whether or not to excuse the next player attack event
     */
    public static boolean excuse = false;

    /**
     * Basic constructor that registers this listener
     *
     * @param plugin plugin to register this listener to
     */
    public EListener(Plugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;

        List<String> enchantable = plugin.getConfig().getStringList(LanguageNode.TABLE_ENCHANTABLE.getFullPath());
        List<String> unenchantable = plugin.getConfig().getStringList(LanguageNode.TABLE_UNENCHANTABLE.getFullPath());
        cantEnchant = unenchantable.get(1).replace('&', ChatColor.COLOR_CHAR);

        ItemMeta meta = placeholder.getItemMeta();
        meta.setDisplayName(enchantable.get(0).replace('&', ChatColor.COLOR_CHAR));
        ArrayList<String> lore = new ArrayList<String>();
        lore.add(enchantable.get(1).replace('&', ChatColor.COLOR_CHAR));
        meta.setLore(lore);
        placeholder.setItemMeta(meta);
        lore.clear();
        lore.add(cantEnchant);
        meta.setLore(lore);
        meta.setDisplayName(unenchantable.get(0).replace('&', ChatColor.COLOR_CHAR));
        placeholder2.setItemMeta(meta);
    }

    /**
     * Event for offensive enchantments
     *
     * @param event the event details
     */
    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHit(EntityDamageByEntityEvent event) {

        if (excuse) {
            excuse = false;
            return;
        }

        // Rule out cases where enchantments don't apply
        if (!(event.getEntity() instanceof LivingEntity)) return;

        LivingEntity damaged = (LivingEntity)event.getEntity();
        LivingEntity damager = event.getDamager() instanceof LivingEntity ? (LivingEntity) event.getDamager()
                : event.getDamager() instanceof Projectile ? ((Projectile)event.getDamager()).getShooter()
                : null;
        if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK
                && event.getCause() != EntityDamageEvent.DamageCause.PROJECTILE) return;
        if (damager != null) {
            // Apply offensive enchantments
            for (Map.Entry<CustomEnchantment, Integer> entry : getValidEnchantments(getItems(damager)).entrySet()) {
                entry.getKey().applyEffect(damager, damaged, entry.getValue(), event);
            }
        }

        // Apply defensive enchantments
        for (Map.Entry<CustomEnchantment, Integer> entry : getValidEnchantments(getItems(damaged)).entrySet()) {
            entry.getKey().applyDefenseEffect(damaged, damager, entry.getValue(), event);
        }
    }

    /**
     * Event for defensive enchantments
     *
     * @param event the event details
     */
    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamaged(EntityDamageEvent event) {

        // Rule out cases where enchantments don't apply
        if (!(event.getEntity() instanceof LivingEntity)) return;

        // Apply enchantments
        LivingEntity damaged = (LivingEntity)event.getEntity();
        for (Map.Entry<CustomEnchantment, Integer> entry : getValidEnchantments(getItems(damaged)).entrySet()) {
            entry.getKey().applyDefenseEffect(damaged, null, entry.getValue(), event);
        }
    }

    /**
     * Event for defensive enchantments
     *
     * @param event the event details
     */
    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamaged(EntityDamageByBlockEvent event) {

        // Rule out cases where enchantments don't apply
        if (!(event.getEntity() instanceof LivingEntity)) return;

        // Apply enchantments
        LivingEntity damaged = (LivingEntity)event.getEntity();
        for (Map.Entry<CustomEnchantment, Integer> entry : getValidEnchantments(getItems(damaged)).entrySet()) {
            entry.getKey().applyDefenseEffect(damaged, null, entry.getValue(), event);
        }
    }


    /**
     * Event for tool enchantments
     *
     * @param event the event details
     */
    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamageBlock(BlockDamageEvent event) {

        // Apply enchantments
        for (Map.Entry<CustomEnchantment, Integer> entry : getValidEnchantments(getItems(event.getPlayer())).entrySet()) {
            entry.getKey().applyToolEffect(event.getPlayer(), event.getBlock(), entry.getValue(), event);
        }
    }

    /**
     * Event for tool enchantments
     *
     * @param event the event details
     */
    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreakBlock(BlockBreakEvent event) {

        // Apply enchantments
        for (Map.Entry<CustomEnchantment, Integer> entry : getValidEnchantments(getItems(event.getPlayer())).entrySet()) {
            entry.getKey().applyToolEffect(event.getPlayer(), event.getBlock(), entry.getValue(), event);
        }
    }

    /**
     * Event for miscellaneous enchantments and Equip effects
     *
     * @param event the event details
     */
    @EventHandler (priority = EventPriority.MONITOR)
    public void onInteract(PlayerInteractEvent event) {

        // Apply enchantments
        for (Map.Entry<CustomEnchantment, Integer> entry : getValidEnchantments(getItems(event.getPlayer())).entrySet()) {
            entry.getKey().applyMiscEffect(event.getPlayer(), entry.getValue(), event);
        }

        new EEquip(event.getPlayer()).runTaskLater(plugin, 1);
    }

    /**
     * Event for entity interaction effects
     *
     * @param event the event details
     */
    @EventHandler (priority = EventPriority.MONITOR)
    public void onInteract(PlayerInteractEntityEvent event) {
        for (Map.Entry<CustomEnchantment, Integer> entry : getValidEnchantments(getItems(event.getPlayer())).entrySet()) {
            entry.getKey().applyEntityEffect(event.getPlayer(), entry.getValue(), event);
        }
    }

    /**
     * Event for Equip and Unequip effects
     *
     * @param event event details
     */
    @EventHandler (priority =  EventPriority.MONITOR, ignoreCancelled = true)
    public void onEquip(InventoryClickEvent event) {
        new EEquip(plugin.getServer().getPlayer(event.getWhoClicked().getName())).runTaskLater(plugin, 1);
    }

    /**
     * Event for Equip and Unequip events
     *
     * @param event event details
     */
    @EventHandler
    public void onBreak(PlayerItemBreakEvent event) {
        new EEquip(event.getPlayer()).runTaskLater(plugin, 1);
    }

    /**
     * Equipment loading event
     *
     * @param event event details
     */
    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onConnect(PlayerJoinEvent event) {
        EEquip.loadPlayer(event.getPlayer());
    }

    /**
     * Equipment loading event
     *
     * @param event event details
     */
    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDisconnect(PlayerQuitEvent event) {
        EEquip.clearPlayer(event.getPlayer());
    }

    /**
     * Handles converting items to and from placeholders when using an enchanting table
     *
     * @param event event details
     */
    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Player player = plugin.getServer().getPlayer(event.getWhoClicked().getName());
        if (event.getInventory() instanceof EnchantingInventory) {
            EnchantingInventory inv = (EnchantingInventory) event.getInventory();
            if (event.getRawSlot() == 0) {
                if (inv.getItem() != null && inv.getItem().getType() != Material.AIR) {
                    ItemStack storedItem = storedItems.get(event.getWhoClicked().getName());
                    inv.getItem().setType(storedItem.getType());
                    inv.getItem().setAmount(storedItem.getAmount());
                    if (storedItem.hasItemMeta()) inv.getItem().setItemMeta(storedItem.getItemMeta());
                    else inv.getItem().setItemMeta(null);
                }
                if (event.getCursor() != null && event.getCursor().getType() != Material.AIR) {
                    if (event.getCursor().hasItemMeta() && event.getCursor().getItemMeta().hasDisplayName()
                            && event.getCursor().getItemMeta().getDisplayName().equals(placeholder.getItemMeta().getDisplayName()))
                        return;
                    storedItems.put(event.getWhoClicked().getName(), event.getCursor().clone());
                    createPlaceholder(event.getCursor(), event.getCursor().clone());
                }
            }
            else if (event.isShiftClick() && (inv.getItem() == null || inv.getItem().getType() == Material.AIR)
                    && event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR) {
                storedItems.put(event.getWhoClicked().getName(), event.getCurrentItem().clone());
                createPlaceholder(event.getCurrentItem(), event.getCurrentItem().clone());
            }
            new EUpdateTask(plugin, player);
        }
    }

    /**
     * Creates a placeholder for the privded item
     *
     * @param item       item to modify
     * @param storedItem original item
     */
    void createPlaceholder(ItemStack item, ItemStack storedItem) {
        if (canEnchant(item)) {
            item.setType(placeholder.getType());
            item.setItemMeta(placeholder.getItemMeta());
        }
        else {
            item.setType(placeholder2.getType());
            item.setItemMeta(placeholder2.getItemMeta());
        }
        item.setAmount(1);
        List<String> lore = item.getItemMeta().getLore();
        lore.add(ChatColor.GRAY + storedItem.getType().name().toLowerCase().replace("_", " ") + " (x" + storedItem.getAmount() + ")");
        ItemMeta meta = item.getItemMeta();
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    /**
     * Checks if an item can be enchanted
     *
     * @param item item to check
     * @return     true if can enchant, false otherwise
     */
    boolean canEnchant(ItemStack item) {
        if (EEnchantTable.getAllValidEnchants(item).size() == 0) return false;
        else if (item.hasItemMeta() && item.getItemMeta().hasEnchants()) return false;
        else if (EnchantmentAPI.getEnchantments(item).size() > 0) return false;
        return true;
    }

    /**
     * Restores any items when an enchanting table is closed
     *
     * @param event event details
     */
    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getInventory() instanceof EnchantingInventory) {
            EnchantingInventory inventory = (EnchantingInventory)event.getInventory();
            if (inventory.getItem() != null && inventory.getItem().getType() != Material.AIR) {
                inventory.setItem(storedItems.get(event.getPlayer().getName()));
            }
        }
    }

    /**
     * Enchantment table integration
     *
     * @param event event details
     */
    @EventHandler (ignoreCancelled = true)
    public void onEnchant(EnchantItemEvent event) {
        event.setCancelled(true);
        if (EnchantmentAPI.getEnchantments(event.getItem()).size() > 0) return;
        if (event.getEnchanter().getLevel() < event.getExpLevelCost()
                && event.getEnchanter().getGameMode() != GameMode.CREATIVE) return;

        event.getInventory().clear();
        event.getEnchantsToAdd().clear();
        ItemStack storedItem = storedItems.get(event.getEnchanter().getName());
        if (storedItem.getAmount() > 1) {
            storedItem.setAmount(storedItem.getAmount() - 1);
            event.getEnchanter().getInventory().addItem(storedItem.clone());
            storedItem.setAmount(1);
        }
        event.getInventory().addItem(EEnchantTable.enchant(storedItem, event.getExpLevelCost()));
        if (event.getEnchanter().getGameMode() != GameMode.CREATIVE)
            event.getEnchanter().setLevel(event.getEnchanter().getLevel() - event.getExpLevelCost());
    }

    /**
     * Doesn't show options for items with custom enchantments
     *
     * @param event event details
     */
    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPrepareEnchant(PrepareItemEnchantEvent event) {
        if (EnchantmentAPI.getEnchantments(event.getItem()).size() > 0) {
            event.setCancelled(true);
        }
        if (event.getItem().hasItemMeta() && event.getItem().getItemMeta().hasLore() && event.getItem().getItemMeta().getLore().contains(cantEnchant)) {
            event.setCancelled(true);
        }
    }

    /**
     * Gets a list of valid enchantments from a set of items
     *
     * @param items the list of items to check for valid enchantments
     * @return      the valid enchantments and their corresponding enchantment levels
     */
    private Map<CustomEnchantment, Integer> getValidEnchantments(ArrayList<ItemStack> items) {
        Map<CustomEnchantment, Integer> validEnchantments = new HashMap<CustomEnchantment, Integer>();
        for (ItemStack item : items) {
            ItemMeta meta = item.getItemMeta();
            if (meta == null) continue;
            if (!meta.hasLore()) continue;
            for (String lore : meta.getLore()) {
                String name = ENameParser.parseName(lore);
                int level = ENameParser.parseLevel(lore);
                if (name == null) continue;
                if (level == 0) continue;
                if (EnchantmentAPI.isRegistered(name)) {
                    validEnchantments.put(EnchantmentAPI.getEnchantment(name), level);
                }
            }
        }
        return validEnchantments;
    }

    /**
     * Retrieves a list of equipment on the entity that have at least some lore
     *
     * @param entity the entity wearing the equipment
     * @return       the list of all equipment with lore
     */
    private ArrayList<ItemStack> getItems(LivingEntity entity) {
        ItemStack[] armor = entity.getEquipment().getArmorContents();
        ItemStack weapon = entity.getEquipment().getItemInHand();
        ArrayList<ItemStack> items = new ArrayList<ItemStack>(Arrays.asList(armor));
        items.add(weapon);

        return items;
    }
}
