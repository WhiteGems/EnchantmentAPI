package com.rit.sucy.commands;

import com.rit.sucy.CustomEnchantment;
import com.rit.sucy.EnchantmentAPI;
import com.rit.sucy.VanillaEnchantment;
import com.rit.sucy.service.ICommand;
import com.rit.sucy.service.PermissionNode;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

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
                for (CustomEnchantment enchantment : EnchantmentAPI.getEnchantments())
                    if (!(enchantment instanceof VanillaEnchantment))
                        message += ChatColor.GOLD + enchantment.name() + ChatColor.GRAY + ", ";
                message = message.substring(0, message.length() - 2);
            }
            sender.sendMessage(message);
            return false;
        }
        return false;
    }
}
