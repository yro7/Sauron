package fr.yronusa.ultimatetracker;

import com.jojodmo.safeNBT.api.SafeNBT;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TrackedItem {
    public List<InventoryLocation> lastInventories;
    public ItemMutable item;
    public UUID originalID;
    public String lastUpdate; // Last update of the item at format YYYY.MM.DD.hh.mm




    public TrackedItem(ItemMutable item, List<InventoryLocation> lastInv, UUID originalID, String date) {
        this.item = item;
        this.lastInventories = lastInv;
        this.originalID = originalID;
        this.lastUpdate = date;
    }

    public static boolean shouldBeTrack(ItemMutable i) {
        if(i.getItem() == null) return false;
        if(i.getItem().getAmount() != 1) return false;


        System.out.println("SHOULD BE TRACKABLE ?");
        boolean res = (i.getItem().getMaxStackSize() == 1
                && i.getItem().hasItemMeta()
                && !i.hasTrackingID()
        );
        System.out.println(res);
        return res;


    }

    public boolean isDuplicated(){
        return this.isDatedBeforeThan(Database.getLastUpdate(this));
    }

    public void changeInventory(InventoryLocation inv){
        List<InventoryLocation> lastInventories = this.getLastInventories();
        lastInventories.add(inv);
        if(lastInventories.size() > UltimateTracker.getInstance().inventoryListLength){
            lastInventories.remove(0);
        }
    }

    public List<InventoryLocation> getLastInventories() {
        return lastInventories;
    }

    public UUID getOriginalID() {
        return originalID;
    }


    public ItemStack getItem() {
        return this.item.item;
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
            item.setTrackable(originalID);
            TrackedItem trackedItem = new TrackedItem(item,
                    new ArrayList<InventoryLocation>(),originalID, item.getLastUpdate());
            if(UltimateTracker.database) {
                Database.add(trackedItem);
            }
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
