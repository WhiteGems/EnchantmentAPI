package com.rit.sucy;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
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
        if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) return;
        if (!(event.getDamager() instanceof LivingEntity)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;

        // Apply enchantments
        LivingEntity damager = (LivingEntity)event.getDamager();
        for (Map.Entry<CustomEnchantment, Integer> entry : getValidEnchantments(getItems(damager)).entrySet()) {
            entry.getKey().applyEffect(damager, (LivingEntity)event.getEntity(), entry.getValue(), event);
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
     * Enchantment table integration
     *
     * @param event event details
     */
    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEnchant(EnchantItemEvent event) {
        ItemStack item = event.getItem();
        for (CustomEnchantment enchantment : EnchantmentAPI.getEnchantments()) {
            if (enchantment.canEnchantOnto(item)) {
                int enchantLevel = enchantment.getEnchantmentLevel(event.getExpLevelCost());
                if (enchantLevel > 0) enchantment.addToItem(item, enchantLevel);
            }
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
