package fr.yronusa.sauron.Event;

import fr.yronusa.sauron.ItemMutable;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;
import java.util.UUID;

public class ItemUpdateDateEvent extends Event implements Cancellable {


    private final ItemMutable item;



    private static final HandlerList HANDLERS_LIST = new HandlerList();

    public ItemUpdateDateEvent(ItemMutable item){
        this.item = item;
    }

    public ItemMutable getItem() {
        return this.item;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    public ItemStack getItemStack(){
        return this.item.getItem();
    }

    public UUID getUUID(){
        return this.item.getID();
    }

    public Timestamp getItemLastUpdate(){
        return this.item.getLastUpdate();
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public void setCancelled(boolean b) {

    }
}
