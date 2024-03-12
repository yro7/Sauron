package fr.yronusa.ultimatetracker;

import fr.yronusa.ultimatetracker.Config.Config;
import fr.yronusa.ultimatetracker.Config.TrackingRule;
import fr.yronusa.ultimatetracker.Database.Database;
import fr.yronusa.ultimatetracker.Event.DupeDetectedEvent;
import fr.yronusa.ultimatetracker.Event.ItemClearEvent;
import fr.yronusa.ultimatetracker.Event.ItemStartTrackingEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
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

    // Used to create a new Tracked Item.
    public TrackedItem(ItemMutable item, UUID originalID, Timestamp date) {
        this.item = item;
        this.originalID = originalID;
        this.lastUpdate = date;
    }


    // Used to get the associated TrackedItem from an ItemMutable directly.
    public TrackedItem(ItemMutable item) {
        this.item = item;
        this.originalID = item.getID();
        this.lastUpdate = item.getLastUpdate();

    }

    public static boolean shouldBeTrack(ItemMutable i) {
        /**if(i.getItem() != null){
            return true;
        }

        return false;
        **/
        for(TrackingRule rule : Config.trackingRules){
            rule.print();
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

    public static TrackedItem startTracking(ItemMutable item){

        if(item.hasTrackingID()){
            System.out.println("[ULTIMATE TRACKER] Error: The item is already tracked.");
            return null;
        }

        else{
            UUID originalID = UUID.randomUUID();
            item.setTrackable(originalID, UltimateTracker.getActualDate());
            TrackedItem trackedItem = new TrackedItem(item);

            if(UltimateTracker.database) {
                Database.add(trackedItem);
            }

            ItemStartTrackingEvent trackEvent = new ItemStartTrackingEvent(trackedItem);
            Bukkit.getPluginManager().callEvent(trackEvent);

            return trackedItem;
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

    public void update(boolean forceUpdate) {

        if(!forceUpdate && !shouldUpdate()){
            return;
        }

        Timestamp newDate = UltimateTracker.getActualDate();
        CompletableFuture<Boolean> isDupli = CompletableFuture.supplyAsync(() -> Database.isDuplicated(this));
        isDupli.exceptionally(error -> {
            Database.update(this.getOriginalID(), newDate);
            this.getItemMutable().updateDate(newDate);
            return false;
        });
        isDupli.thenAccept((res) -> {
            if(res){
                if(this.getPlayer() != null){
                    this.getPlayer().sendMessage(Config.dupeFoundPlayer);

                }

                DupeDetectedEvent dupeDetectEvent = new DupeDetectedEvent(this, this.getPlayer());
                ItemClearEvent itemClearEvent = new ItemClearEvent(this, this.getPlayer(), ItemClearEvent.ClearReason.DUPE_DETECTED);
                Bukkit.getPluginManager().callEvent(dupeDetectEvent);
                Bukkit.getPluginManager().callEvent(itemClearEvent);
                if(!itemClearEvent.isCancelled()){
                    this.quarantine();
                }

            }

            else{
                Database.update(this.getOriginalID(), newDate);
                this.getItemMutable().updateDate(newDate);
            }
        });
    }

    public void update() {
        update(false);
    }

    private boolean shouldUpdate() {
        Timestamp itemTimestamp = this.getLastUpdateItem();
        Timestamp actualTime = UltimateTracker.getActualDate();
        long difference = actualTime.getTime() - itemTimestamp.getTime();
        return difference / 1000 >= Config.delay;
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
        this.update(true);
    }

}
