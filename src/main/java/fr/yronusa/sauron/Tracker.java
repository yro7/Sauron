package fr.yronusa.sauron;

import fr.yronusa.sauron.Config.Config;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

public class Tracker implements org.bukkit.event.Listener {

    public static HashSet<Location> checkedInventories;

    public static BukkitRunnable currentPlayersCheck;

    public void execute(Inventory inventory, ItemStack item, int slot){
        if(item==null){
            return;
        }

        ItemMutable i = new ItemMutable(item, inventory, slot);
        if(i.hasTrackingID()){
            new TrackedItem(i).update();
            return;
        }

        if(TrackedItem.shouldBeTrack(i)){
            TrackedItem.startTracking(i);
        }

    }

    public static void initialize(){
        if(Config.automaticInventoryUpdating) updatePlayersInventorySafe();

        new BukkitRunnable() {
            @Override
            public void run() {
                checkedInventories.clear();
            }
        }.runTaskTimer(Sauron.getInstance(), 0,20L*Config.containerUpdateInterval);
    }
    @EventHandler
    public void trackHandledItems(PlayerItemHeldEvent e){
        Player p = e.getPlayer();
        Inventory inv = p.getInventory();
        int slot = e.getNewSlot();
        execute(inv, inv.getItem(slot),slot);
    }

    @EventHandler
    public void trackMovedItems(InventoryMoveItemEvent e){
        TrackedItem.update(e.getDestination());
        TrackedItem.update(e.getInitiator());
    }

    @EventHandler
    public void checkItemInOpenedChest(PlayerInteractEvent e){
        Block block = e.getClickedBlock();
        if(block != null && block.getState() instanceof Container container){
            updateInventorySafely(container.getInventory());
        }
    }


    /**
     * Safely updates the inventory of all online players.
     * This method iterates through each online player's inventory and updates the items with tracking IDs.
     * It uses a more gentle approach by checking one player's inventory at a time to reduce database requests.
     */
    public static void updatePlayersInventorySafe() {
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers().stream().filter(p->!p.hasPermission("sauron.exempt")).toList();
                ArrayDeque<Player> onlinePlayersDeque = new ArrayDeque<>(onlinePlayers);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (onlinePlayersDeque.isEmpty()) {
                            // If there are no more online players to check, cancel the task
                            this.cancel();
                            return;
                        }

                        Player p = onlinePlayersDeque.pop();
                        while (!p.isOnline() && p.hasPermission("sauron.exempt")) {
                            // Skip disconnected & exempts players
                            if (onlinePlayersDeque.isEmpty()) {
                                // If there are no more players to check, cancel the task
                                this.cancel();
                                return;
                            }
                            p = onlinePlayersDeque.pop();
                        }
                        System.out.println("[SAURON] Now checking " + p.getName() + "'s inventory.");
                        Tracker.updateInventorySafely(p.getInventory());
                    }
                }.runTaskTimer(Sauron.getInstance(), 0, Config.delayBetweenPlayers * 20L);
            }
        };

        task.runTaskTimer(Sauron.getInstance(), 0, Config.delayBetweenChecks * 20L);
        currentPlayersCheck = task;
    }


    /**
     * Iterates through the inventory, checks for items with tracking IDs,
     * and updates them accordingly.
     * This method will wait every item update/startTracking to ease the database.
     * If the inventory has previously been updated, the method will be cancelled.
     * The list of recently updated inventories is cleared
     *
     * @param inventory The inventory to update safely.
     */
    public static void updateInventorySafely(Inventory inventory) {

        if(checkedInventories.contains(inventory.getLocation())) return;

        System.out.println("UPDATING A CHEST...");
        checkedInventories.add(inventory.getLocation());
        int size = inventory.getSize();
        AtomicInteger position = new AtomicInteger(0);
        new BukkitRunnable() {
            @Override
            public void run() {
                int counter = position.get();
                if (counter >= size) {
                    // If reached the end of the inventory, cancel the task
                    this.cancel();
                    return;
                }

                // Find the next item with a tracking ID
                while (counter < size && !ItemMutable.hasTrackingID(inventory.getItem(counter))) {
                    counter++;
                }

                if (counter < size) {
                    // If found an item with a tracking ID, create a TrackedItem and update it
                    ItemMutable itemToCheck = new ItemMutable(inventory.getItem(counter), inventory, counter);
                    new TrackedItem(itemToCheck).update();
                    counter++;
                    position.set(counter);
                }
            }
        }.runTaskTimer(Sauron.getInstance(), 0, Config.automaticUpdateInterval);
    }





}
