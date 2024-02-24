package fr.yronusa.ultimatetracker;

import com.jojodmo.safeNBT.api.SafeNBT;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public class TrackedItem {


    public List<Inventory> lastInventories;

    public ItemMutable item;
    public UUID originalID;
    public String lastUpdate; // Last update of the item at format YYYY.MM.DD.hh.mm


    public TrackedItem(ItemMutable item, Inventory inv, List<Inventory> lastInv, int inventoryPlace, UUID originalID, String date) {
        this.item = item;
        this.lastInventories = lastInv;
        this.originalID = originalID;
        this.lastUpdate = date;
    }


    public void changeInventory(Inventory inv){
        this.lastInventory = this.inventory;
        this.inventory = inv;
    }



    public ItemStack getItem() {
        return this.item.item;
    }

    public ItemMutable getItemMutable() {
        return this.item;
    }



    public Inventory getInventory() {
        return this.inventory;
    }

    public int getPlace() {
        return this.inventoryPlace;
    }

    public void setOriginalID(String id) {
        Inventory inv = this.getInventory();
        ItemStack i = this.getItem();

        SafeNBT nbt = SafeNBT.get(i);
        nbt.setString("ut_originalID", id);

        ItemStack i2 = nbt.apply(i);
        this.item = i2;
        inv.setItem(this.inventoryPlace, i2);
    }

    public void setTrackingID(String id) {
        Inventory inv = this.getInventory();
        ItemStack i = this.getItem();

        SafeNBT nbt = SafeNBT.get(i);
        nbt.setString("ut_trackingID", id);

        ItemStack i2 = nbt.apply(i);
        this.item = i2;
        inv.setItem(this.inventoryPlace, i2);
    }


    public boolean equals(TrackedItem i){
        return (i.getOriginalUUID() == this.getOriginalUUID()
                && i.last().equals(this.getTrackingUUID()));
    }

    public boolean instanceOf(TrackedItem i) {
        UUID trackingA = this.getTrackingUUID();
        UUID trackingB = i.getTrackingUUID();
        return (i.getOriginalUUID() == this.getOriginalUUID()
                && Database.trackingInstance(trackingA, trackingB));
    }

    public static void addID(ItemStack i){
        if(is)

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
