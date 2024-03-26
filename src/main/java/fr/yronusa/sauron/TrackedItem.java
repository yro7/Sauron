package fr.yronusa.sauron;

import fr.yronusa.sauron.Config.Config;
import fr.yronusa.sauron.Config.TrackingRule;
import fr.yronusa.sauron.Database.Database;
import fr.yronusa.sauron.Event.BlacklistedItemDetectedEvent;
import fr.yronusa.sauron.Event.DupeDetectedEvent;
import fr.yronusa.sauron.Event.ItemStartTrackingEvent;
import fr.yronusa.sauron.Event.StackedItemDetectedEvent;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayOutputStream;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;


public class TrackedItem {


    public ItemMutable item;
    public UUID originalID;
    public Timestamp lastUpdate; // Last update of the item at format YYYY.MM.DD.hh.mm

    // Used to get the associated TrackedItem from an ItemMutable directly.
    // The ItemMutable is necessary to be able to update data on the item more easily.
    public TrackedItem(ItemMutable item) {
        this.item = item;
        this.originalID = item.getID();
        this.lastUpdate = item.getLastUpdate();

    }

    public static boolean shouldBeTrack(ItemMutable i) {
        ItemStack item = i.getItem();
        if(item == null) return false;
        if(!Config.trackStackedItems && item.getAmount() != 1) return false;

        for(TrackingRule rule : Config.trackingRules){
            if(rule.test(i.getItem())) return true;
        }

        return false;
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
        // Check if the item already has a tracking ID
        if (item.hasTrackingID()) {
            // If yes, create a TrackedItem and update it
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
                Database.add(trackedItem);
            }

            // Trigger event for item tracking start
            ItemStartTrackingEvent trackEvent = new ItemStartTrackingEvent(trackedItem);
            Bukkit.getPluginManager().callEvent(trackEvent);

            return trackedItem;
        }
    }


    public static void update(Inventory inv){
        int position = 0;
        for(ItemStack i : inv.getContents()) {

            ItemMutable item = new ItemMutable(i, inv, position);
            if(item.hasTrackingID()){
                (new TrackedItem(item)).update();
                return;
            }
            if(shouldBeTrack(item)){
                startTracking(item);
            }
        }
    }

    public Player getPlayer(){
        if(this.getItemMutable().getInventory().getHolder() instanceof Player p){
            return p;
        }

        return null;
    }


    public String getBase64(){
        String base = itemStackArrayToBase64(new ItemStack[]{this.getItem()});
        return base.replaceAll("\\n", "");
    }

    public Timestamp getLastUpdateItem(){
        return this.getItemMutable().getLastUpdate();
    }

    /**
     * Updates the item with optional force update flag.
     *
     * @param forceUpdate A boolean flag indicating whether to force the update regardless of other conditions.
     *                    If true, the update will be executed even if the item doesn't meet update criteria.
     *                    If false, the update will be skipped unless {@link #shouldUpdate()} returns true.
     */
    public void update(boolean forceUpdate) {
        // Check if the update should be skipped based on conditions
        if (!forceUpdate && !shouldUpdate()) {
            return;
        }

        // Check for stacked items and trigger event if configured to clear stacked items
        if (this.getItem() != null && Config.clearStackedItems && this.getItem().getAmount() > 1) {
            StackedItemDetectedEvent stackedItemDetected = new StackedItemDetectedEvent(this);
            Bukkit.getPluginManager().callEvent(stackedItemDetected);
            return;
        }

        // Asynchronously check if the item is blacklisted or duplicated
        CompletableFuture<Boolean> isBlacklisted = CompletableFuture.supplyAsync(() -> Database.isBlacklisted(this)).exceptionally(error -> false);
        CompletableFuture<Boolean> isDupli = CompletableFuture.supplyAsync(() -> Database.isDuplicated(this)).exceptionally(error -> {
            // Handle duplication check error and update the item if necessary
            Timestamp newDate = Sauron.getActualDate();
            if (Sauron.database) Database.update(this, newDate);
            this.getItemMutable().updateDate(newDate);
            return false;
        });

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

        // Process the combined results
        combinedResult.thenAccept(resPair -> {
            if (resPair.getLeft()) {
                // Trigger event for blacklisted item detection
                BlacklistedItemDetectedEvent blacklistDetectEvent = new BlacklistedItemDetectedEvent(this);
                Bukkit.getScheduler().runTask(Sauron.getInstance(), () -> Bukkit.getPluginManager().callEvent(blacklistDetectEvent));
                return;
            }

            if (resPair.getRight()) {
                // Trigger event for duplicated item detection
                DupeDetectedEvent dupeDetectEvent = new DupeDetectedEvent(this, this.getPlayer());
                Bukkit.getScheduler().runTask(Sauron.getInstance(), () -> Bukkit.getPluginManager().callEvent(dupeDetectEvent));
                return;
            }

            // If the item is neither blacklisted nor duplicated, update it
            Timestamp newDate = Sauron.getActualDate();
            if(Sauron.database) Database.update(this, newDate);
            this.getItemMutable().updateDate(newDate);
        });
    }


    public void update() {
        update(false);
    }

    private boolean shouldUpdate() {
        Timestamp itemTimestamp = this.getLastUpdateItem();
        Timestamp actualTime = Sauron.getActualDate();
        long difference = actualTime.getTime() - itemTimestamp.getTime();
        return difference / 1000 >= Config.updateInterval;
    }


    public static String itemStackArrayToBase64(ItemStack[] items) throws IllegalStateException {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeInt(items.length);
            for (int i = 0; i < items.length; i++) {
                dataOutput.writeObject(items[i]);
            }
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save item stacks.", e);
        }
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
