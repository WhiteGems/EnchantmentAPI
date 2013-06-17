package com.rit.sucy;

import com.rit.sucy.config.RootNode;
import com.rit.sucy.service.ENameParser;
import com.rit.sucy.service.ERomanNumeral;
import com.rit.sucy.service.MaterialClass;
import com.rit.sucy.service.MaterialsParser;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base class for custom enchantments
 */
public abstract class CustomEnchantment implements Comparable<CustomEnchantment>{

    /**
     * Default group that doesn't conflict with any other enchantments
     */
    public static final String DEFAULT_GROUP = "Default";

    /**
     * Name of the enchantment
     */
    protected final String enchantName;

    /**
     * Names of all the items that can receive this enchantment at an enchanting table
     */
    protected Material [] naturalItems;

    /**
     * Weight of the enchantment
     */
    protected Map<MaterialClass, Integer> weight;

    /**
     * Whether or not the enchantment is enabled
     */
    protected boolean isEnabled;

    /**
     * The conflict group of this enchantment
     */
    protected String group;

    /**
     * Creates a new custom enchantment with the given name
     *
     * @param name enchantment name
     */
    public CustomEnchantment(String name) {
        this(name, new Material[0]);
    }

    /**
     * Creates a new custom enchantment with the given name that can be
     * enchanted onto the items using an enchantment table with names
     * given in the array.
     *
     * @param name         the unique name of the enchantment
     * @param naturalItems the names of items that can normally have this enchantment
     * @deprecated use the constructor with Material[] instead
     */
    public CustomEnchantment(String name, String[] naturalItems) {
        this(name, naturalItems, 5);
    }

    /**
     * Creates a new custom enchantment with the given name that can be
     * enchanted onto the items using an enchantment table with names
     * given in the array.
     *
     * @param name         the unique name of the enchantment
     * @param naturalItems the names of items that can normally have this enchantment
     */
    public CustomEnchantment(String name, Material[] naturalItems) {
        this(name, naturalItems, 5);
    }

    /**
     * Creates a new custom enchantment with the given name that can
     * be enchanted onto the items using an enchantment table with names
     * given in the array. The chance of this enchantment occurring is
     * based on the weight (generally between 1 and 10, 1 being the most rare)
     *
     * @param name         the unique name of the enchantment
     * @param naturalItems the names of items that can normally have this enchantment
     * @param weight       the weight of the enchantment
     * @deprecated use constructor with Material[] instead instead of String[]
     */
    public CustomEnchantment(String name, String[] naturalItems, int weight) {
        this(name, MaterialsParser.toMaterial(naturalItems), DEFAULT_GROUP, weight);
    }

    /**
     * Creates a new custom enchantment with the given name that can
     * be enchanted onto the items using an enchantment table.
     *  The chance of this enchantment occurring is
     * based on the weight (generally between 1 and 10, 1 being the most rare)
     *
     * @param name         the unique name of the enchantment
     * @param naturalItems the items that can normally have this enchantment
     * @param weight       the weight of the enchantment
     */
    public CustomEnchantment(String name, Material [] naturalItems, int weight) {
        this(name, naturalItems, DEFAULT_GROUP, weight);
    }

    /**
     * Creates a new custom enchantment with the given name that can
     * be enchanted onto the items using an enchantment table that conflicts
     * with other enchantments in the same group.
     *
     * @param name         the unique name of the enchantment
     * @param naturalItems the items that can normally have this enchantment
     * @param group        the conflict group for this enchantment
     */
    public CustomEnchantment(String name, Material[] naturalItems, String group) {
        this(name, naturalItems, group, 5);
    }

    /**
     * Creates a custom enchantment with the given name that can apply to
     * the items in the list of natural items in the enchanting table and
     * conflicts with other enchantments in the same group.
     *
     * @param name         the unique name of the enchantment
     * @param naturalItems the items that can normally have this enchantment
     * @param group        the group that this enchantment conflicts with
     * @param weight       the weight of this enchantment
     */
    public CustomEnchantment(String name, Material[] naturalItems, String group, int weight) {
        Validate.notEmpty(name, "Your Enchantment needs a name!");
        Validate.notNull(naturalItems, "Input an empty array instead of \"null\"!");
        Validate.isTrue(weight >= 0, "Weight can't be negative!");

        this.enchantName = name;
        this.naturalItems = naturalItems;
        this.isEnabled = true;
        this.group = group;

        this.weight = new HashMap<MaterialClass, Integer>();
        this.weight.put(MaterialClass.DEFAULT, weight);
    }

    /**
     * Retrieves the name of the enchantment
     *
     * @return Enchantment name
     */
    public String name() {
        return enchantName;
    }

