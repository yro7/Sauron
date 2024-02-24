package fr.yronusa.ultimatetracker.Commands;

import fr.yronusa.ultimatetracker.Database;
import mc.obliviate.inventory.Gui;
import mc.obliviate.inventory.Icon;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Iterator;
import java.util.List;

public class SavedItemsGUI extends Gui {

    final int page;

    public SavedItemsGUI(Player player, int page) {
        super(player, "saveditems-gui", "§5§lObjets sauvegardés - " + page, 6);
        //player, id, title, row
        this.page = page;
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {

        int a = this.page*45;
        int b = a + 45;


        // BUTTONS: page precedente, page suivante
        List<ItemStack> items;

        try{
            items = Database.getSavedItems(a, b);
        }
        catch(Exception e){
            System.out.println("prout error");
            return;
        }

        System.out.println(items);

        Iterator<ItemStack> iterator = items.iterator();

        while(iterator.hasNext()){
            ItemStack item = items.iterator().next();
            Icon icon = new Icon(item);
            icon.onClick(e -> {
                this.player.getInventory().addItem(item);
            });

            addItem(icon);

        }


        Icon nextPage = new Icon(Material.ARROW).setName("§a§lClique pour aller à la page suivante.");
        Icon previousPage = new Icon(Material.ARROW).setName("§a§lClique pour aller à la page précédente.");
        nextPage.onClick(e -> {
            this.setClosed(true);
            new SavedItemsGUI(this.player, this.page+1).open();
        });

        previousPage.onClick(e -> {
            this.setClosed(true);
            new SavedItemsGUI(this.player, this.page-1).open();
        });

        addItem(nextPage, 52);
        if(this.page != 0) addItem(previousPage, 46);


    }

}
