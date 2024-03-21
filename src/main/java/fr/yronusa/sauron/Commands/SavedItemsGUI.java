package fr.yronusa.sauron.Commands;

import mc.obliviate.inventory.Gui;
import org.bukkit.entity.Player;

public class SavedItemsGUI extends Gui {

    final int page;

    public SavedItemsGUI(Player player, int page) {
        super(player, "saveditems-gui", "§5§lObjets sauvegardés - " + page, 6);
        //player, id, title, row
        this.page = page;
    }
/**
    @Override
    public void onOpen(InventoryOpenEvent event) {
        int a = this.page*45;
        int b = a + 45;
        List<ItemStack> items = null;

        try{
         //   items = Database.getSavedItems(a, b);
        }
        catch(Exception e){
            return;
        }

        CompletableFuture<List<TrackedItem>> getTrackedItems= CompletableFuture.supplyAsync(() -> Database.isDuplicated(this));
        isDupli.exceptionally(error -> {
            if(Sauron.database) Database.update(this, newDate);
            this.getItemMutable().updateDate(newDate);
            return false;
        });
        isDupli.thenAccept((res) -> {
            if(res){
                DupeDetectedEvent dupeDetectEvent = new DupeDetectedEvent(this, this.getPlayer());

                // Necessary because in the newest version of Spigot, Event can't be called from async thread.
                Bukkit.getScheduler().runTask(Sauron.getInstance(), () -> Bukkit.getPluginManager().callEvent(dupeDetectEvent));
            }

            else{
                Database.update(this, newDate);
                this.getItemMutable().updateDate(newDate);
            }
        });

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
**/
}