    /**
     * Set if this enchantment is enabled or not
     */
    public void setEnabled (boolean enabled)
    {
        this.isEnabled = enabled;
    }

    /**
     * Retrieves whether or not the enchantment is enabled
     *
     * @return enabled state
     */
    public boolean isEnabled() {
        return isEnabled;
    }

    /**
     * Retrieves the level of enchantment depending on the modified exp level
     *
     * @param  expLevel the experience level the player used (between 1 and 49)
     * @return          returns the enchantment level
     */
    public int getEnchantmentLevel(int expLevel) {
        return 1;
    }

    /**
     * Gets the cost per level in the anvil when this enchantment is present
     *
     * @param withBook whether or not a book was used
     * @return         cost per level
     */
    public int getCostPerLevel(boolean withBook) {
        int costIndex = weight.get(MaterialClass.DEFAULT) * getEnchantmentLevel(49);
        int divisor = withBook ? 2 : 1;
        return
            weight.get(MaterialClass.DEFAULT) == 1 ? 8 / divisor
            : costIndex < 10 ? 4 / divisor
            : costIndex < 30 ? 2 / divisor
            : 1;
    }

    /**
     * Set the items on which this enchantment can be found
     *
     * @param materials list of valid Materials for this enchantment
     */
    public void setNaturalMaterials(Material[] materials)
    {
        this.naturalItems = materials;
    }

    /**
     * Get the items on which this enchantment can naturally be found on
     *
     * @return      the names of the items
     * @deprecated use getNaturalMaterials instead
     */
    public String[] getNaturalItems(){
        String[] natItems = new String [naturalItems.length];
        for (int i = 0; i < naturalItems.length; i++)
            natItems[i] = naturalItems[i].name();
        return natItems;
    }

    /**
     * @return list of materials this item applies to normally
     */
    public Material[] getNaturalMaterials()
    {
        return naturalItems;
    }

    /**
     * Set weight for default MaterialClass
     *
     * @param weight enchantment weight
     */
    public void setWeight(int weight)
    {
        this.weight.put(MaterialClass.DEFAULT, weight);
    }

    /**
     * @return the default weight of this item
     */
    public int getWeight (){
        return weight.get(MaterialClass.DEFAULT);
    }

    /**
     * Get the weight of an Enchantment for a specific MaterialClass
     *
     * @param material  Material to get the weight for
     * @return          of the Enchantment or the DefaultWeight if not found
     */
    public int getWeight (MaterialClass material){
        return weight.containsKey(material) ? weight.get(material) : weight.get(MaterialClass.DEFAULT);
    }

    /**
     * Checks if this enchantment can be normally applied to the item.
     *
     * @param  item the item to check for
     * @return      true if the enchantment can be normally applied, false otherwise
     */
    public boolean canEnchantOnto(ItemStack item) {
        if (naturalItems == null || item == null) return false;
        for (Material validItem : naturalItems) {
            if (item.getType() == validItem) return true;
        }
        return false;
    }

    /**
     * Set the conflict group for this enchantment
     *
     * @param group the new group for this enchantment
     */
    public void setGroup (String group){
        this.group = group;
    }

    /**
     * @return conflict group
     */
    public String getGroup() {
        return group;
    }

    /**
     * Check if this CustomEnchantment conflicts with another Enchantment
     *
     * @param enchantment   to check
     * @return              true if conflicts and false if Enchantment can be applied
     */
    public boolean conflictsWith (CustomEnchantment enchantment){
        Validate.notNull(enchantment);

        return !group.equals(DEFAULT_GROUP) && group.equalsIgnoreCase(enchantment.group);
    }

    /**
     * Check for the given List of Items if they conflict with this Enchantment
     *
     * @param enchantmentsToCheck   All Enchantments to check
     *
     * @return                      if this enchant conflicts with one (or more) enchantments
     */
    public boolean conflictsWith (List<CustomEnchantment> enchantmentsToCheck){
        Validate.notNull(enchantmentsToCheck);
        for (CustomEnchantment enchantment : enchantmentsToCheck)
            if (conflictsWith(enchantment))
                return true;
        return false;
    }

    /**
     * Check for the given List of Items if they conflict with this Enchantment
     *
     * @param enchantmentsToCheck   All Enchantments to check
     *
     * @return                      if this enchant conflicts with one (or more) enchantments
     */
    public boolean conflictsWith (CustomEnchantment ... enchantmentsToCheck){
        Validate.notNull(enchantmentsToCheck);
        for (CustomEnchantment enchantment : enchantmentsToCheck)
            if (conflictsWith(enchantment))
                return true;
        return false;
    }

