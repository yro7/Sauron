package fr.yronusa.ultimatetracker.Config;

import com.jojodmo.safeNBT.api.SafeNBT;
import fr.yronusa.ultimatetracker.ItemMutable;
import fr.yronusa.ultimatetracker.UltimateTracker;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;

public class TrackingRule implements Predicate<ItemStack> {


    public static List<TrackingRule> getTrackingRulesFromConfig(){


        return null;
    }

    List<String> contains;
    List<Material> materials;
    List<ItemFlag> flags;
    List<String> nbt;
    HashMap<String,String> nbtEquals;

    public TrackingRule(List<String> contains, List<Material> materials,
                        List<ItemFlag> flags, List<String> nbt,
                        HashMap<String, String> nbtEquals) {
        this.contains = contains;
        this.materials = materials;
        this.flags = flags;
        this.nbt = nbt;
        this.nbtEquals = nbtEquals;
    }

    public static boolean containsString(ItemStack i, List<String> strings){
        List<String> lore = i.getItemMeta().getLore();
        String name = i.getItemMeta().getDisplayName();
        for(String s : strings){

            // Check if the lore contains the string
            if(lore != null){
                for(String loreLine : lore){
                    if(loreLine.contains(s)){
                        return true;
                    }
            }

            }
            // Check if the name contains the string
            if(name.contains(s)) return true;
        }

        return false;
    }

    public static boolean isOfMaterial(ItemStack i, List<Material> materials){
        return materials.contains(i.getType());
    }

    public static boolean hasFlag(ItemStack i, List<ItemFlag> flags){
        for(ItemFlag flag : i.getItemFlags()){
            if(flags.contains(flag)) return true;
        }
        return false;
    }

    public static boolean hasNbt(ItemStack i, List<String> nbt){
        SafeNBT itemNbt = SafeNBT.get(i);
        for(String s : nbt){
            if(itemNbt.hasKey(s)) return true;
        }
        return false;
    }

    public static boolean hasKeyValueNbt(ItemStack i, HashMap<String,String> nbt){
        SafeNBT itemNbt = SafeNBT.get(i);
        for(String s : nbt.keySet()){
            if(!itemNbt.hasKey(s) || getKey(itemNbt, s).toString().equals(nbt.get(s))) return false;
        }

        return true;
    }


    public static Object getKey(SafeNBT nbt, String key) {
        Object res;
        try {
            res = nbt.getString(key);
        } catch (Exception e1) {
            try {
                res = nbt.getInt(key);
            } catch (Exception e2) {
                try {
                    res = nbt.getDouble(key);
                } catch (Exception e3) {
                    try {
                        res = nbt.getFloat(key);
                    } catch (Exception e4) {
                        try {
                            res = nbt.getBoolean(key);
                        } catch (Exception e5) {
                            throw new RuntimeException("Failed to retrieve value for key: " + key, e5);
                        }
                    }
                }
            }
        }

        return res;
    }


    @Override
    public boolean test(ItemStack itemStack) {
        return isOfMaterial(itemStack, materials)
                && hasFlag(itemStack, this.flags)
                && hasNbt(itemStack, this.nbt)
                && hasKeyValueNbt(itemStack, this.nbtEquals)
                && containsString(itemStack, this.contains);
    }
}
