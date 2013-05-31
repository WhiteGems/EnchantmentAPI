package com.rit.sucy;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

class EListener implements Listener {

    // Plugin reference
    EnchantmentAPI plugin;

    // Constructor
    public EListener(EnchantmentAPI plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    // Applies enchantments on hit
    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHit(EntityDamageByEntityEvent event) {

        // Rule out cases where enchantments don't apply
        if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) return;
        if (!(event.getDamager() instanceof LivingEntity)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;

        // Check for enchantments and apply them
        LivingEntity damager = (LivingEntity)event.getDamager();
        ItemStack item = damager.getEquipment().getItemInHand();
        ItemMeta meta = item.getItemMeta();
        for (String lore : meta.getLore()) {
            String name = ENameParser.parseName(lore);
            if (name == null) continue;
            if (EnchantmentAPI.enchantments.containsKey(name))
                EnchantmentAPI.enchantments.get(name).applyEffect(damager,
                        (LivingEntity)event.getEntity(),
                        ENameParser.parseLevel(lore));
        }
    }

    // Applies enchantments on enchantment table use
    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEnchant(EnchantItemEvent event) {
        ItemStack item = event.getItem();
        for (CustomEnchantment enchantment : EnchantmentAPI.enchantments.values()) {
            if (enchantment.canEnchantOnto(item)) {
                int enchantLevel = enchantment.getEnchantmentLevel(event.getExpLevelCost());
                event.getEnchanter().sendMessage("Exp: " + event.getExpLevelCost() + ", Level: " + enchantLevel);
                if (enchantLevel > 0) enchantment.addToItem(item, enchantLevel);
            }
        }
    }
}
