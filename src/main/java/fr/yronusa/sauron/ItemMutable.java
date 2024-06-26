package fr.yronusa.sauron;

import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import fr.yronusa.sauron.Config.Config;
import fr.yronusa.sauron.Event.IllegalItemDetectedEvent;
import fr.yronusa.sauron.Event.ItemUpdateDateEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayOutputStream;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 * A mutable (item's metadata) version of ItemStack.
 * Used to deal with item's NBT more easily.
 * "\n"\
 * More generally, is used for delegation of {@link TrackedItem}. Is also used to deal with illegal items (see verifIllegal method)
 */
public class ItemMutable {

    public ItemStack item;
    public Inventory inventory;


    public ItemMutable(ItemStack item, Inventory inventory) {
        this.item = item;
        this.inventory = inventory;
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
        if(this.hasTrackingID()){
            return Timestamp.valueOf(this.getNBT().getString("sauron_date"));
        }
        else{
            Log.console("Error: the item is not tracked.", Log.Level.HIGH);
        }
        return null;
    }

    public ItemMutable(Player p) {
        this.item = p.getInventory().getItemInMainHand();
        this.inventory = p.getInventory();
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



    public UUID getID(){
        if(this.hasTrackingID()){
           return UUID.fromString(this.getNBT().getString("sauron_id"));
        }
        else{
            Log.console("Error: the item is not tracked.", Log.Level.HIGH);
        }

        return null;
    }

    /**
     * Tries to get a sauron_id on the specified item.
     * @return True if the item is tracked, false otherwise.
     */
    public boolean hasTrackingID(){
        ReadWriteNBT nbt = this.getNBT();
        if(nbt == null) return false;
        return nbt.hasTag("sauron_id");
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
            ReadWriteNBT nbt2 = NBT.itemStackToNBT(i);
            nbt2.setString("sauron_id", id.toString());
            nbt2.setString("sauron_date", newDate.toString());
            this.update(nbt2);
        }



    }

    public void update(ReadWriteNBT newItemNBT){
        if(hasTrackingID()){
            newItemNBT = getNBT(newItemNBT);
        }
        ReadWriteNBT finalNewItemNBT = newItemNBT;
        NBT.modify(this.item, nbt -> {

            String newID;
            String newTimeStamp;
            newID = finalNewItemNBT.getString("sauron_id");
            newTimeStamp = finalNewItemNBT.getString("sauron_date");

            nbt.setString("sauron_date",newTimeStamp);
            nbt.setString("sauron_id",newID);
        });

    }

    public Timestamp updateDate(Timestamp newDate) throws IllegalStateException{
        ReadWriteNBT nbt = this.getNBT();
        nbt.setString("sauron_date", newDate.toString());
        ItemUpdateDateEvent updateDateEvent = new ItemUpdateDateEvent(this);
        this.update(nbt);
        Bukkit.getPluginManager().callEvent(updateDateEvent);
        Log.console("Successfully updated the date on item of UUID " + this.getID() + "", Log.Level.DEBUG);
        return newDate;
    }

    /**
     *
     * @return the number of similar items that have been cleared in the inventory.
     */
    public int delete() {
        System.out.print(this);
        Inventory itemInventory = this.getInventory();

        int counter = (int) Arrays.stream(itemInventory.getStorageContents())
                .filter(i -> i != null && i.isSimilar(this.item))
                .count();
        itemInventory.remove(this.item);

        this.item = new ItemStack(Material.AIR);
        this.item.setType(Material.AIR);
        this.item.setAmount(0);
        return counter;
    }

    public void changeUUID(UUID uuid){
        ReadWriteNBT nbt = this.getNBT();
        nbt.setString("sauron_id", uuid.toString());
        this.update(nbt);

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

    /**
     * Try to get an eventual player that triggered the process which led to the creation of the ItemMutable.
     * In some cases, that player won't exist, for example if the plugin scanned a container that no player was looking onto.
     * a {@link ItemMutable} object doesn't to have a player to works as expected, but it is useful for logging purposes.
     *
     * @return the {@link Player} associated with the Tracked Item if found, null otherwise.
     */
    public Player getPlayer(){
        Inventory inventory = this.getInventory();;

        // If the inventory is owned by a player, return it
        if(inventory.getHolder() instanceof Player p){
            return p;
        }

        // Else, try to see which player could be looking into the container
        for(HumanEntity humanEntity : inventory.getViewers()){
            if(humanEntity instanceof Player p) return p;
        }

        // Else, try to find the nearest player in a 32 blocks radius.
        Location inventoryLocation = inventory.getLocation();
        Optional<Player> optionalPlayer = Arrays.stream(inventoryLocation.getChunk().getEntities())
                .filter(e -> (e instanceof Player))
                .map(e -> (Player) e)
                .min(Comparator.comparingDouble(o -> inventoryLocation.distance(o.getLocation())));
        if(optionalPlayer.isPresent()) return optionalPlayer.get();;

        // Otherwise, return null.
        return null;
    }

    public String toString(){
        return this.item.getType() + " " + this.getID()
                + " in inventory " + this.inventory;
    }

    /**
     * Retrieve a simpler version of the item's NBT. Depending on version, the NBT has a different form.
     * @return a ReadWriteNBT that contains sauron_id & sauron_date available with a simple {@link ReadWriteNBT#getString(String)}.
     */
    public ReadWriteNBT getNBT(){
        if(this.item == null) return null;
        return getNBT(NBT.itemStackToNBT(this.item));
    }

    public static ReadWriteNBT getNBT(ReadWriteNBT itemNBT){
        AtomicReference<ReadWriteNBT> res = new AtomicReference<>();
        if(itemNBT.hasTag("sauron_id")) return itemNBT;

        // In 1.12

        Optional.ofNullable(itemNBT.getCompound("tag"))
                .ifPresent(res::set);

        // In 1.20+
        Optional.ofNullable(itemNBT.getCompound("components"))
                .ifPresent(nbt -> {
                    Optional.ofNullable(nbt.getCompound("minecraft:custom_data"))
                            .ifPresent(res::set);
                });

        // In 1.20+ on Paper
        Optional.ofNullable(itemNBT.getCompound("components"))
                .ifPresent(nbt -> {
                    Optional.ofNullable(nbt.getCompound("minecraft:custom_name"))
                            .ifPresent(res::set);
                });

        return res.get();
    }

}
