package fr.yronusa.ultimatetracker;

import fr.yronusa.ultimatetracker.Event.ItemUpdateDateEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Array;

public class Listener implements org.bukkit.event.Listener {

    @EventHandler
    public void onHandlingItem(PlayerItemHeldEvent e){
        ItemStack item2 = e.getPlayer().getInventory().getItem(e.getNewSlot());

        if(item2 == null) return;
        ItemMutable i = new ItemMutable(e.getPlayer(), e.getNewSlot());

        if(i.hasTrackingID()){
            TrackedItem trackedItem = new TrackedItem(i);
            trackedItem.update();
            return;
        }

        if(TrackedItem.shouldBeTrack(i)){
            ItemMutable item = new ItemMutable(e.getPlayer(), e.getNewSlot());
            TrackedItem.startTracking(item);
        }
    }

}
