package fr.yronusa.ultimatetracker.Commands;

import fr.yronusa.ultimatetracker.Database;
import fr.yronusa.ultimatetracker.ItemNBT;
import fr.yronusa.ultimatetracker.ItemSaver;
import fr.yronusa.ultimatetracker.TrackedItem;
import org.bukkit.Material;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.sound.midi.Track;
import java.util.List;
import java.util.UUID;

public class Command implements CommandExecutor {


    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, org.bukkit.command.@NotNull Command command, @NotNull String s, @NotNull String[] strings) {
       if(!(commandSender instanceof Player)){
           commandSender.sendMessage("§cCette commande doit être utilisée en jeu!");
           return true;
       }

       if(strings.length == 0){
           commandSender.sendMessage("§2§l---|--- §a§lUltimateTracker §2§l---|---");
           commandSender.sendMessage("");
           commandSender.sendMessage("§aCommandes:");
           commandSender.sendMessage("");
           commandSender.sendMessage("§7* §a/ut: §7Affiche ce menu d'aide.");
           commandSender.sendMessage("");
           commandSender.sendMessage("§7* §a/ut list saved: §7Affiche la liste des objets enregistrés.");
           commandSender.sendMessage("§7* §a/ut list tracked: §7Affiche la liste des objets traqués.");
           commandSender.sendMessage("");
           commandSender.sendMessage("§7* §a/ut stop: §7Arrête ou remet en route l'update automatique des yci_id 'blank'. Utile pour du debug ou de l'édition d'items customs.");
           commandSender.sendMessage("");
           commandSender.sendMessage("§7* §a/ut (stop): §7Arrête ou remet en route l'update automatique des yci_id 'blank'. Utile pour du debug ou de l'édition d'items customs.");



           return true;
       }

       Player p = (Player) commandSender;

        switch(strings[0].toLowerCase()){
            case "save":
                save(p);
                break;
            case "delete":
                delete(p);
                break;
            case "find":
                find(p);
                break;
            case "list":
                list(p, strings);
                break;
            case "quarantines":
                quarantines(p);
                break;

        }

        return true;
    }

    private void save(Player p) {

        ItemStack i = p.getInventory().getItemInMainHand();
        if(i.getType() != Material.AIR){

            try{
                Database.saveItem(i);
                p.sendMessage("§7* §aObjet enregistré avec succès !");
            }

            catch(Exception e){
                e.printStackTrace();
                p.sendMessage("§7* §cERREUR: L'objet n'a pas correctement pu être sauvegardé.");
            }

        }

        else{
            p.sendMessage("§7* §cVous devez tenir un objet en main!");
        }

    }

    public void delete(Player p){

    }

    private void find(Player p) {
    }


    private void quarantines(Player p) {
    }

    private void list(Player p, String[] args){
        if(args.length < 2){
            p.sendMessage("§7* §cVeuillez spécifier ''saved'' ou ''tracked''.");
            return;
        }
        switch(args[1]){
            case "saved":
                new SavedItemsGUI(p, 0).open();
                break;
            case "tracked":
               // new TrackedItemsGUI(p, 0).open();
                break;
            default:
                p.sendMessage("§7* §cVeuillez spécifier ''saved'' ou ''tracked''.");
                break;
        }
    }

    public TrackedItem startTracking(ItemStack item, Inventory inv, int inventoryPlace) {

        ItemNBT itemNbt = new ItemNBT(item);
        if(itemNbt.hasTrackingID() ){

            return null;
        }

        UUID newID = null;

        return new TrackedItem(item, inv, inv,inventoryPlace, newID, newID);
    }

}
