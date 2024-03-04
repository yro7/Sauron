package fr.yronusa.ultimatetracker;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;

public class InventoryLocation {

    Player player;

    InventoryType inventoryType;
    Location location;

    public InventoryLocation(Player p, Location location, InventoryType inventoryType) {
        this.player = p;
        this.location = location;
        this.inventoryType = inventoryType;
    }

    public InventoryLocation(Player p) {
        this.location = p.getLocation();
        this.player = p;
        this.inventoryType = InventoryType.PLAYER;
    }

    public InventoryLocation(String fromString){
        // Format :
        // PlayerName, InventoryType, X, Y, Z, World

        String[] compound = fromString.split(",");
        String player = compound[0];
        String inventoryType = compound[1];
        String[] location = compound[2].split(",");

        this.player = Bukkit.getPlayer(player);

        switch(inventoryType){
            case "P":
                this.inventoryType = InventoryType.PLAYER;
                break;
            case "S":
                this.inventoryType = InventoryType.SHULKER_BOX;
                break;
            default:
                this.inventoryType = InventoryType.CHEST;

        }

        this.location = new Location(Bukkit.getWorld(location[3]),
                Integer.parseInt(location[0]), Integer.parseInt(location[1]), Integer.parseInt(location[2]));

    }

    public String formatForDatabase(){
        // Format :
        // PlayerName, InventoryType, X, Y, Z, World

        String player = this.player.getName();
        String inventoryType;

        switch(this.inventoryType){
            case PLAYER:
                inventoryType = "P";
            case SHULKER_BOX:
                inventoryType = "S";
            default:
                inventoryType = "C";

        }

        String location = this.location.getBlockX() + "," + this.location.getBlockY() +
                    "," + this.location.getBlockZ() + "," + this.location.getWorld().getName();
        return player + "," + inventoryType + "," + location;

    }


}
