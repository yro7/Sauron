package fr.yronusa.sauron.Commands;

import fr.yronusa.sauron.Config.Config;
import fr.yronusa.sauron.Database.Database;
import fr.yronusa.sauron.Database.Initializer;
import fr.yronusa.sauron.ItemMutable;
import fr.yronusa.sauron.Sauron;
import fr.yronusa.sauron.TrackedItem;
import fr.yronusa.sauron.Tracker;
import org.bukkit.Material;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;

public class Command implements CommandExecutor {

    public static HashMap<Player, BukkitTask> doNotTrackPlayers = new HashMap<>();

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

        if(!p.hasPermission("sauron.use."+strings[0].toLowerCase())){
            p.sendMessage(Config.insufficientPermission);
        }

        switch(strings[0].toLowerCase()){
            case "list":
                list(p);
                break;
            case "stop":
                stop(p);
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

    public void help(Player p) {
        p.sendMessage("§4§l§m-|--- §c§l SAURON §4§l§m----|-");
        p.sendMessage("§eCreated by yronusa");
        p.sendMessage( "§eVersion : " + Sauron.getVersion());
        for(String msg : Config.helpCommand){
            p.sendMessage(msg);
        }
    }

    public void track(Player p) {
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

    public void refund(Player p){
        ItemMutable item = new ItemMutable(p);
        if(!item.hasTrackingID()){
            p.sendMessage(Config.notTracked);
        }

        else{
            TrackedItem trackedItem = new TrackedItem(item);
            UUID oldID = trackedItem.getOriginalID();
            Database.blacklist(trackedItem, oldID);
            trackedItem.resetUUID();
            Database.add(trackedItem);

        }
    }


    public void reload(Player p){
        Sauron.getInstance().reloadConfig();
        Config.load();
        Sauron.database = Initializer.initializeDatabase();
        p.sendMessage(Config.reloadSuccessful);

        // Reload the automatic tracker
        if(Tracker.currentPlayersCheck != null) Tracker.currentPlayersCheck.cancel();
        Tracker.initialize();
    }

    public void list(Player p){

    }

    public void stop(Player p){
        if(Command.doNotTrackPlayers.containsKey(p)){
            Command.doNotTrackPlayers.get(p).cancel();
            Command.doNotTrackPlayers.remove(p);
            p.sendMessage("§7* §cAutomatic tracking of your items has been re-enabled.");
            return;
        }
        p.sendMessage("§7* §aAutomatic tracking of your items has been stopped for 5 minutes.");
        BukkitTask warning = new BukkitRunnable() {
            @Override
            public void run() {
                p.sendMessage("§c[Sauron] Warning: Automatic tracking of your item will be re-enabled in 30 seconds.");
            }
        }.runTaskLater(Sauron.getInstance(), (60*5 - 30)*20);

        new BukkitRunnable() {
            @Override
            public void run() {
                Command.doNotTrackPlayers.remove(p);
            }
        }.runTaskLater(Sauron.getInstance(), 60*5);

        Command.doNotTrackPlayers.put(p,warning);
    }


}

