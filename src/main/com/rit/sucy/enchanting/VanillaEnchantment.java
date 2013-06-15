package com.rit.sucy.enchanting;

import com.rit.sucy.CustomEnchantment;
import com.rit.sucy.service.MaterialClass;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Diemex
 */
public class VanillaEnchantment extends CustomEnchantment
{
    /**
     * The Enchantment which this represents
     */
    Enchantment vanilla;
    /**
     * Allow this enchantment to be more prevalent on certain Material types
     */
    Map<MaterialClass, Integer> enchantability;
    /**
     * Value at index is the required level for that level of the enchant
     */
    int [] expLevels;

    public VanillaEnchantment(Enchantment vanilla, int weight, int [] expLevels, String name) {
        super(name, new Material[] {}, weight); //we override the method
        this.vanilla = vanilla;
        this.expLevels = expLevels;

        this.weight = new HashMap<MaterialClass, Integer>();
        this.weight.put(MaterialClass.DEFAULT, weight);

        //Set it to the default Materials, defined by bukkit
        setNaturalMaterials(getAllEnchantableMaterials());

        this.enchantability = new HashMap<MaterialClass, Integer>();
    }

    public Enchantment getVanillaEnchant() {
        return vanilla;
    }

    @Override
    public ItemStack addToItem(ItemStack item, int level) {
        item.addUnsafeEnchantment(vanilla, level);
        return item;
    }

    @Override
    public ItemStack removeFromItem(ItemStack item) {
        item.removeEnchantment(vanilla);
        return item;
    }

    /**
     * Get all the Materials onto which this enchantment can be applied by default
     *
     * @return all enchantable Materials
     */
    public Material[] getAllEnchantableMaterials() {
        List<Material> materials = new ArrayList<Material>();
        //Quick, dirty, effective if there is no method in the API
        for (Material material : Material.values())
        {
            if (material != Material.AIR) //NPE
                if (vanilla.canEnchantItem(new ItemStack(material)))
                    materials.add(material);
        }
        return materials.toArray(new Material[materials.size()]);
    }

    /**
     * Gets the level of the vanilla enchantment with the given index
     *
     * @param expLevel modified exp level
     * @return         level of enchantment
     */
    @Override
    public int getEnchantmentLevel(int expLevel) {
        for (int i = expLevels.length - 1; i >= 0; i--) {
            if (expLevel >= expLevels[i]) return i + 1;
        }
        return 1;
    }

    @Override
    public boolean conflictsWith(CustomEnchantment enchantment) {
        return  this.name() .equalsIgnoreCase       (enchantment.name()) ||
                this.name() .contains("PROTECTION") && enchantment.name().contains("PROTECTION") ||
                this.name() .contains("SILK")       && enchantment.name().contains("LOOT") ||
                this.name() .contains("LOOT")       && enchantment.name().contains("SILK") ||
                this.name() .contains("DAMAGE")     && enchantment.name().contains("DAMAGE") ;
    }
}
