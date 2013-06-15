package com.rit.sucy.config;

import com.rit.sucy.CustomEnchantment;
import com.rit.sucy.EnchantmentAPI;
import com.rit.sucy.enchanting.VanillaEnchantment;
import com.rit.sucy.service.MaterialsParser;
import com.rit.sucy.service.ModularConfig;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Configuration handler for the root config.yml file.
 */
public class RootConfig extends ModularConfig
{
    public static String baseNode = "EnchantmentAPI.";
    private String customNode = "Custom Enchantments.";
    private String vanillaNode = "Vanilla Enchantments.";

    /**
     * Constructor. You no say?
     *
     * @param plugin - plugin instance.
     */
    public RootConfig(EnchantmentAPI plugin)
    {
        super(plugin);
    }

    @Override
    public void starting()
    {
        //DO NOTHING WE HAVE TO WAIT FOR THE ENCHANTMENTS TO BE LOADED
    }

    @Override
    public void closing()
    {
        plugin.reloadConfig();
        plugin.saveConfig();
    }

    @Override
    public void save()
    {
        plugin.saveConfig();
    }

    @Override
    public void set(String path, Object value)
    {
        final ConfigurationSection config = plugin.getConfig();
        config.set(path, value);
        plugin.saveConfig();
    }

    @Override
    public void reload()
    {
        plugin.reloadConfig();
        loadDefaults(plugin.getConfig());
        loadSettings(plugin.getConfig());
        boundsCheck();
        loadEnchantments(plugin.getConfig());
        writeConfig();
    }

    @Override
    public void loadSettings(ConfigurationSection config)
    {
        for (final RootNode node : RootNode.values())
        {
            updateOption(node, config);
        }
    }

    @Override
    public void loadDefaults(ConfigurationSection config)
    {
        for (RootNode node : RootNode.values())
        {
            if (!config.contains(node.getPath()))
            {
                config.set(node.getPath(), node.getDefaultValue());
            }
        }
    }

    /**
     * Loads settings from the config and updates the enchantments in memory
     */
    public void loadEnchantments (ConfigurationSection config)
    {
        Collection<CustomEnchantment> enchantments = EnchantmentAPI.getEnchantments();
        for (CustomEnchantment enchantment : enchantments)
        {
            String section = enchantment instanceof VanillaEnchantment ? vanillaNode : customNode;
            for (EnchantmentNode node : EnchantmentNode.values())
            {
                String path = baseNode + section + enchantment.name() + node.getPath();
                if (config.contains(path))
                {
                    Object obj = config.get(path);
                    switch(node)
                    {
                        case ENABLED:
                            if (obj instanceof Boolean)
                                enchantment.setEnabled((Boolean) obj);
                            break;
                        case ITEMS:
                            if (obj instanceof List)
                            {
                                @SuppressWarnings("unchecked")
                                List<String> stringList = (List<String>) obj;
                                Material[] materials = MaterialsParser.toMaterial(stringList.toArray(new String[stringList.size()]));
                                enchantment.setNaturalMaterials(materials);
                            }
                            break;
                        case WEIGHT:
                            if (obj instanceof Integer)
                                enchantment.setWeight((Integer) obj);
                            break;
                        default:
                            throw new UnsupportedOperationException("The node " + node.name() + " hasn't been configured yet");
                    }
                }
            }
        }
    }

    /**
     * Writes the config to file.
     * First the settings.
     * then the vanilla enchantment settings.
     * and last the custom enchantment settings.
     */
    public void writeConfig ()
    {
        FileConfiguration config = plugin.getConfig();
        YamlConfiguration out = new YamlConfiguration();
        //Normal Settings
        if (RootNode.values().length > 0)
        {
            for (RootNode node : RootNode.values())
            {
                out.set(node.getPath(), config.get(node.getPath()));
            }
        }

        //Separate vanilla from custom enchants
        List<CustomEnchantment> customEnchantments = new ArrayList<CustomEnchantment>(EnchantmentAPI.getEnchantments());
        List<VanillaEnchantment> vanillaEnchantments = new ArrayList<VanillaEnchantment>();
        Iterator<CustomEnchantment> iter = customEnchantments.iterator();
        while (iter.hasNext())
        {
            CustomEnchantment customEnchant = iter.next();
            if (customEnchant instanceof VanillaEnchantment)
            {
                vanillaEnchantments.add((VanillaEnchantment) customEnchant);
                iter.remove();
            }
        }

        //Vanilla Enchantments
        Collections.sort(vanillaEnchantments);
        for (VanillaEnchantment vanillaEnchantment : vanillaEnchantments)
        {
            String base = baseNode + vanillaNode + vanillaEnchantment.name();
            //Enabled
            out.set(base + EnchantmentNode.ENABLED.getPath(), vanillaEnchantment.isEnabled());
            //Weight
            out.set(base + EnchantmentNode.WEIGHT.getPath(), vanillaEnchantment.getWeight());
            //Items
            out.set(base + EnchantmentNode.ITEMS.getPath(), MaterialsParser.toStringArray(vanillaEnchantment.getNaturalMaterials()));
        }

        //Custom Enchantments
        Collections.sort(customEnchantments);
        for (CustomEnchantment customEnchantment : customEnchantments)
        {
            String base = baseNode + customNode + customEnchantment.name();
            //Enabled
            out.set(base + EnchantmentNode.ENABLED.getPath(), customEnchantment.isEnabled());
            //Weight
            out.set(base + EnchantmentNode.WEIGHT.getPath(), customEnchantment.getWeight());
            //Items
            out.set(base + EnchantmentNode.ITEMS.getPath(), MaterialsParser.toStringArray(customEnchantment.getNaturalMaterials()));
        }

        try {
            String path = plugin.getDataFolder().getAbsolutePath() + File.separator + "config.yml"; //so we can see the var in debugger
            out.save(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void boundsCheck() {

    }
}