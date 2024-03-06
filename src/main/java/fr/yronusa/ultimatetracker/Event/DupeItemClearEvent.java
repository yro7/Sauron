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

import java.sql.Timestamp;
import java.util.UUID;

public class DupeItemClearEvent extends Event implements Cancellable {


    private final TrackedItem item;


    private final Player player;

    private static final HandlerList HANDLERS_LIST = new HandlerList();

    public DupeItemClearEvent(TrackedItem item, Player player){
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

    public UUID getTrackedItem() {
        return this.item.getOriginalID();
    }

    public ItemStack getItemStack(){
        return this.item.getItem();
    }

    public UUID getUUID(){
        return this.item.getOriginalID();
    }

    public Timestamp getItemLastUpdate(){
        return this.item.getLastUpdate();
    }

    public Timestamp getDatabaseLastUpdate(){
        return Database.getLastUpdate(this.getTrackedItem());
    }

    public Player getPlayer(){
        return this.player;
    }



    public Location getLocation(){
        return this.getPlayer().getLocation();
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public void setCancelled(boolean b) {

    }
}
