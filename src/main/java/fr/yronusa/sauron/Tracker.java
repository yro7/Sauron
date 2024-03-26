package fr.yronusa.sauron;

import fr.yronusa.sauron.Config.Config;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

public class Tracker implements org.bukkit.event.Listener {

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
    public void checkItemInContainer(InventoryOpenEvent e){
        ItemStack[] content = e.getInventory().getContents();
        Inventory inventory = e.getInventory();

        int position = 0;
        for(ItemStack item : content){
            if(item == null) {
                position++;
                continue;
            }

            if(ItemMutable.hasTrackingID(item)){
                ItemMutable itemMutable = new ItemMutable(item, inventory, position);
                (new TrackedItem(itemMutable)).update(false);
            }

            position++;
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
                Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
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
                        while (!p.isOnline()) {
                            // Skip disconnected players
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
     *
     * @param inventory The inventory to update safely.
     */
    public static void updateInventorySafely(Inventory inventory) {
        int size = inventory.getSize();
        AtomicInteger position = new AtomicInteger(0);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (position.get() >= size) {
                    // If reached the end of the inventory, cancel the task
                    this.cancel();
                    return;
                }

                // Find the next item with a tracking ID
                while (position.get() < size && !ItemMutable.hasTrackingID(inventory.getItem(position.get()))) {
                    position.incrementAndGet();
                }

                if (position.get() < size) {
                    // If found an item with a tracking ID, create a TrackedItem and update it
                    ItemMutable itemToCheck = new ItemMutable(inventory.getItem(position.get()), inventory, position.get());
                    new TrackedItem(itemToCheck).update();
                    position.incrementAndGet();
                }
            }
        }.runTaskTimer(Sauron.getInstance(), 0, Config.automaticUpdateInterval);
    }





}
