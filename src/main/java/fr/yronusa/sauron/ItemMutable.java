package fr.yronusa.sauron;

import fr.yronusa.sauron.Config.Config;
import fr.yronusa.sauron.Event.IllegalItemDetectedEvent;
import fr.yronusa.sauron.Event.ItemUpdateDateEvent;
import fr.yronusa.sauron.SafeNBTAPI.SafeNBT;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayOutputStream;
import java.sql.Timestamp;
import java.util.UUID;

/**
 *
 * A mutable (item's metadata) version of ItemStack.
 * Used to deal with item's NBT more easily.
 * "\n"\
 * More generally, is used for delegation of {@link TrackedItem}. Is also used to deal with illegal items (see verifIllegal method)
 */
public class ItemMutable {

    ItemStack item;
    Inventory inventory;
    int inventoryPlace;


    public ItemMutable(ItemStack item, Inventory inventory, int place) {
        this.item = item;
        this.inventory = inventory;
        this.inventoryPlace = place;

        // Avoid to check for illegal item if there aren't any.
        if(Config.enableIllegalItemsLookup) this.verifIllegal();
    }

    /**
     * Checks if an item is illegal and fires a {@link IllegalItemDetectedEvent} if so.
     * Is used every time an {@link ItemMutable} is generated so that the plugin basically checks every item for illegal ones.
     */
    public boolean verifIllegal() {
        ItemStack item = this.getItem();
        if(item == null || Config.illegalItemRules == null) return false;
        if(Config.itemStackIsIllegal(item)){
                IllegalItemDetectedEvent illegalItemDetectedEvent = new IllegalItemDetectedEvent(this);
                Bukkit.getPluginManager().callEvent(illegalItemDetectedEvent);
        }

        return true;
    }
    public Timestamp getLastUpdate(){
        SafeNBT nbt = SafeNBT.get(this.getItem());
        if(nbt.hasKey("sauron_date")){
            return Timestamp.valueOf(nbt.getString("sauron_date"));
        }
        else{
            Log.console("Error: the item is not tracked.", Log.Level.HIGH);

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
    public boolean shouldBeTrack() {
        if(!Config.enableItemsTracking) return false;
        if(this.getItem() == null) return false;
        if(!Config.trackStackedItems && this.getItem().getAmount() != 1) return false;

        return Config.itemStackIsToTrack(this.getItem());
    }
    public ItemStack getItem() {
        return this.item;
    }

    public Inventory getInventory() {
        return this.inventory;
    }

    public int getInventoryPlace() {
        return this.inventoryPlace;
    }


    public UUID getID(){
        SafeNBT nbt = SafeNBT.get(this.item);
        return UUID.fromString(nbt.getString("sauron_id"));
    }

    /**
     * Tries to get a sauron_id on the specified item.
     * @return True if the item is tracked, false otherwise.
     */
    public boolean hasTrackingID(){

        SafeNBT nbt = SafeNBT.get(this.item);
        return nbt.hasKey("sauron_id");
    }

    public static boolean hasTrackingID(ItemStack item){
        if(item == null) return false;
        SafeNBT nbt = SafeNBT.get(item);
        return nbt.hasKey("sauron_id");
    }


    /**
     * Adds all necessary data on a newly tracked item.
     * @param id The standard java {@link UUID} that will be registered onto the item's nbt.
     * @param newDate A sql {@link Timestamp} of last item's update.
     */

    public void setTrackable(UUID id, Timestamp newDate){

        if(this.hasTrackingID()){
            return;
        }

        else{
            ItemStack i = this.getItem();
            SafeNBT nbt = SafeNBT.get(i);
            nbt.setString("sauron_id", id.toString());
            nbt.setString("sauron_date", newDate.toString());
            this.update(nbt.apply(i).getItemMeta());
        }



    }


    /**
     * Allows to update an ItemStack ItemMeta (immutable by default).
     * @param newItem the modified version of the item
     */
    public void updateSave(ItemStack newItem){
        Inventory inv = this.getInventory();
        this.item = newItem;
        inv.setItem(this.getInventoryPlace(), newItem);

    }

    public void update(ItemMeta newItemMeta){
        this.item.setItemMeta(newItemMeta);

    }

    public Timestamp updateDate(Timestamp newDate) throws IllegalStateException{
        ItemStack i = this.getItem();
        SafeNBT nbt = SafeNBT.get(i);
        nbt.setString("sauron_date", newDate.toString());
        this.update(nbt.apply(i).getItemMeta());

        ItemUpdateDateEvent updateDateEvent = new ItemUpdateDateEvent(this);
        Bukkit.getPluginManager().callEvent(updateDateEvent);
        Log.console("Successfully updated the date on item of UUID " + this.getID() + "", Log.Level.DEBUG);

        return newDate;
    }

    public void delete(){
        System.out.print(this);
        this.item = new ItemStack(Material.AIR);
        this.getInventory().setItem(this.inventoryPlace, this.item);
    }

    public void changeUUID(UUID uuid){
        ItemStack i = this.getItem();
        SafeNBT nbt = SafeNBT.get(i);
        nbt.setString("sauron_id", uuid.toString());
        this.update(nbt.apply(i).getItemMeta());
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

    public String getBase64(){
        String base = itemStackArrayToBase64(new ItemStack[]{this.getItem()});
        return base.replaceAll("\\n", "");
    }

    public Player getPlayer(){
        Location inventoryLocation = this.getInventory().getLocation();
        Inventory inventory = this.getInventory();;
        if(inventory.getHolder() instanceof Player p){
            return p;
        }

        for(HumanEntity humanEntity : inventory.getViewers()){
            if(humanEntity instanceof Player p) return p;
        }

        return null;
    }

    public String toString(){
        return this.item.getType() + " " + this.getID()
                + " place " + this.inventoryPlace + " inventory " + this.inventory;
    }

}
