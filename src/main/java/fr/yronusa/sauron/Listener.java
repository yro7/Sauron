package fr.yronusa.sauron;

import fr.yronusa.sauron.Config.Config;
import fr.yronusa.sauron.Event.DupeDetectedEvent;
import fr.yronusa.sauron.Event.StackedItemDetectedEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class Listener implements org.bukkit.event.Listener {

    @EventHandler
    public void onHandlingItem(PlayerItemHeldEvent e){
        ItemStack item2 = e.getPlayer().getInventory().getItem(e.getNewSlot());

        if(item2 == null) return;
        ItemMutable i = new ItemMutable(e.getPlayer(), e.getNewSlot());

        if(i.hasTrackingID()){
            TrackedItem trackedItem = new TrackedItem(i);
            trackedItem.update();
            return;
        }

        if(TrackedItem.shouldBeTrack(i)){
            ItemMutable item = new ItemMutable(e.getPlayer(), e.getNewSlot());
            TrackedItem.startTracking(item);
        }
    }

    @EventHandler
    public void onContainerOpen(InventoryOpenEvent e ){
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
        }
    }

    @EventHandler
    public void onDupeDetected(DupeDetectedEvent e){
        System.out.println("DUPLICATION FOUND");
        if(e.getPlayer() != null){
            e.getPlayer().sendMessage(Config.dupeFoundPlayer);
        }

        e.getTrackedItem().quarantine();
    }

    @EventHandler
    public void onStackedItemDetected(StackedItemDetectedEvent e){
        e.getTrackedItem().quarantine();
        if(e.getPlayer() != null){
            e.getPlayer().sendMessage(Config.stackedItemPlayer);
        }
    }


}
