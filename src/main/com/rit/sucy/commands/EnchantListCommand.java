package com.rit.sucy.commands;

import com.rit.sucy.CustomEnchantment;
import com.rit.sucy.EnchantmentAPI;
import com.rit.sucy.enchanting.VanillaEnchantment;
import com.rit.sucy.service.ICommand;
import com.rit.sucy.service.PermissionNode;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;

/**
 * List all custom Enchantments
 */
public class EnchantListCommand implements ICommand
{
    @Override
    public boolean execute(EnchantmentAPI plugin, CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission(PermissionNode.LIST.getNode()))
        {
            String message = ChatColor.GREEN + "Registered enchantments: ";
            if (EnchantmentAPI.getEnchantments().size() > 0) {
                ArrayList<CustomEnchantment> enchants = new ArrayList<CustomEnchantment>(EnchantmentAPI.getEnchantments());
                Collections.sort(enchants);
                for (CustomEnchantment enchantment : enchants)
                    if (!(enchantment instanceof VanillaEnchantment))
                        message += ChatColor.GOLD + enchantment.name() + ChatColor.GRAY + ", ";
                message = message.substring(0, message.length() - 2);
            }
            sender.sendMessage(message);
            return true;
        }
        return false;
    }
}
