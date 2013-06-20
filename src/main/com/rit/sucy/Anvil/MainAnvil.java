package com.rit.sucy.Anvil;

import net.minecraft.server.v1_5_R3.ContainerAnvil;
import net.minecraft.server.v1_5_R3.ContainerAnvilInventory;
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

    public MainAnvil(Plugin plugin, Player player) {
        this.player = player;
        this.plugin = plugin;

        // Run a random nms script to make sure its working
        CraftItemStack.asCraftMirror(new net.minecraft.server.v1_5_R3.ItemStack(1, 1, 1));
    }

    public void setInv(Inventory anvil) {
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
    public void setResultSlot(ItemStack result) {
        if (result == null) {
            inv.getResultInventory().setItem(0, null);
        }
        else
            inv.getResultInventory().setItem(0, CraftItemStack.asNMSCopy(result));
        ((CraftPlayer) player).getHandle().setContainerData(anvil, 0, anvil.a);
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
    public void setRepairCost(int repairCost) {
        this.repairCost = repairCost;
        try {
            anvil.a = repairCost;
            ((CraftPlayer) player).getHandle().setContainerData(anvil, 0, anvil.a);
        }
        catch (Exception e){
            e.printStackTrace();
        }
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
        inv.getIngredientsInventory().setItem(0, null);
        inv.getIngredientsInventory().setItem(1, null);
        ((CraftPlayer) player).getHandle().setContainerData(anvil, 0, anvil.a);
    }

    @Override
    public void close() {
    }

    @Override
    public Inventory getInventory() {
        return inv;
    }
}
