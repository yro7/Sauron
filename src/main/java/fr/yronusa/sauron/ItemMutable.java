package fr.yronusa.sauron;

import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import fr.yronusa.sauron.Config.Config;
import fr.yronusa.sauron.Config.TrackingRule;
import fr.yronusa.sauron.Event.IllegalItemDetectedEvent;
import fr.yronusa.sauron.SafeNBTAPI.SafeNBT;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
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
        for(TrackingRule rule : Config.illegalItemRules){
            if(rule.test(item)){
                IllegalItemDetectedEvent illegalItemDetectedEvent = new IllegalItemDetectedEvent(this);
                Bukkit.getPluginManager().callEvent(illegalItemDetectedEvent);
            }
        }
        return true;
    }
    public Timestamp getLastUpdate(){
        SafeNBT nbt = SafeNBT.get(this.getItem());
        if(nbt.hasKey("sauron_date")){
            return Timestamp.valueOf(nbt.getString("sauron_date"));
        }
        else{
            System.out.println("[Sauron] Error: the item isn't tracked.");
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

    public boolean hasTrackingID(){

        ReadWriteNBT nbt2 = NBT.itemStackToNBT(this.getItem());
        ReadWriteNBT nbt3 = nbt2.getCompound("tag");
        if(nbt3 != null) return nbt3.hasTag("sauron_id");
        return false;
          /**
        if(TrackedItem.shouldBeTrack(this)){
            SafeNBT nbt = SafeNBT.get(this.item);
            return nbt.hasKey("sauron_id");
        }

        return false;
       // SafeNBT nbt = SafeNBT.get(this.item);
      //  return nbt.hasKey("sauron_id"); **/
    }

    public static boolean hasTrackingID(ItemStack i){
        SafeNBT nbt = SafeNBT.get(i);
        return nbt.hasKey("sauron_id");
    }

    public void setTrackable(UUID id, Timestamp newDate){

        if(this.hasTrackingID()){
            return;
        }

        else{
            ItemStack i = this.getItem();
            SafeNBT nbt = SafeNBT.get(i);
            nbt.setString("sauron_id", id.toString());
            nbt.setString("sauron_date", newDate.toString());
            this.update(nbt.apply(i));
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
        nbt.setString("sauron_date", newDate.toString());
        this.update(nbt.apply(i));
        return newDate;
    }

    public void delete(){
        this.item.setAmount(0);
    }

    public void changeUUID(UUID uuid){
        ItemStack i = this.getItem();
        SafeNBT nbt = SafeNBT.get(i);
        nbt.setString("sauron_id", uuid.toString());
        this.update(nbt.apply(i));
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
        Inventory inventory = this.getInventory();;
        if(inventory.getHolder() instanceof Player p){
            return p;
        }

        for(HumanEntity humanEntity : inventory.getViewers()){
            if(humanEntity instanceof Player p) return p;
        }

        return null;
    }

}
