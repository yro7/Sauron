package fr.yronusa.sauron;

import fr.yronusa.sauron.Config.Config;
import fr.yronusa.sauron.Event.BlacklistedItemDetectedEvent;
import fr.yronusa.sauron.Event.DupeDetectedEvent;
import fr.yronusa.sauron.Event.IllegalItemDetectedEvent;
import fr.yronusa.sauron.Event.StackedItemDetectedEvent;
import org.bukkit.event.EventHandler;


/**
 * The class that deals with Sauron's listeners, such as {@link DupeDetectedEvent} or {@link BlacklistedItemDetectedEvent}.
 */
public class Listener implements org.bukkit.event.Listener {

    @EventHandler
    public void onDupeDetected(DupeDetectedEvent e){
        Log.dupe(e.getUUID(), e.getPlayer(), e.getLocation(), e.getTrackedItem().getBase64(), e.getItemLastUpdate(), e.getDatabaseLastUpdate());
        if(e.getPlayer() != null){
            e.getPlayer().sendMessage(Config.dupeFoundPlayer);
        }
        e.getTrackedItem().quarantine();
    }

    @EventHandler
    public void onStackedItemDetected(StackedItemDetectedEvent e){
        Log.stackedFound(e.getUUID(), e.getPlayer(), e.getPlayer().getLocation(), e.getItemStack().getAmount(), e.getTrackedItem().getBase64());
        if(e.getPlayer() != null){
            e.getPlayer().sendMessage(Config.stackedItemPlayer);
        }
        e.getTrackedItem().quarantine();
    }

    @EventHandler
    public void clearBlacklistedItems(BlacklistedItemDetectedEvent e){
        Log.blacklistFound(e.getUUID(), e.getPlayer(), e.getTrackedItem().getItemMutable().getInventory().getLocation(), e.getTrackedItem().getBase64());
        if(e.getPlayer() != null){
            e.getPlayer().sendMessage(Config.blacklistedItemPlayer);
        }
        e.getTrackedItem().quarantine();
    }

    @EventHandler
    public void clearIllegalItem(IllegalItemDetectedEvent e){
        Log.illegalItemFound(e.getPlayer(), e.getItemMutable().getInventory().getLocation(), e.getItemMutable().getBase64());
        if(e.getPlayer() != null){
            e.getPlayer().sendMessage(Config.illegalItemPlayer);
        }

        e.getItemMutable().delete();

    }
}
