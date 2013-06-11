package com.rit.sucy;

import org.bukkit.Bukkit;
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
    PROTECTION_ENVIRONMENTAL (Enchantment.PROTECTION_ENVIRONMENTAL,
            10, new int [] {1, 15, 25, 35}),
    PROTECTION_FALL(Enchantment.PROTECTION_FALL,
            5,  new int [] {1, 15, 20, 25}),
    PROTECTION_FIRE(Enchantment.PROTECTION_FIRE,
            5,  new int [] {1, 20, 30, 35}),
    PROTECTION_PROJECTILE(Enchantment.PROTECTION_PROJECTILE,
            5,  new int [] {1, 15, 20, 25}),
    WATER_WORKER(Enchantment.WATER_WORKER,
            2,  new int [] {1}),
    PROTECTION_EXPLOSIONS(Enchantment.PROTECTION_EXPLOSIONS,
            2,  new int [] {1, 15, 25, 30}),
    OXYGEN(Enchantment.OXYGEN,
            2,  new int [] {1, 30, 40}),
    THORNS(Enchantment.THORNS,
            1,  new int [] {1, 45, 65}),

    /**
     * WEAPONS
     */
    DAMAGE_ALL(Enchantment.DAMAGE_ALL,
            10, new int [] {1, 15, 25, 40, 50}),
    DAMAGE_ARTHROPODS(Enchantment.DAMAGE_ARTHROPODS,
            5,  new int [] {1, 20, 25, 35, 50}),
    KNOCKBACK(Enchantment.KNOCKBACK,
            5,  new int [] {1, 40}),
    DAMAGE_UNDEAD(Enchantment.DAMAGE_UNDEAD,
            5,  new int [] {1, 20, 25, 35, 50}),
    FIRE_ASPECT(Enchantment.FIRE_ASPECT,
            2,  new int [] {1, 50}),
    LOOT_BONUS_MOBS(Enchantment.LOOT_BONUS_MOBS,
            2,  new int [] {1, 30, 60}),

    /**
     * TOOLS
     */
    DIG_SPEED(Enchantment.DIG_SPEED,
            10, new int [] {1, 30, 40, 50, 60}),
    DURABILITY(Enchantment.DURABILITY,
            5,  new int [] {1, 30, 40}),
    LOOT_BONUS_BLOCKS(Enchantment.LOOT_BONUS_BLOCKS,
            2,  new int [] {1, 40, 50}),
    SILK_TOUCH(Enchantment.SILK_TOUCH,
            1,  new int [] {1}),

    /**
     * BOW
     */
    ARROW_DAMAGE(Enchantment.ARROW_DAMAGE,
            10, new int [] {1, 15, 25, 35, 45}),
    ARROW_FIRE(Enchantment.ARROW_FIRE,
            2,  new int [] {1}),
    ARROW_KNOCKBACK(Enchantment.ARROW_KNOCKBACK,
            2,  new int [] {1, 35}),
    ARROW_INFINITE(Enchantment.ARROW_INFINITE,
            1,  new int [] {1}),
    ;
    /**
     * The Enchantment id in the vanilla game
     */
    private final Enchantment enchantment;
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
     * @param enchantWeight - The weight this enchantment has when enchants are choosen
     * @param levels        - The value at a given index corresponds to the experience levels required to get this enchantment
     */
    private VanillaData(Enchantment enchantment, int enchantWeight, int[] levels){
        this.enchantment = enchantment;
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
     * Get the actual Enchantment id from the Vanilla Enchantment enum
     *
     * @return the vanilla enchantment
     */
    public Enchantment getEnchantment() {
        return enchantment;
    }
}