    /**
     * Adds this enchantment onto the given item with the enchantment level provided
     *
     * @param  item         the item being enchanted
     * @param  enchantLevel the level of enchantment
     * @return              the enchanted item
     */
    public ItemStack addToItem(ItemStack item, int enchantLevel) {
        Validate.notNull(item);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) meta = Bukkit.getServer().getItemFactory().getItemMeta(item.getType());
        List<String> metaLore = meta.getLore() == null ? new ArrayList<String>() : meta.getLore();

        // Make sure the enchantment doesn't already exist on the item
        for (Map.Entry<CustomEnchantment, Integer> entry : EnchantmentAPI.getEnchantments(item).entrySet()) {
            if (entry.getKey().name().equals(name())) {
                if (entry.getValue() < enchantLevel) {
                    metaLore.remove(ChatColor.GRAY + name() + " " + ERomanNumeral.numeralOf(entry.getValue()));
                }
                else {
                    return item;
                }
            }
        }

        // Add the enchantment
        metaLore.add(0, ChatColor.GRAY + enchantName + " " + ERomanNumeral.numeralOf(enchantLevel));
        meta.setLore(metaLore);
        String name = ENameParser.getEnchantedName(item);
        if (name != null) meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Removes this enchantment from the item if it exists
     *
     * @param item item to remove this enchantment from
     * @return     the item without this enchantment
     */
    public ItemStack removeFromItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        if (!meta.hasLore()) return item;
        List<String> metaLore = meta.getLore();

        // Make sure the enchantment doesn't already exist on the item
        for (Map.Entry<CustomEnchantment, Integer> entry : EnchantmentAPI.getEnchantments(item).entrySet()) {
            if (entry.getKey().name().equals(name())) {
                metaLore.remove(ChatColor.GRAY + name() + " " + ERomanNumeral.numeralOf(entry.getValue()));
            }
        }
        return item;
    }

    /**
     * Compare the name of the enchantment
     *
     * @param obj   Object to compare
     * @return      if Objects are equal
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CustomEnchantment)
            return this.name().equalsIgnoreCase(((CustomEnchantment) obj).name());
        return false;
    }

    /**
     * Make CustomEnchantments sortable by List.sort().
     * We just look at the name for comparison.
     *
     * @param customEnchantment to compare to
     * @return -1 if less than, 0 if equal, 1 if greater
     */
    @Override
    public int compareTo(CustomEnchantment customEnchantment) {
        return this.name().compareTo(customEnchantment.name());
    }

    /**
     * Applies the enchantment affect when attacking someone
     *
     * @param user         the entity that has the enchantment
     * @param target       the entity that was struck by the enchantment
     * @param enchantLevel the level of the used enchantment
     * @param event        the event details
     */
    public void applyEffect(LivingEntity user, LivingEntity target, int enchantLevel, EntityDamageByEntityEvent event) { }

    /**
     * Applies the enchantment defensively (when taking damage)
     *
     * @param user         the entity hat has the enchantment
     * @param target       the entity that attacked the enchantment, can be null
     * @param enchantLevel the level of the used enchantment
     * @param event        the event details (EntityDamageByEntityEvent, EntityDamageByBlockEvent, or just EntityDamageEvent)
     */
    public void applyDefenseEffect(LivingEntity user, LivingEntity target,
            int enchantLevel, EntityDamageEvent event) {}

    /**
     * Applies effects while breaking blocks (for tool effects)
     *
     * @param player the player with the enchantment
     * @param block  the block being broken
     * @param event  the event details (either BlockBreakEvent or BlockDamageEvent)
     */
    public void applyToolEffect(Player player, Block block, int enchantLevel, BlockEvent event) {}

    /**
     * Applies effects when the player left or right clicks (For other kinds of enchantments like spells)
     *
     * @param player the player with the enchantment
     * @param event  the event details
     */
    public void applyMiscEffect(Player player, int enchantLevel, PlayerInteractEvent event) {}

    /**
     * Applies effects when the item is equipped
     *
     * @param player       the player that equipped it
     * @param enchantLevel the level of enchantment
     */
    public void applyEquipEffect(Player player, int enchantLevel) {}

    /**
     * Applies effects when the item is unequipped
     *
     * @param player       the player that unequipped it
     * @param enchantLevel the level of enchantment
     */
    public void applyUnequipEffect(Player player, int enchantLevel) {}

    /**
     * Applies effects when the player interacts with an entity
     *
     * @param player       player with the enchantment
     * @param enchantLevel enchantment level
     * @param event        the event details
     */
    public void applyEntityEffect(Player player, int enchantLevel, PlayerInteractEntityEvent event) {}
}
