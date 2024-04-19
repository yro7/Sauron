package fr.yronusa.sauron;

import fr.yronusa.sauron.Config.Config;
import fr.yronusa.sauron.Database.Database;
import fr.yronusa.sauron.Event.BlacklistedItemDetectedEvent;
import fr.yronusa.sauron.Event.DupeDetectedEvent;
import fr.yronusa.sauron.Event.ItemStartTrackingEvent;
import fr.yronusa.sauron.Event.StackedItemDetectedEvent;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;


/**
 * The principal object of the plugin.
 * Represents the items that have a sauron_uuid nbt, which are the ones tracked by the plugin.
 */
public class TrackedItem {


    public ItemMutable item;
    public UUID originalID;
    public Timestamp lastUpdate; // Last update of the item at format YYYY.MM.DD.hh.mm

    /** Used to get the associated TrackedItem from an {@link ItemMutable} directly.
     * The ItemMutable is necessary to be able to update data on the item more easily.
     * @param item the ItemMutable which will define the TrackedItem.
     */
    // Used to get the associated TrackedItem from an ItemMutable directly.
    // The ItemMutable is necessary to be able to update data on the item more easily.
    public TrackedItem(ItemMutable item) {
        this.item = item;
        this.originalID = item.getID();
        this.lastUpdate = item.getLastUpdate();
    }


    public String getBase64(){
        return this.getItemMutable().getBase64();
    }

    public Timestamp getLastUpdateItem(){
        return this.getItemMutable().getLastUpdate();
    }

    public UUID getOriginalID() {
        return originalID;
    }
    public ItemStack getItem() {
        return this.item.item;
    }

    public List<InventoryLocation> getLastInventories(){
        return null;
    }

    public ItemMutable getItemMutable() {
        return this.item;
    }

    /**
     * Starts tracking the given item.
     *
     * @param item The item to start tracking.
     * @return The TrackedItem object representing the tracked item.
     */
    public static TrackedItem startTracking(ItemMutable item) {

        if(item.getPlayer() != null && item.getPlayer().hasPermission("sauron.exempt")) return null;

        // Check if the item already has a tracking ID
        if (item.hasTrackingID()) {
            // If yes, create a TrackedItem object to update it
            TrackedItem trackedItem = new TrackedItem(item);
            trackedItem.update();
            return trackedItem;
        } else {
            // If no tracking ID exists, generate a new one and start tracking
            UUID originalID = UUID.randomUUID();
            item.setTrackable(originalID, Sauron.getActualDate());
            TrackedItem trackedItem = new TrackedItem(item);

            // Add the item to the database if enabled
            if (Sauron.database) {
                Database.addTrackedItem(trackedItem);
            }

            // Trigger event for item tracking start
            ItemStartTrackingEvent trackEvent = new ItemStartTrackingEvent(trackedItem);
            Bukkit.getPluginManager().callEvent(trackEvent);

            return trackedItem;
        }
    }


    /**
     * Tries to get the player that triggered the process which led to the creation of the TrackedItem.
     * In some cases, that player won't exist, for example if the plugin scanned a container that no player was looking onto.
     * a {@link TrackedItem} object doesn't to have a player to works as expected, but it is useful for logging purposes.
     *
     * @return the {@link Player} associated with the Tracked Item if found, null otherwise.
     */
    public Player getPlayer() {
        return this.getItemMutable().getPlayer();
    }


