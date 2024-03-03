package fr.yronusa.ultimatetracker;

import com.jojodmo.safeNBT.api.SafeNBT;
import org.bukkit.Material;
import org.bukkit.entity.Player;
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
    public String getLastUpdate(){
        SafeNBT nbt = SafeNBT.get(this.getItem());
        if(nbt.hasKey("ut_date")){
            return nbt.getString("ut_date");
        }
        else{
            System.out.println("[ULTIMATE TRACKER] Error: the item isn't tracked.");
        }

        return null;
    }
    public ItemMutable(Player p) {
        this.item = p.getInventory().getItemInMainHand();
        this.inventory = p.getInventory();
        this.inventoryPlace = p.getInventory().getHeldItemSlot();
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
        return nbt.hasKey("ut_id");
    }

    public boolean hasTrackingID(){
        SafeNBT nbt = SafeNBT.get(this.item);
        return nbt.hasKey("ut_id");
    }

    public void setTrackable(UUID id){
        if(this.hasTrackingID()){
            return;
        }

        else{
            ItemStack i = this.getItem();
            SafeNBT nbt = SafeNBT.get(i);
            nbt.setString("ut_id", id.toString());
            nbt.setString("ut_date", this.updateDate());
            this.update(nbt.apply(i));
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
