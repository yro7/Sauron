package fr.yronusa.sauron;

import fr.yronusa.sauron.Commands.Command;
import fr.yronusa.sauron.Config.Config;
import fr.yronusa.sauron.Event.DupeDetectedEvent;
import fr.yronusa.sauron.Event.StackedItemDetectedEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class Listener implements org.bukkit.event.Listener {


    public void execute(Player p, Inventory inventory, ItemStack item, int slot){
        if(Command.doNotTrackPlayers.containsKey(p) || item==null){
            return;
        }

        ItemMutable i = new ItemMutable(item, inventory, slot);
        if(i.hasTrackingID()){
            new TrackedItem(i).update();
        }

    }
    @EventHandler
    public void trackHandledItems(PlayerItemHeldEvent e){
        if(Command.doNotTrackPlayers.containsKey(e.getPlayer())){
            return;
        }
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

        verifyTrack
    }

    @EventHandler
    public void trackTookItems(InventoryClickEvent e){
        Player p = (Player) e.getInventory().getHolder();
        if(Command.doNotTrackPlayers.containsKey(p)){
            return;
        }
        ItemStack item2 = p.getInventory().getItem(e.getNewSlot());
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
    public void checkItemInContainer(InventoryOpenEvent e ){
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



}
