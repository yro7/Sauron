package fr.yronusa.ultimatetracker.Event;

import fr.yronusa.ultimatetracker.Database;
import fr.yronusa.ultimatetracker.TrackedItem;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.sound.midi.Track;
import java.util.UUID;

public class DupeDetectedEvent extends Event {


    private final TrackedItem item;


    private final Player player;

    private static final HandlerList HANDLERS_LIST = new HandlerList();

    public DupeDetectedEvent(TrackedItem item, Player player){
        this.player = player;
        this.item = item;
    }


    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    public TrackedItem getTrackedItem() {
        return this.item;
    }

    public ItemStack getItemStack(){
        return this.item.getItem();
    }

    public UUID getUUID(){
        return this.item.getOriginalID();
    }

    public String getItemLastUpdate(){
        return this.item.getLastUpdate();
    }

    public String getDatabaseLastUpdate(){
        return Database.getLastUpdate(this.getTrackedItem().getOriginalID());
    }

    public Player getPlayer(){
        return this.player;
    }



    public Location getLocation(){
        return this.getPlayer().getLocation();
    }

}
