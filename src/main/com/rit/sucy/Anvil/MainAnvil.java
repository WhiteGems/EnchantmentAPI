package com.rit.sucy.Anvil;

import net.minecraft.server.v1_5_R3.ContainerAnvil;
import net.minecraft.server.v1_5_R3.ContainerAnvilInventory;
import net.minecraft.server.v1_5_R3.EntityHuman;
import net.minecraft.server.v1_5_R3.IInventory;
import org.bukkit.craftbukkit.v1_5_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_5_R3.inventory.CraftInventoryAnvil;
import org.bukkit.craftbukkit.v1_5_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;

public class MainAnvil implements AnvilView {

    final Player player;
    final Plugin plugin;
    CraftInventoryAnvil inv;
    ContainerAnvil anvil;
    int repairCost;

    public MainAnvil(Plugin plugin, Inventory anvil, Player player) {
        this.player = player;
        this.plugin = plugin;

        inv = (CraftInventoryAnvil)anvil;
        try {
            Field container = ContainerAnvilInventory.class.getDeclaredField("a");
            container.setAccessible(true);
            this.anvil = (ContainerAnvil)container.get(inv.getInventory());
        }
        catch (Exception e) {
            // -.-
        }
    }

    /**
     * Retrieves the text from the anvil name field
     *
     * @return name field text
     */
    public String getNameText() {
        try {
            // Gross
            Field textField = ContainerAnvil.class.getDeclaredField("m");
            textField.setAccessible(true);
            String name = (String)textField.get(anvil);
            if (name == null)
                return null;

            // More gross
            Field g = ContainerAnvil.class.getDeclaredField("g");
            g.setAccessible(true);
            net.minecraft.server.v1_5_R3.ItemStack item = ((IInventory)g.get(anvil)).getItem(0);
            if (item == null)
                return null;

            // Disgusting
            Field n = ContainerAnvil.class.getDeclaredField("n");
            n.setAccessible(true);
            if (name.equalsIgnoreCase(((EntityHuman)n.get(anvil)).getLocale().c(item.a())))
                return null;

            // Much better
            else if (name.equals(item.getName()))
                return null;

            // Finally, we're done T_T
            return name;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ItemStack[] getInputSlots() {
        ItemStack[] input = new ItemStack[2];
        input[0] = CraftItemStack.asCraftMirror(inv.getIngredientsInventory().getItem(0));
        input[1] = CraftItemStack.asCraftMirror(inv.getIngredientsInventory().getItem(1));
        return input;
    }

    @Override
    public ItemStack[] getInputSlots(int slot, ItemStack newItem) {
        ItemStack[] input = new ItemStack[2];
        input[0] = slot == 0 ? newItem : CraftItemStack.asCraftMirror(inv.getIngredientsInventory().getItem(0));
        input[1] = slot == 1 ? newItem : CraftItemStack.asCraftMirror(inv.getIngredientsInventory().getItem(1));
        return input;
    }

    @Override
    public int getInputSlotID(int input) {
        return input - 1;
    }

    @Override
    public void setResultSlot(final ItemStack result) {
        plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
            @Override
            public void run() {
                if (result == null)
                    inv.getResultInventory().setItem(0, null);
                else
                    inv.getResultInventory().setItem(0, CraftItemStack.asNMSCopy(result));
                ((CraftPlayer) player).getHandle().setContainerData(anvil, 0, anvil.a);
            }
        });
    }

    @Override
    public ItemStack getResultSlot() {
        return CraftItemStack.asCraftMirror(inv.getResultInventory().getItem(0));
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public void setRepairCost(final int repairCost) {
        this.repairCost = repairCost;
        plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
            @Override
            public void run() {
                try {
                    anvil.a = repairCost;
                    ((CraftPlayer) player).getHandle().setContainerData(anvil, 0, anvil.a);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public int getRepairCost() {
        return repairCost;
    }

    @Override
    public boolean isInputSlot(int slot) {
        return slot == 0 || slot == 1;
    }

    @Override
    public int getResultSlotID() {
        return 2;
    }

    @Override
    public void clearInputs() {
        plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
            @Override
            public void run() {
                inv.getIngredientsInventory().setItem(0, null);
                inv.getIngredientsInventory().setItem(1, null);
                ((CraftPlayer) player).getHandle().setContainerData(anvil, 0, anvil.a);
            }
        });
    }

    @Override
    public void close() {
    }

    @Override
    public Inventory getInventory() {
        return inv;
    }
}
