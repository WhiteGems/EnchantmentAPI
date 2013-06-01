package com.rit.sucy;

import org.bukkit.Bukkit;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Listens for events and passes them onto enchantments
 */
class EListener implements Listener {

    // Plugin reference
    EnchantmentAPI plugin;

    // Constructor
    public EListener(EnchantmentAPI plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
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

        // Check for enchantments and apply them
        LivingEntity damager = (LivingEntity)event.getDamager();
        ArrayList<ItemStack> items = getItems(damager);
        for (ItemStack item : items) {
            ItemMeta meta = item.getItemMeta();
            for (String lore : meta.getLore()) {
                String name = ENameParser.parseName(lore);
                if (name == null) continue;
                if (EnchantmentAPI.enchantments.containsKey(name))
                    EnchantmentAPI.enchantments.get(name).applyEffect(damager,
                            (LivingEntity) event.getEntity(),
                            ENameParser.parseLevel(lore), event);
            }
        }
    }

    /**
     * Event for defensive enchantments
     *
     * @param event the event details
     */
    // Applies enchantments when hit
    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamaged(EntityDamageByEntityEvent event) {

        // Rule out cases where enchantments don't apply
        if (!(event.getEntity() instanceof LivingEntity)) return;

        // Check for enchantments and apply them
        LivingEntity damaged = (LivingEntity)event.getEntity();
        ArrayList<ItemStack> items = getItems(damaged);
        for (ItemStack item : items) {
            ItemMeta meta = item.getItemMeta();
            for (String lore : meta.getLore()) {
                String name = ENameParser.parseName(lore);
                if (name == null) continue;
                if (EnchantmentAPI.enchantments.containsKey(name))
                    EnchantmentAPI.enchantments.get(name).applyDefenseEffect(damaged,
                            event.getDamager() instanceof LivingEntity ? (LivingEntity) event.getDamager() :
                                    event.getDamager() instanceof Projectile ? ((Projectile)event.getDamager()).getShooter() : null,
                            ENameParser.parseLevel(lore), event);
            }
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

        // Check for enchantments and apply them
        LivingEntity damaged = (LivingEntity)event.getEntity();
        ArrayList<ItemStack> items = getItems(damaged);
        for (ItemStack item : items) {
            ItemMeta meta = item.getItemMeta();
            for (String lore : meta.getLore()) {
                String name = ENameParser.parseName(lore);
                if (name == null) continue;
                if (EnchantmentAPI.enchantments.containsKey(name))
                    EnchantmentAPI.enchantments.get(name).applyDefenseEffect(damaged,
                            null, ENameParser.parseLevel(lore), event);
            }
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

        // Check for enchantments and apply them
        LivingEntity damaged = (LivingEntity)event.getEntity();
        ArrayList<ItemStack> items = getItems(damaged);
        for (ItemStack item : items) {
            ItemMeta meta = item.getItemMeta();
            for (String lore : meta.getLore()) {
                String name = ENameParser.parseName(lore);
                if (name == null) continue;
                if (EnchantmentAPI.enchantments.containsKey(name))
                    EnchantmentAPI.enchantments.get(name).applyDefenseEffect(damaged,
                            null, ENameParser.parseLevel(lore), event);
            }
        }
    }


    /**
     * Event for tool enchantments
     *
     * @param event the event details
     */
    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamageBlock(BlockDamageEvent event) {

        // Check for enchantments and apply them
        ArrayList<ItemStack> items = getItems(event.getPlayer());
        for (ItemStack item : items) {
            ItemMeta meta = item.getItemMeta();
            for (String lore : meta.getLore()) {
                String name = ENameParser.parseName(lore);
                if (name == null) continue;
                if (EnchantmentAPI.enchantments.containsKey(name))
                    EnchantmentAPI.enchantments.get(name).applyToolEffect(event.getPlayer(),
                            event.getBlock(), event);
            }
        }
    }

    /**
     * Event for tool enchantments
     *
     * @param event the event details
     */
    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreakBlock(BlockBreakEvent event) {

        // Check for enchantments and apply them
        ArrayList<ItemStack> items = getItems(event.getPlayer());
        Bukkit.broadcastMessage("Count: " + items.size());
        for (ItemStack item : items) {
            ItemMeta meta = item.getItemMeta();
            for (String lore : meta.getLore()) {
                String name = ENameParser.parseName(lore);
                if (name == null) continue;
                if (EnchantmentAPI.enchantments.containsKey(name))
                    EnchantmentAPI.enchantments.get(name).applyToolEffect(event.getPlayer(),
                            event.getBlock(), event);
            }
        }
    }

    /**
     * Retrieves a list of equipment on the entity that have at least some lore
     *
     * @param entity the entity wearing the equipment
     * @return       the list of all equipment with lore
     */
    private ArrayList<ItemStack> getItems(LivingEntity entity) {

        // Get all equipped armor and the weapon on hand
        ItemStack[] armor = entity.getEquipment().getArmorContents();
        ItemStack weapon = entity.getEquipment().getItemInHand();
        ArrayList<ItemStack> items = new ArrayList<ItemStack>(Arrays.asList(armor));
        items.add(weapon);

        // Search for possible enchantments
        for (int i = 0; i < items.size(); i++) {
            ItemStack item = items.get(i);
            ItemMeta meta = item.getItemMeta();
            if (meta == null) {
                items.remove(item);
                i--;
                continue;
            }
            if (!meta.hasLore())  {
                items.remove(item);
                i--;
            }
        }

        return items;
    }

    /**
     * Enchantment table integration
     *
     * @param event event details
     */
    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEnchant(EnchantItemEvent event) {
        ItemStack item = event.getItem();
        for (CustomEnchantment enchantment : EnchantmentAPI.enchantments.values()) {
            if (enchantment.canEnchantOnto(item)) {
                int enchantLevel = enchantment.getEnchantmentLevel(event.getExpLevelCost());
                if (enchantLevel > 0) enchantment.addToItem(item, enchantLevel);
            }
        }
    }
}