    /**
     * Updates the item with optional force update flag.
     * Performs all necessaries check : is the item duplicated ? Is it blacklisted ?
     *
     * @param forceUpdate A boolean flag indicating whether to force the update regardless of other conditions.
     *                    If true, the update will be executed even if the item doesn't meet update criteria.
     *                    If false, the update will be skipped unless {@link #shouldUpdate()} returns true.
     */
    public void update(boolean forceUpdate) {
        // Check if the update should be skipped based on conditions
        // Cancel if the player is exempt or if the item shouldnt be updated
        if (!forceUpdate && !shouldUpdate() ||
            this.getPlayer() != null && this.getPlayer().hasPermission("sauron.exempt")) {
                return;
        }

        // Check for stacked items and trigger event if configured to clear stacked items
        if (this.getItem() != null && Config.clearStackedItems && this.getItem().getAmount() > 1) {
            StackedItemDetectedEvent stackedItemDetected = new StackedItemDetectedEvent(this);
            Bukkit.getPluginManager().callEvent(stackedItemDetected);
            return;
        }

        Log.console("Begin updating UUID " + this.getOriginalID() + " ...", Log.Level.DEBUG);
        // Asynchronously check if the item is blacklisted or duplicated
        CompletableFuture<Boolean> isBlacklisted = CompletableFuture.supplyAsync(() -> Database.isBlacklisted(this)).exceptionally(error -> false);
        CompletableFuture<Boolean> isDupli = CompletableFuture.supplyAsync(() -> Database.isDuplicated(this)).exceptionally(error -> false);
        // If for any particular reason the supplyAsync fails, we simply suppose that the item  wasn't duplicated or blacklisted and pursue with a
        // normal check.

        // Combine the results of blacklist and duplication checks
        CompletableFuture<Pair<Boolean, Boolean>> combinedResult = isBlacklisted.thenCombine(isDupli, (blacklist, dupe) -> new Pair<>() {
            @Override
            public Boolean setValue(Boolean value) {
                return null;
            }

            @Override
            public Boolean getLeft() {
                if (blacklist == null) return false;
                return blacklist;
            }

            @Override
            public Boolean getRight() {
                return dupe;
            }
        });
        combinedResult.thenAccept(resPair ->
                {

                TrackedItem item = this;
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        {
                            if (resPair.getLeft()) {
                                // Trigger event for blacklisted item detection
                                BlacklistedItemDetectedEvent blacklistDetectEvent = new BlacklistedItemDetectedEvent(item);
                                Bukkit.getScheduler().runTask(Sauron.getInstance(), () -> Bukkit.getPluginManager().callEvent(blacklistDetectEvent));
                                return;
                            }

                            // If a tracked item was updated during a crash, the anti-dupe will be triggered after the reboot.
                            // In that case, we just bypass the dupe check and allow the item to be updated.

                            // If the item was in fact really duplicated, the duplicated one will be cleared at the next look-up.

                            if (resPair.getRight() && !Database.wasUpdatedBeforeCrash(item)) {
                                // Trigger event for duplicated item detection
                                DupeDetectedEvent dupeDetectEvent = new DupeDetectedEvent(item, item.getPlayer());
                                Bukkit.getScheduler().runTask(Sauron.getInstance(), () -> Bukkit.getPluginManager().callEvent(dupeDetectEvent));
                                return;

                            }


                            // If the item is neither blacklisted nor duplicated, update it
                            Timestamp newDate = Sauron.getActualDate();
                            try{
                                item.getItemMutable().updateDate(newDate);
                                if(Sauron.database) Database.update(item, newDate);
                            } catch(IllegalArgumentException e){
                                e.printStackTrace();
                            }
                        }
                    }
                }.runTask(Sauron.getInstance());
                });

    }


    public void update() {
        update(false);
    }

    /**
     * Is the timestamp on the item old enough to allow the item to be updated ?
     * Can be bypassed using the "true" flag option in {@link TrackedItem#update(boolean)}}.
     * @return true if yes, false otherwise
     */
    public boolean shouldUpdate() {
        Timestamp itemTimestamp = this.getLastUpdateItem();
        Timestamp actualTime = Sauron.getActualDate();
        long difference = actualTime.getTime() - itemTimestamp.getTime();
        return difference / 1000 >= Config.inHandUpdateInterval;
    }


    public void quarantine(){
        this.getItemMutable().delete();
    }

    public void resetUUID() {
        UUID newID = UUID.randomUUID();
        this.originalID = newID;
        this.getItemMutable().changeUUID(newID);
    }

}
