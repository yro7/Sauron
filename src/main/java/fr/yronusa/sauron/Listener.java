package fr.yronusa.sauron;

import fr.yronusa.sauron.Config.Config;
import fr.yronusa.sauron.Event.BlacklistedItemDetectedEvent;
import fr.yronusa.sauron.Event.DupeDetectedEvent;
import fr.yronusa.sauron.Event.StackedItemDetectedEvent;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;

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
        Log.blacklistFound(e.getUUID(), e.getPlayer(), e.getPlayer().getLocation(), e.getTrackedItem().getBase64());
        if(e.getPlayer() != null){
            e.getPlayer().sendMessage(Config.blacklistedItemPlayer);
        }
        e.getTrackedItem().quarantine();
    }
}
