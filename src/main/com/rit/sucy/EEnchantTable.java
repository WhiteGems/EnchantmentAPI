package com.rit.sucy;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Handles selecting enchantments when enchanting items
 */
public class EEnchantTable {

    /**
     * Maximum tries before the enchantment stops adding enchantments
     */
    static final int MAX_TRIES = 10;

    /**
     * Enchants an item
     *
     * @param item         item to enchant
     * @param enchantLevel experience level used
     * @return             the enchanted item
     */
    public static ItemStack enchant(ItemStack item, int enchantLevel) {

        boolean chooseEnchantment = true;
        //enchants added to the item
        Map<CustomEnchantment, Integer> choosenEnchantsWithCost = new HashMap<CustomEnchantment, Integer>();
        //Build a Map where the number of keys for a certain Enchantment corresponds to the weight
        List<CustomEnchantment> validEnchants = getAllValidEnchants(item);

        // Find the total weight of all applicable enchantments
        int totalWeight = weightOfAllEnchants(validEnchants, item);

        int level = 1;

        // Keep choosing enchantments as long as needed
        while (chooseEnchantment) {

            chooseEnchantment = false;

            // Modify the enchantment level
            enchantLevel = modifiedLevel(enchantLevel, MaterialClass.getEnchantabilityFor(item.getType()));

            // Try to add an Enchantment, stop adding enchantments if the enchantment would conflict
            CustomEnchantment enchant = null;
            int tries = 0;
            do {
                enchant = weightedRandom(validEnchants, item);
                if (enchant.conflictsWith(new ArrayList<CustomEnchantment>(choosenEnchantsWithCost.keySet())))
                    continue;
                level = enchant.getEnchantmentLevel((int)(0.3 + enchantLevel * (0.29 * Math.random() + 0.7)));

                // Add the enchantment to the list
                choosenEnchantsWithCost.put(enchant, level);
                break;
            } while(tries++ < MAX_TRIES);

            // Reduce the chance of getting another one along with the power of the next one
            enchantLevel /= 2;
            if (Math.random() < (enchantLevel + 1) / 25.0) chooseEnchantment = true;

            // Books can only have a single enchantment
            if (item.getType() == Material.BOOK) chooseEnchantment = false;
        }

        // Apply the enchantments
        for (Map.Entry<CustomEnchantment, Integer> enchantCostEntry : choosenEnchantsWithCost.entrySet()) {
            CustomEnchantment selectedEnchant = enchantCostEntry.getKey();
            int levelCost = enchantCostEntry.getValue();

            if (selectedEnchant == null)
                return item; //And cancel event

            selectedEnchant.addToItem(item, levelCost);
        }

        return item;
    }

    /**
     * Calculates a modified experience level
     *
     * @param expLevel       chosen exp level
     * @param enchantability the enchantibility of the item
     * @return               modified exp level
     */
    static int modifiedLevel(int expLevel, int enchantability) {
        expLevel = expLevel + random(enchantability / 4 * 2) + 1;
        double bonus = random(0.3) + 0.85;
        return (int)(expLevel * bonus + 0.5);
    }

    /**
     * Chooses a random integer with triangular distribution
     *
     * @param max maximum value
     * @return    random integer
     */
    static int random(int max) {
        return (int)(Math.random() * max / 2 + Math.random() * max / 2);
    }

    /**
     * Chooses a random double with triangular distribution
     *
     * @param max maximum value
     * @return    random double
     */
    static double random(double max) {
        return Math.random() * max / 2 + Math.random() * max / 2;
    }

    /**
     * Get an Enchantment considering the weight (probability) of each Enchantment
     *
     * @param enchantments  possible valid enchantments
     * @param item          to get the weights for
     *
     * @return              One possible CustomEnchantment
     */
    static CustomEnchantment weightedRandom (Collection<CustomEnchantment> enchantments, ItemStack item){
        //TODO use Item

        // Compute the total weight of all items together
        int totalWeight = weightOfAllEnchants(enchantments, item);

        //select a random value between 0 and our total
        int random = new Random().nextInt(totalWeight);

        Iterator<CustomEnchantment> iter = enchantments.iterator();
        CustomEnchantment enchantment = null;

        //loop thru our weightings until we arrive at the correct one
        int current = 0;
        while (iter.hasNext()){
            enchantment = iter.next();
            current += enchantment.getWeight();
            if (random < current)
                return enchantment;
        }

        return enchantment; //or null
    }

    /**
     * Gets the total weight of all custom enchantments applicable to the item
     *
     * @param item  item type to get the weight for
     * @return      total custom enchantment weight
     */
    static int weightOfAllEnchants(Collection<CustomEnchantment> validEnchants, ItemStack item) {
        int count = 0;
        for (CustomEnchantment enchantment : validEnchants) {
            //TODO fix this design issue
            count += enchantment.getWeight();
        }
        return count;
    }

    static List<CustomEnchantment> getAllValidEnchants(ItemStack item){
        List<CustomEnchantment> validEnchantments = new ArrayList<CustomEnchantment>();

        for (CustomEnchantment enchantment : EnchantmentAPI.getEnchantments()){
            if (item.getType() == Material.BOOK)
                validEnchantments.add(enchantment);
            else if (enchantment instanceof VanillaEnchantment ? enchantment.canEnchantOnto(item) : enchantment.canEnchantOnto(item)){
                if (enchantment.name().equals("DURABILITY")
                        && !item.getType().name().contains("PICKAXE")
                        && !item.getType().name().contains("AXE")
                        && !item.getType().name().contains("SPADE")
                        && !item.getType().name().contains("HOE"))
                    continue;
                validEnchantments.add(enchantment);
            }
        }

        return validEnchantments;
    }
}
