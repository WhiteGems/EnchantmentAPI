package com.rit.sucy;

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
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Listens for events and passes them onto enchantments
 */
class EListener implements Listener {

    Plugin plugin;

    /**
     * Basic constructor that registers this listener
     *
     * @param plugin plugin to register this listener to
     */
    public EListener(Plugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;
    }

    /**
     * Event for offensive enchantments
     *
     * @param event the event details
     */
    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHit(EntityDamageByEntityEvent event) {

        // Rule out cases where enchantments don't apply
        Entity damager = event.getDamager();
        if (damager instanceof Projectile) damager = ((Projectile) damager).getShooter();
        if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) return;
        if (!(damager instanceof LivingEntity)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;

        // Apply enchantments
        for (Map.Entry<CustomEnchantment, Integer> entry : getValidEnchantments(getItems((LivingEntity)damager)).entrySet()) {
            entry.getKey().applyEffect((LivingEntity)damager, (LivingEntity)event.getEntity(), entry.getValue(), event);
        }
    }

    /**
     * Event for defensive enchantments
     *
     * @param event the event details
     */
    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamaged(EntityDamageByEntityEvent event) {

        // Rule out cases where enchantments don't apply
        if (!(event.getEntity() instanceof LivingEntity)) return;

        // Apply enchantments
        LivingEntity damaged = (LivingEntity)event.getEntity();
        LivingEntity damager = event.getDamager() instanceof LivingEntity ? (LivingEntity) event.getDamager()
                : event.getDamager() instanceof Projectile ? ((Projectile)event.getDamager()).getShooter()
                : null;
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
        if (event.getInventory().getHolder() instanceof Player)
            new EEquip((Player)event.getInventory().getHolder()).runTaskLater(plugin, 1);
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
     * Enchantment table integration
     *
     * @param event event details
     */
    @EventHandler
    public void onEnchant(EnchantItemEvent event) {
        event.setCancelled(true);
        if (EnchantmentAPI.getEnchantments(event.getItem()).size() > 0) return;
        if (event.getEnchanter().getLevel() < event.getExpLevelCost()
                && event.getEnchanter().getGameMode() != GameMode.CREATIVE) return;

        event.getInventory().clear();
        event.getInventory().addItem(EEnchantTable.enchant(event.getItem(), event.getExpLevelCost(), event));
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
            return;
        }
    }

    /*
    @EventHandler (priority = EventPriority.LOWEST)
    public void onAnvil(InventoryClickEvent event) {
        if (event.getInventory().getType() == InventoryType.ANVIL) {
            Player player = plugin.getServer().getPlayer(event.getWhoClicked().getName());
            boolean top = event.getRawSlot() < event.getView().getTopInventory().getSize();
            if (top) {
                if (!event.isRightClick() && event.getSlot() == 2 && event.getCurrentItem().getType() != Material.AIR) {
                    player.sendMessage("Done");
                }
            }
            new EAnvilTask(event.getView().getTopInventory()).runTaskLater(plugin, 1);
        }
    }
    */

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
