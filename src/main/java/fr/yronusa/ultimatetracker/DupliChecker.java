package fr.yronusa.ultimatetracker;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;

public class DupliChecker implements Listener {

    @EventHandler
    public void onContainerOpen(InventoryOpenEvent e){
        ItemStack[] contents = e.getInventory().getStorageContents();
    }


    public boolean containsTrackedItem(TrackedItem i, ItemStack[] contents){
        for(ItemStack item : contents){
            if(item != null){
                return true;
            }
        }

    }
}
