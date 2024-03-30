package fr.yronusa.sauron.Event;

import fr.yronusa.sauron.ItemMutable;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;


/**
 * Is called when an illegal item is detected.
 * Illegal items are the ones defined in the config, in rules.illegal-items section.
 */
public class IllegalItemDetectedEvent extends Event implements Cancellable {

    private final ItemMutable item;

    private static final HandlerList HANDLERS_LIST = new HandlerList();

    public IllegalItemDetectedEvent(ItemMutable item) {
        this.item = item;
    }


    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    public ItemMutable getItemMutable() {
        return this.item;
    }

    public ItemStack getItemStack(){
        return this.item.getItem();
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public void setCancelled(boolean b) {
    }

    public Player getPlayer(){
        return this.getItemMutable().getPlayer();
    }


}
