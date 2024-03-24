package fr.yronusa.sauron;

import fr.yronusa.sauron.Config.Config;
import fr.yronusa.sauron.Event.DupeDetectedEvent;
import fr.yronusa.sauron.Event.StackedItemDetectedEvent;
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

    @EventHandler
    public void onDupeDetected(DupeDetectedEvent e){
        Log.dupe(e.getUUID(), e.getPlayer(), e.getLocation());
        if(e.getPlayer() != null){
            e.getPlayer().sendMessage(Config.dupeFoundPlayer);
        }
        e.getTrackedItem().quarantine();
    }

    @EventHandler
    public void onStackedItemDetected(StackedItemDetectedEvent e){
        Log.stackedFound(e.getUUID(), e.getPlayer(), e.getPlayer().getLocation(), e.getItemStack().getAmount());
        if(e.getPlayer() != null){
            e.getPlayer().sendMessage(Config.stackedItemPlayer);
        }
        e.getTrackedItem().quarantine();
    }


    public static void updatePlayersInventorySafe(){
        new BukkitRunnable() {
            @Override
            public void run() {
                Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
                ArrayDeque<Player> onlinePlayersDeque = new ArrayDeque<>(onlinePlayers);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if(onlinePlayersDeque.isEmpty()){
                            this.cancel();
                            return;
                        }

                        Player p = onlinePlayersDeque.pop();
                        while(!p.isOnline()){
                            if(onlinePlayersDeque.isEmpty()){
                                this.cancel();
                                return;
                            }
                            p = onlinePlayersDeque.pop();
                        }
                        System.out.println("[SAURON] Now checking " + p.getName() + "'s inventory.");
                        Tracker.updateInventorySafely(p.getInventory());
                        return;
                    }
                }.runTaskTimer(Sauron.getInstance(), 0, 2*20);
                // Checks the inventory one-by-one to be more gentle with the database requests, if there are a lot of
                // item to update. 2s interval between each inventory check.
            }
        }.runTaskTimer(Sauron.getInstance(), 0, 5*20*60);
        // every 5 minutes: update each players inventory (safely)


    }

    public static void updateInventorySafely(Inventory inventory) {
        int size = inventory.getSize();
        AtomicInteger position = new AtomicInteger(0);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (position.get() >= size) {
                    this.cancel();
                    return;
                }

                while (position.get() < size && !ItemMutable.hasTrackingID(inventory.getItem(position.get()))) {
                    position.incrementAndGet();
                }

                if (position.get() < size) {
                    ItemMutable itemToCheck = new ItemMutable(inventory.getItem(position.get()), inventory, position.get());
                    new TrackedItem(itemToCheck).update();
                    position.incrementAndGet();
                }
            }
        }.runTaskTimer(Sauron.getInstance(), 0, Config.automaticUpdateInterval);
    }




}
