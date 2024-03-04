package fr.yronusa.ultimatetracker.Event;

import fr.yronusa.ultimatetracker.TrackedItem;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.sound.midi.Track;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class ItemUpdateDateEvent extends Event {

    private final TrackedItem item;

    private final String newDate;
    private static final HandlerList HANDLERS_LIST = new HandlerList();

    public ItemUpdateDateEvent(TrackedItem item, String newDate){
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

    public String getOldDate(){
        return this.item.getLastUpdate();
    }

    public String getNewDate(){
        return this.newDate;
    }

}
