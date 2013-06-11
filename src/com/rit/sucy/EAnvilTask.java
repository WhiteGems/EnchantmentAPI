package com.rit.sucy;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;

/**
 * Used to create custom outputs for the anvil
 * (currently not functioning properly)
 */
/*
public class EAnvilTask extends BukkitRunnable {

    Inventory anvil;

    public EAnvilTask(Inventory anvil) {
        this.anvil = anvil;
    }

    public void run() {
        if (anvil.getItem(0) == null || anvil.getItem(1) == null) return;
        if (anvil.getItem(0).getType() == anvil.getItem(1).getType()) {
            ItemStack item1 = anvil.getItem(0);
            ItemStack item2 = anvil.getItem(1);
            Map<CustomEnchantment, Integer> enchants1 = EnchantmentAPI.getEnchantments(anvil.getItem(0));
            Map<CustomEnchantment, Integer> enchants2 = EnchantmentAPI.getEnchantments(anvil.getItem(1));
            if (enchants1.size() > 0 || enchants2.size() > 0
                    || item1.getItemMeta().hasEnchants() || item2.getItemMeta().hasEnchants()) {
                ItemStack item = new ItemStack(item1.getType());
                item.addUnsafeEnchantments(item1.getEnchantments());
                item.addEnchantments(item2.getEnchantments());
                for (Map.Entry<CustomEnchantment, Integer> enchant : enchants1.entrySet())
                    enchant.getKey().addToItem(item, enchant.getValue());
                for (Map.Entry<CustomEnchantment, Integer> enchant : enchants2.entrySet())
                    enchant.getKey().addToItem(item, enchant.getValue());
                anvil.setItem(2, item);
            }
        }
    }
}
*/