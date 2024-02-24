package fr.yronusa.ultimatetracker;

import com.jojodmo.safeNBT.api.SafeNBT;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class ItemMutable {

    ItemStack item;

    Inventory inventory;
    int inventoryPlace;


    public ItemMutable(ItemStack item, Inventory inventory, int place) {
        this.item = item;
        this.inventory = inventory;
        this.inventoryPlace = place;
    }

    public ItemStack getItem() {
        return item;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public int getInventoryPlace() {
        return inventoryPlace;
    }

    public boolean hasTrackingID(){
        SafeNBT nbt = SafeNBT.get(this.item);
        return nbt.hasKey("ut_trackingID");
    }

    public String getlastUpdate(){
        SafeNBT nbt = SafeNBT.get(this.item);
        if(nbt.hasKey("ut_date")){
            return nbt.getString("ut_date");
        }
        else{
            System.out.println("[ULTIMATE TRACKER] Error: the item isn't tracked.");
        }

        return null;
    }

    public void update(ItemStack newItem){
        Inventory inv = this.getInventory();
        ItemStack i = this.getItem();
        this.item = newItem;
        inv.setItem(this.getInventoryPlace(), newItem);
    }

    public void updateDate(){
        String format = "yyyy.MM.dd.HH.mm";
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        String formattedDateTime = currentDateTime.format(formatter);
        ItemStack i = this.getItem();
        SafeNBT nbt = SafeNBT.get(i);
        nbt.setString("ut_date", formattedDateTime);
        this.update(nbt.apply(i));
    }

}
