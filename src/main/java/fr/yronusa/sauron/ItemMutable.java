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
      //  SafeNBT nbt = SafeNBT.get(this.getItem());
        ReadWriteNBT nbt2 = NBT.itemStackToNBT(this.getItem());
        if(this.hasTrackingID()){
            return Timestamp.valueOf(nbt2.getCompound("tag").getString("sauron_date"));
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
        //  SafeNBT nbt = SafeNBT.get(this.getItem());
        ReadWriteNBT nbt2 = NBT.itemStackToNBT(this.getItem());
        if(this.hasTrackingID()){
           return UUID.fromString(nbt2.getCompound("tag").getString("sauron_id"));
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

        ReadWriteNBT nbt2 = NBT.itemStackToNBT(this.item);
        if(nbt2.hasTag("tag") && nbt2.getCompound("tag").hasTag("sauron_id")) return true;
        return nbt2.hasTag("sauron_id");
    }

    public static boolean hasTrackingID(ItemStack item){
        if(item == null) return false;
        ReadWriteNBT nbt2 = NBT.itemStackToNBT(item);
        return nbt2.hasTag("sauron_id");
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
            System.out.println("new item trackable : " + nbt2);

            nbt2.setString("sauron_id", id.toString());
            nbt2.setString("sauron_date", newDate.toString());
            System.out.println("§C  SETTING TRACKABLE THE ITEM");
            System.out.println("new item trackable2 : " + nbt2);
            this.update(nbt2);
        }



    }

    public void update(ReadWriteNBT newItemNBT){
        System.out.println("§C  UPDATING THE ITEM WITH NBT  " + newItemNBT);
        ItemMutable item = this;
        NBT.modify(this.item, nbt -> {


            String newID;
            String newTimeStamp;
            System.out.println(" item nbt : " + nbt);
            if (item.hasTrackingID()) {
                newID = newItemNBT.getCompound("tag").getString("sauron_id");
                newTimeStamp = newItemNBT.getCompound("tag").getString("sauron_date");
            }
            else{
                newID = newItemNBT.getString("sauron_id");
                newTimeStamp = newItemNBT.getString("sauron_date");
            }

            nbt.setString("sauron_date",newTimeStamp);
            nbt.setString("sauron_id",newID);
            System.out.println(" item nbt after edit : " + nbt);
        });

        ReadWriteNBT test = NBT.itemStackToNBT(this.item);
        System.out.println(" item nbt after edit 2 : " + test);
    }

    public Timestamp updateDate(Timestamp newDate) throws IllegalStateException{
        ItemStack i = this.getItem();
     //   SafeNBT nbt = SafeNBT.get(i);
        ReadWriteNBT nbt2 = NBT.itemStackToNBT(i);

        if(this.hasTrackingID()){
            nbt2.getCompound("tag").setString("sauron_date",newDate.toString());
        }
        else{
            nbt2.setString("sauron_date", newDate.toString());
        }

        this.update(nbt2);

        ItemUpdateDateEvent updateDateEvent = new ItemUpdateDateEvent(this);
        Bukkit.getPluginManager().callEvent(updateDateEvent);
        Log.console("Successfully updated the date on item of UUID " + this.getID() + "", Log.Level.DEBUG);

        return newDate;
    }

    /**
     *
     * @return the number of similar items that have been cleared in the inventory.
     */
    public int delete(){

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
        ItemStack i = this.getItem();
        ReadWriteNBT nbt2 = NBT.itemStackToNBT(i);
        nbt2.setString("sauron_id", uuid.toString());
        this.update(nbt2);
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

}
