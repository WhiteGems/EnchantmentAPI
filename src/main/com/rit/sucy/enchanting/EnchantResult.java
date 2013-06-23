package com.rit.sucy.enchanting;

import com.rit.sucy.CustomEnchantment;
import org.bukkit.inventory.ItemStack;

public class EnchantResult {

    private ItemStack item;
    private int level = -1;
    private CustomEnchantment firstEnchant;

    public EnchantResult() {}

    public int getLevel() {
        return level;
    }

    public ItemStack getItem() {
        return item;
    }

    public CustomEnchantment getFirstEnchant() {
        return firstEnchant;
    }

    public void setLevel(int value) {
        if (level == -1)
            level = value;
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }

    public void setFirstEnchant(CustomEnchantment enchant) {
        if (firstEnchant == null)
            firstEnchant = enchant;
    }
}
