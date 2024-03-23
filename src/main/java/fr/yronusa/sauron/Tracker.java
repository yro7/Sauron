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
import org.bukkit.scheduler.BukkitTask;

import java.util.Collection;
import java.util.Stack;

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


    public static void updatePlayersInventory(){
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
                Stack<Player> onlinePlayersStack = new Stack<>();
                onlinePlayersStack.addAll(onlinePlayers);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if(onlinePlayers.isEmpty()){
                            this.cancel();
                            return;
                        }

                        Player p = onlinePlayersStack.pop();
                        System.out.println("NOW UPDATING PLAYER " + p.getDisplayName());
                        while(!p.isOnline()){
                            p = onlinePlayersStack.pop();
                        }
                        Tracker.updateInventorySafely(p.getInventory());
                    }
                }.runTaskTimer(Sauron.getInstance(), 0, 2*20);
                // Checks the inventory one-by-one to be more gentle with the database requests, if there are a lot of
                // item to update. 2s interval between each inventory check.

            }
        }.runTaskTimer(Sauron.getInstance(), 0, 10*20*5);


    }

    public static void updateInventorySafely(Inventory inventory){

        int size = inventory.getSize();
        int[] position = {0};
        new BukkitRunnable() {
            @Override
            public void run() {
                System.out.println("NOW CHECKING ITEM AT POSITION " + position[0]);
                while(!ItemMutable.hasTrackingID(inventory.getItem(position[0]))){
                    if(position[0] >= size){
                        this.cancel();
                        return;
                    }
                    position[0]++;
                }

                ItemMutable itemToCheck = new ItemMutable(inventory.getItem(position[0]), inventory, position[0]);
                new TrackedItem(itemToCheck).update();
                position[0]++;

                if(position[0] > size){
                    this.cancel();
                }


                // Checks the inventory's content one-by-one to be more gentle with the database requests.

            }
        }.runTaskTimer(Sauron.getInstance(), 0, Config.automaticUpdateInterval);


    }



}
