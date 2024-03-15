package fr.yronusa.ultimatetracker.Commands;

import fr.yronusa.ultimatetracker.Config.Config;
import fr.yronusa.ultimatetracker.Database.Database;
import fr.yronusa.ultimatetracker.ItemMutable;
import fr.yronusa.ultimatetracker.TrackedItem;
import fr.yronusa.ultimatetracker.UltimateTracker;
import org.bukkit.Material;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class Command implements CommandExecutor {


    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, org.bukkit.command.@NotNull Command command, @NotNull String s, @NotNull String[] strings) {
       if(!(commandSender instanceof Player p)){
           commandSender.sendMessage("§cThis is a player only command, sorry.");
           return true;
       }

        if(strings.length == 0){
            help(p);
            return true;
       }

        if(!p.hasPermission("ut.use."+strings[0].toLowerCase())){
            p.sendMessage(Config.insufficientPermission);
        }

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
            case "track":
                track(p);
                break;
            case "reload":
                reload(p);
                break;
            case "refund":
                refund(p);
                break;
            default:
                help(p);
                break;

        }

        return true;
    }

    private void help(Player p) {
        p.sendMessage("§2§l---|--- §a§lUltimateTracker §2§l---|---");
        p.sendMessage("§aCreated by Yronusa for Vikicraft and Community");
        p.sendMessage( "§aVersion : " + UltimateTracker.getVersion());
        for(String msg : Config.helpCommand){

            p.sendMessage(msg);
        }
    }

    private void track(Player p) {

        ItemStack i = p.getInventory().getItemInMainHand();
        if(i.getType() != Material.AIR){

            try{
                ItemMutable item = new ItemMutable(p);

                if(item.hasTrackingID()){
                    p.sendMessage(Config.trackAlreadyTracked);
                    return;
                }

                if(item.getItem().getAmount() > 1){
                    p.sendMessage(Config.trackStacked);
                    return;
                }

                TrackedItem.startTracking(item);
                p.sendMessage(Config.trackSuccess);
            }

            catch(Exception e){
                e.printStackTrace();
                p.sendMessage(Config.trackFailed);
            }

        }

        else{
            p.sendMessage(Config.trackEmpty);
        }
    }

    private void save(Player p) {

        ItemStack i = p.getInventory().getItemInMainHand();
        if(i.getType() != Material.AIR){

            try{
                //Database.saveItem(i);
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

    private void refund(Player p){
        ItemMutable item = new ItemMutable(p);
        if(!item.hasTrackingID()){
            p.sendMessage(Config.notTracked);
        }

        else{
            TrackedItem trackedItem = new TrackedItem(item);
            UUID oldID = trackedItem.getOriginalID();
            trackedItem.resetUUID();
            Database.blacklist(trackedItem, oldID);
        }
    }


    public void reload(Player p){
        UltimateTracker.getInstance().reloadConfig();
        p.sendMessage(Config.reloadSuccessful);
    }

    private void list(Player p, String[] args){
        if(args.length < 2){
            p.sendMessage("§7* §cVeuillez spécifier ''saved'' ou ''tracked''.");
            return;
        }
        switch(args[1]){
            case "saved":
              //  new SavedItemsGUI(p, 0).open();
                break;
            case "tracked":
               // new TrackedItemsGUI(p, 0).open();
                break;
            default:
                p.sendMessage("§7* §cVeuillez spécifier ''saved'' ou ''tracked''.");
                break;
        }
    }

}
