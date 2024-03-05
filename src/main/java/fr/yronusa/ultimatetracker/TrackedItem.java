package fr.yronusa.ultimatetracker;

import com.jojodmo.safeNBT.api.SafeNBT;
import fr.yronusa.ultimatetracker.Config.TrackingRule;
import fr.yronusa.ultimatetracker.Event.ItemStartTrackingEvent;
import fr.yronusa.ultimatetracker.Event.ItemUpdateDateEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import javax.sound.midi.Track;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public class TrackedItem {

    public ItemMutable item;
    public UUID originalID;
    public String lastUpdate; // Last update of the item at format YYYY.MM.DD.hh.mm

    // Used to create a new Tracked Item.
    public TrackedItem(ItemMutable item, UUID originalID, String date) {
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
        if(i.getItem() != null){
            return true;
        }

        return false;
        /**
        for(TrackingRule rule : UltimateTracker.getTrackingRules()){
            if(rule.test(i.getItem())) return true;
        }
        return false;**/
    }

    public boolean isDuplicated(){
        return this.isDatedBeforeThan(Database.getLastUpdate(this.getOriginalID()));
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
            System.out.println("starting tracking item...");
            UUID originalID = UUID.randomUUID();
            item.setTrackable(originalID, UltimateTracker.getActualDate());
            System.out.println("item set tracked cote tracked item, now generating trackeditem");
            TrackedItem trackedItem = new TrackedItem(item);
            System.out.println("prout");

            if(UltimateTracker.database) {
                Database.add(trackedItem);
            }

            ItemStartTrackingEvent trackEvent = new ItemStartTrackingEvent(trackedItem);
            Bukkit.getPluginManager().callEvent(trackEvent);

            return trackedItem;
        }
    }


    public boolean isDatedBeforeThan(String date){
        String format = "yyyy.MM.dd.HH.mm";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        LocalDateTime itemDate = LocalDateTime.parse(this.getLastUpdate(), formatter);
        LocalDateTime otherDate = LocalDateTime.parse(date, formatter);
        return itemDate.isBefore(otherDate);
    }



    public String getBase64(){
        return itemStackArrayToBase64(new ItemStack[]{this.getItem()});
    }

    public String getLastUpdate(){
        return this.getItemMutable().getLastUpdate();
    }

    public void update(){
        String newDate = UltimateTracker.getActualDate();
        ItemUpdateDateEvent updateEvent = new ItemUpdateDateEvent(this, newDate);
        Bukkit.getPluginManager().callEvent(updateEvent);
        this.getItemMutable().updateDate(newDate);
        Database.update(this.originalID, newDate);

    }



    public static String itemStackArrayToBase64(ItemStack[] items) throws IllegalStateException {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            // Write the size of the inventory
            dataOutput.writeInt(items.length);

            // Save every element in the list
            for (int i = 0; i < items.length; i++) {
                dataOutput.writeObject(items[i]);
            }

            // Serialize that array
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save item stacks.", e);
        }
    }


    /**  private void antiDupeCheck(String s) {
        FileConfiguration config  = UltimateTracker.getInstance().getConfig();
        boolean enabled = Boolean.parseBoolean(config.getString("database.enabled"));
        if(s.equals("destruction") && enabled){
            Database db = new Database();
            db.addOldItem(this);

        }
        // CHECK IF ID BELONGS TO "OLD DESTRUCTED IDs" AND IF YES, LOG DUPE MESSAGE
    }



    public void log(String s) {
        // SYSTEM DE LOG PAS ENCORE FINI,
        // A terme il enregistrera le log dans un fichier.
        Player p = this.getPlayer();
        String pName = p.getName();
        String pId = p.getUniqueId().toString();
        String player = pName + " (" + pId + ")";

        UUID id = this.getUUID();
        String type = this.getType().toString();

        Location loc = p.getLocation();
        int x = (int) loc.getX();
        int y = (int) loc.getY();
        int z = (int) loc.getZ();
        String world = loc.getWorld().getName();
        String location = "x: " + x + " y:" + y + " z: " + z + " in: " + world;

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        String date = "[" + now + "]";

        String log = date + " " + player + " " + s + " " + type + " at " + location + ". Item-id: " + id;
        System.out.println(log);
    }**/



}
