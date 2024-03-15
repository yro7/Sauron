package fr.yronusa.sauron.Event;

import fr.yronusa.sauron.TrackedItem;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;
import java.util.UUID;

public class DatabaseItemBlacklistEvent extends Event {
    private final TrackedItem item;
    private static final HandlerList HANDLERS_LIST = new HandlerList();

    public DatabaseItemBlacklistEvent(TrackedItem item){
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

    public Timestamp getLastUpdate(){
        return this.item.getLastUpdateItem();
    }

}
