package fr.yronusa.ultimatetracker;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemHeldEvent;
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

}
