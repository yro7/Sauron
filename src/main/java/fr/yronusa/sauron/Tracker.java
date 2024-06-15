package fr.yronusa.sauron;

import fr.yronusa.sauron.Config.Config;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The class that contains all listeners that are used to perform look up on the server, to find items to track/update.
 */
public class Tracker implements org.bukkit.event.Listener {

    public static HashSet<Location> checkedInventories;

    public static BukkitRunnable currentPlayersCheck;

    public static List<BukkitRunnable> inventoriesUpdatingTask;

    public void execute(Inventory inventory, ItemStack item){
        if(item==null){
            return;
        }
        ItemMutable i = new ItemMutable(item, inventory);

        if(i.hasTrackingID()){
            System.out.println("has tracking id ouloulou");
            new TrackedItem(i).update();
            return;
        }

        if(i.shouldBeTrack()){
            TrackedItem.startTracking(i);
        }



    }

    public static void initialize(){
        checkedInventories = new HashSet<>();
        inventoriesUpdatingTask = new ArrayList<>();
        if(Config.automaticInventoryUpdating) updatePlayersInventorySafe();

      //  updateYronusa();

        // Automatically clears the list of containers that have been checked every x time
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
        execute(inv, inv.getItem(slot));
        System.out.println("has NOTTT AdDDZ");


    }

    /**
     * Allows to check the containers open by players.
     * It is a better use than {@link org.bukkit.event.inventory.InventoryOpenEvent} which causes incompabilities with
     * plugins (such as auctions plugin, crates plugin or inventory/ec viewing plugins).
     *
     * @param e The {@link PlayerInteractEvent} that triggered the method
     */
    @EventHandler
    public void checkItemInOpenedChest(PlayerInteractEvent e){
        Block block = e.getClickedBlock();
        if(block != null && block.getState() instanceof Container container){
            updateInventorySafely(container.getInventory());
        }
    }
    public static void updateTest(Player p){
        new BukkitRunnable() {
            @Override
            public void run() {
                System.out.println("SAURON DEBUG: UPDATE YRONUSA...");
                updateInventorySafely(p.getInventory());
            }
        }.runTaskTimer(Sauron.getInstance(), 0L, 20L*4);


    }


    /**
     * Safely updates the inventory of all online players.
     * This method iterates through each online player's inventory and updates the items with tracking IDs.
     * It uses a more gentle approach by checking one player's inventory at a time to reduce database requests.
     */
    public static void updatePlayersInventorySafe() {
        BukkitRunnable checksAllPlayers = new BukkitRunnable() {
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
                        while (!p.isOnline()) {
                            // Skip disconnected & exempts players
                            if (onlinePlayersDeque.isEmpty()) {
                                // If there are no more players to check, cancel the task
                                this.cancel();
                                return;
                            }
                            p = onlinePlayersDeque.pop();
                        }
                        Log.console("Now checking " + p.getName() + "'s inventory.", Log.Level.LOW);
                        Tracker.updateInventorySafely(p.getInventory());
                    }
                }.runTaskTimer(Sauron.getInstance(), 0, Config.delayBetweenPlayers * 20L);
            }
        };


        checksAllPlayers.runTaskTimer(Sauron.getInstance(), 0, Config.delayBetweenChecks * 20L);
        currentPlayersCheck = checksAllPlayers;
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

        // If the inventory has recently been checked, cancel the operation.
        if(checkedInventories.contains(inventory.getLocation())) return;
        checkedInventories.add(inventory.getLocation());

        int size = inventory.getSize();

        // This position counter will increment each time the task is repeated, allowing to check items one after one with
        // a delay between.
        AtomicInteger position = new AtomicInteger(0);
        BukkitRunnable updateInventory = new BukkitRunnable() {
            @Override
            public void run() {
                int counter = position.get();
                ItemMutable itemToCheck = new ItemMutable(inventory.getItem(counter), inventory);
                // Find the next item with a tracking ID
                // (and perform check for illegals item, through ItemMutable's constructor).
                while (counter < size-1) {

                    // If we found a tracking item, we update it and then return;
                    // so we wait before eventually updating another item.
                    if (itemToCheck.hasTrackingID()) {
                        TrackedItem trackedItem = new TrackedItem(itemToCheck);
                        if(trackedItem.shouldUpdate()){
                            trackedItem.update();
                            counter++;
                            position.set(counter);
                            return;
                        }
                    }

                    if(itemToCheck.shouldBeTrack()){
                        TrackedItem.startTracking(itemToCheck);
                        counter++;
                        position.set(counter);
                        return;
                    }
                    counter++;
                    itemToCheck = new ItemMutable(inventory.getItem(counter),inventory);
                }

                // At this point, we are at the end of the inventory, so we cancel the task
                Tracker.inventoriesUpdatingTask.remove(this);
                this.cancel();

                position.set(counter);
            }
        };

        Tracker.inventoriesUpdatingTask.add(updateInventory);

        updateInventory.runTaskTimer(Sauron.getInstance(), 0, (Config.delayBetweenItems+1));
    }





}
