package fr.yronusa.ultimatetracker.Event;

import fr.yronusa.ultimatetracker.TrackedItem;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;
import java.util.UUID;

public class ItemUpdateDateEvent extends Event {

    private final TrackedItem item;

    private final Timestamp newDate;
    private static final HandlerList HANDLERS_LIST = new HandlerList();

    public ItemUpdateDateEvent(TrackedItem item, Timestamp newDate){
        this.item = item;
        this.newDate = newDate;
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

    public Timestamp getOldDate(){
        return this.item.getLastUpdateItem();
    }

    public Timestamp getNewDate(){
        return this.newDate;
    }

}
