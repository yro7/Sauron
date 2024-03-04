package fr.yronusa.ultimatetracker.Config;

import com.jojodmo.safeNBT.api.SafeNBT;
import fr.yronusa.ultimatetracker.ItemMutable;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class TrackingRule {


    public static List<TrackingRule> getTrackingRulesFromConfig(){
        return null;
    }


    static class Rule {
        List<String> contains;
        List<Material> materials;
        List<ItemFlag> flags;
        List<String> nbt;
        HashMap<String,String> nbtEquals;
    }

    List<Rule> rules;

    public Predicate<ItemMutable> getPredicate(){
        return null;
    }

    public TrackingRule(List<Rule> rules) {
        this.rules = rules;
    }

    public static Predicate<ItemStack> createRulePredicate(Rule rule) {

        return new Predicate<ItemStack>() {
            @Override
            public boolean test(ItemStack itemStack) {
                itemStack.addItemFlags();
                return true;
            }
        };
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

        return true;
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
            if(!itemNbt.hasKey(s) || )
        }

        return true;
    }





    public static Object getGoodKey(SafeNBT nbt, String key) {
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


}
