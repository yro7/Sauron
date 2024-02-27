package fr.yronusa.ultimatetracker;

import com.jojodmo.safeNBT.api.SafeNBT;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.UUID;

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


    public boolean getID(){
        SafeNBT nbt = SafeNBT.get(this.item);
        return nbt.hasKey("ut_trackingID");
    }

    public boolean hasTrackingID(){
        SafeNBT nbt = SafeNBT.get(this.item);
        return nbt.hasKey("ut_trackingID");
    }

    public void setTrackable(UUID id){
        if(this.hasTrackingID()){
            return;
        }

        else{
            ItemStack i = this.getItem();
            SafeNBT nbt = SafeNBT.get(i);
            nbt.setString("ut_id", id.toString());
            this.updateDate();
            nbt.setString("ut_date", this.updateDate());
        }



    }


    public void update(ItemStack newItem){
        Inventory inv = this.getInventory();
        ItemStack i = this.getItem();
        this.item = newItem;
        inv.setItem(this.getInventoryPlace(), newItem);
    }

    public String updateDate(){
        String format = "yyyy.MM.dd.HH.mm";
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        String formattedDateTime = currentDateTime.format(formatter);
        ItemStack i = this.getItem();
        SafeNBT nbt = SafeNBT.get(i);
        nbt.setString("ut_date", formattedDateTime);
        this.update(nbt.apply(i));
        return formattedDateTime;
    }



}
