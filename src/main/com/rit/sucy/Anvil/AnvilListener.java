package com.rit.sucy.Anvil;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Hashtable;

public class AnvilListener implements Listener {

    private final Plugin plugin;

    private final Hashtable<String, AnvilView> views = new Hashtable<String, AnvilView>();

    public AnvilListener(Plugin plugin) {
        // Register listeners
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Opens the custom inventory instead of the default anvil inventory
     *
     * @param event event details
     */
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getType() == Material.ANVIL) {
            Player player = event.getPlayer();

            try{
                MainAnvil anvil = new MainAnvil(plugin, player);
                views.put(player.getName(), anvil);
            }
            catch (Exception e) {
                event.setCancelled(true);
                plugin.getLogger().info("-.- it broke already");
                e.printStackTrace();
                CustomAnvil anvil = new CustomAnvil(plugin, player);
                views.put(player.getName(), anvil);
            }
        }
    }

    /**
     * Gives back any items when the inventory is closed
     *
     * @param event event details
     */
    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (views.containsKey(event.getPlayer().getName())) {
            views.get(event.getPlayer().getName()).close();
            views.remove(event.getPlayer().getName());
        }
    }

    /**
     * Handles anvil transactions
     *
     * @param event event details
     */
    @EventHandler
    public void onClick(final InventoryClickEvent event) {
        final Player player = event.getWhoClicked() instanceof Player ? (Player)event.getWhoClicked() : null;

        // Make sure the inventory is the custom inventory
        if (player != null && views.containsKey(player.getName()))
        {
            if (views.get(player.getName()) instanceof MainAnvil)
                ((MainAnvil) views.get(player.getName())).setInv(event.getInventory());
            if (views.get(player.getName()).getInventory().getName().equals(event.getInventory().getName()))
            {
                AnvilView view = views.get(player.getName());
                ItemStack[] inputs = view.getInputSlots();
                boolean top = event.getRawSlot() < view.getInventory().getSize();
                if (event.getSlot() == -999) return;

                if (event.isShiftClick()) {

                    // Shift-clicking out one of the components will clear the end-product if there was one
                    if (view.isInputSlot(event.getRawSlot())) {
                        ItemStack[] items = view.getInputSlots(event.getSlot(), null);
                        AnvilMechanics.updateResult(view, items);
                    }

                    // Shift-clicking out the end-product will cost the player and consume the components
                    else if (event.getRawSlot() == view.getResultSlotID() && isFilled(view.getResultSlot())) {
                        if (player.getGameMode() != GameMode.CREATIVE && (view.getRepairCost() > player.getLevel() || view.getRepairCost() >= 40)) {
                            event.setCancelled(true);
                        }
                        else {
                            view.clearInputs();
                            if (player.getGameMode() != GameMode.CREATIVE)
                                player.setLevel(player.getLevel() - view.getRepairCost());
                        }
                    }

                    // Don't allow clicking in other slots in the anvil
                    else if (top) {
                        event.setCancelled(true);
                    }

                    // Don't allow shift clicking into the product slot
                    else if (areFilled(inputs[0], inputs[1])) {
                        event.setCancelled(true);
                    }

                    // Update the product slot when needed
                    else if (isFilled(inputs[0])) {
                        ItemStack[] items = view.getInputSlots(view.getInputSlotID(2), event.getCurrentItem());
                        AnvilMechanics.updateResult(view, items);
                    }
                    else if (isFilled(inputs[1])) {
                        ItemStack[] items = view.getInputSlots(view.getInputSlotID(1), event.getCurrentItem());
                        AnvilMechanics.updateResult(view, items);
                    }
                }
                else if (event.isLeftClick()) {

                    // update the product slot if the components are changed
                    if (event.getRawSlot() == view.getInputSlotID(1)) {
                        AnvilMechanics.updateResult(view, view.getInputSlots(view.getInputSlotID(1), event.getCursor()));
                    }
                    else if (event.getRawSlot() == view.getInputSlotID(2)) {
                        AnvilMechanics.updateResult(view, view.getInputSlots(view.getInputSlotID(2), event.getCursor()));
                    }

                    // Same as shift-clicking out the product
                    else if (event.getRawSlot() == view.getResultSlotID() && !isFilled(event.getCursor()) && isFilled(view.getResultSlot())) {
                        if (player.getGameMode() != GameMode.CREATIVE && (view.getRepairCost() > player.getLevel() || view.getRepairCost() >= 40)) {
                            event.setCancelled(true);
                        }
                        else {
                            view.clearInputs();
                            if (player.getGameMode() != GameMode.CREATIVE)
                                player.setLevel(player.getLevel() - view.getRepairCost());
                        }
                    }

                    // Don't allow clicks in other slots of the anvil
                    else if (top) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    private boolean isFilled(ItemStack item) {
        return item != null && item.getType() != Material.AIR;
    }

    private boolean areFilled(ItemStack item1, ItemStack item2) {
        return isFilled(item1) && isFilled(item2);
    }
}
