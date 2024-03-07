package fr.yronusa.ultimatetracker;

import com.jojodmo.safeNBT.api.SafeNBT;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.sql.Timestamp;
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
    public Timestamp getLastUpdate(){
        SafeNBT nbt = SafeNBT.get(this.getItem());
        if(nbt.hasKey("ut_date")){
            return Timestamp.valueOf(nbt.getString("ut_date"));
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

    public ItemMutable(Player p, int slot) {
        this.item = p.getInventory().getItem(slot);
        this.inventory = p.getInventory();
        this.inventoryPlace = slot;
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


    public UUID getID(){
        SafeNBT nbt = SafeNBT.get(this.item);
        return UUID.fromString(nbt.getString("ut_id"));
    }

    public boolean hasTrackingID(){
        SafeNBT nbt = SafeNBT.get(this.item);
        return nbt.hasKey("ut_id");
    }

    public void setTrackable(UUID id, Timestamp newDate){

        if(this.hasTrackingID()){
            return;
        }

        else{
            ItemStack i = this.getItem();
            SafeNBT nbt = SafeNBT.get(i);
            nbt.setString("ut_id", id.toString());
            nbt.setString("ut_date", newDate.toString());
            this.update(nbt.apply(i));
            System.out.println("item set trackable haha");
        }



    }


    public void update(ItemStack newItem){
        Inventory inv = this.getInventory();
        ItemStack i = this.getItem();
        this.item = newItem;
        inv.setItem(this.getInventoryPlace(), newItem);
    }

    public Timestamp updateDate(Timestamp newDate){
        ItemStack i = this.getItem();
        SafeNBT nbt = SafeNBT.get(i);
        nbt.setString("ut_date", newDate.toString());
        this.update(nbt.apply(i));
        return newDate;
    }

    public void delete(){
        this.item.setAmount(0);
    }



}
