package com.rit.sucy.enchanting;

import com.rit.sucy.CustomEnchantment;
import org.bukkit.enchantments.Enchantment;

/**
 * Holds information about Enchantments in the vanilla game
 * like their weight, and the exp levels required to get a specific lvl of an enchantment
 */
public enum VanillaData
{
    /**
     * ARMOR
     */
    PROTECTION_ENVIRONMENTAL (Enchantment.PROTECTION_ENVIRONMENTAL, "Damage_Reduction",
            10, new int [] {1, 5, 15, 25}),
    PROTECTION_FALL(Enchantment.PROTECTION_FALL,"Damage_Reduction",
            5,  new int [] {1, 5, 15, 20}),
    PROTECTION_FIRE(Enchantment.PROTECTION_FIRE, "Damage_Reduction",
            5,  new int [] {1, 5, 15, 25}),
    PROTECTION_PROJECTILE(Enchantment.PROTECTION_PROJECTILE, "Damage_Reduction",
            5,  new int [] {1, 5, 15, 25}),
    WATER_WORKER(Enchantment.WATER_WORKER,
            2,  new int [] {1}),
    PROTECTION_EXPLOSIONS(Enchantment.PROTECTION_EXPLOSIONS, "Damage_Reduction",
            2,  new int [] {1, 5, 20, 25}),
    OXYGEN(Enchantment.OXYGEN,
            2,  new int [] {1, 8, 25}),
    THORNS(Enchantment.THORNS,
            1,  new int [] {1, 12, 30}),

    /**
     * WEAPONS
     */
    DAMAGE_ALL(Enchantment.DAMAGE_ALL, "Bonus_Damage",
            10, new int [] {1, 5, 15, 25, 35}),
    DAMAGE_ARTHROPODS(Enchantment.DAMAGE_ARTHROPODS, "Bonus_Damage",
            5,  new int [] {1, 5, 15, 20, 32}),
    KNOCKBACK(Enchantment.KNOCKBACK,
            5,  new int [] {1, 8}),
    DAMAGE_UNDEAD(Enchantment.DAMAGE_UNDEAD, "Bonus_Damage",
            5,  new int [] {1, 5, 15, 20, 32}),
    FIRE_ASPECT(Enchantment.FIRE_ASPECT,
            2,  new int [] {1, 5}),
    LOOT_BONUS_MOBS(Enchantment.LOOT_BONUS_MOBS,
            2,  new int [] {1, 8, 32}),

    /**
     * TOOLS
     */
    DIG_SPEED(Enchantment.DIG_SPEED,
            10, new int [] {1, 5, 15, 25, 35}),
    DURABILITY(Enchantment.DURABILITY,
            5,  new int [] {1, 5, 25}),
    LOOT_BONUS_BLOCKS(Enchantment.LOOT_BONUS_BLOCKS, "Block_Modifier",
            2,  new int [] {1, 8, 32}),
    SILK_TOUCH(Enchantment.SILK_TOUCH, "Block_Modifier",
            1,  new int [] {1}),

    /**
     * BOW
     */
    ARROW_DAMAGE(Enchantment.ARROW_DAMAGE,
            10, new int [] {1, 5, 15, 25, 35}),
    ARROW_FIRE(Enchantment.ARROW_FIRE,
            2,  new int [] {1}),
    ARROW_KNOCKBACK(Enchantment.ARROW_KNOCKBACK,
            2,  new int [] {1, 8}),
    ARROW_INFINITE(Enchantment.ARROW_INFINITE,
            1,  new int [] {1}),
    ;
    /**
     * The Enchantment id in the vanilla game
     */
    private final Enchantment enchantment;

    /**
     * The conflict group of the enchantment
     */
    private final String group;

    /**
     * The weight this enchantment has when enchants are choosen
     */
    private final int enchantWeight;

    /**
     * The value at a given index corresponds to the experience levels required to get this enchantment
     */
    private final int[] levels;

    /**
     * Private Constructor for this enum
     *
     * @param enchantment   - The Enchantment id in the vanilla game
     * @param enchantWeight - The weight this enchantment has when enchants are chosen
     * @param levels        - The value at a given index corresponds to the experience levels required to get this enchantment
     */
    private VanillaData(Enchantment enchantment, int enchantWeight, int[] levels){
        this(enchantment, CustomEnchantment.DEFAULT_GROUP, enchantWeight, levels);
    }

    /**
     * Private constructor for this enum
     *
     * @param enchantment   - The Enchantment id in the vanilla game
     * @param group         - The conflict group of the enchantment
     * @param enchantWeight - The weight of this enchantment has when enchants are chosen
     * @param levels        - The value at a given index corresponds to the experience levels required to get this enchantment
     */
    private VanillaData(Enchantment enchantment, String group, int enchantWeight, int[] levels) {
        this.enchantment = enchantment;
        this.group = group;
        this.enchantWeight = enchantWeight;
        this.levels = levels;
    }

    /**
     * Get the experience levels a player needs for a specific level of the enchantment
     *
     * @return an int array, the value at the index is the experience you need for an enchant of that lvl (the index)
     */
    public int[] getLevels() {
        return levels;
    }

    /**
     * Get the weight of the enchantment (used when choosing which enchant we should choose)
     *
     * @return the weight of the enchant
     */
    public int getEnchantWeight() {
        return enchantWeight;
    }

    /**
     * Get the group of the enchantment
     *
     * @return the group of the enchant
     */
    public String getGroup() {
        return group;
    }

    /**
     * Get the actual Enchantment id from the Vanilla Enchantment enum
     *
     * @return the vanilla enchantment
     */
    public Enchantment getEnchantment() {
        return enchantment;
    }
}
