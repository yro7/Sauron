package fr.yronusa.ultimatetracker.Config;

import com.jojodmo.safeNBT.api.SafeNBT;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class TrackingRule implements Predicate<ItemStack> {
    static ConfigurationSection configSection = Config.config.getConfigurationSection("rules");
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

    public static List<TrackingRule> getTrackingRulesFromConfig(){

        List<TrackingRule> res = new ArrayList<>();

        Set<String> rulesPath = configSection.getKeys(false);
        for(String s : rulesPath){
            res.add(new TrackingRule(s));
        }

        System.out.print("returning : " + res);
        return res;
    }

    public TrackingRule(String s) {

        System.out.print("generating new tracking rule from string " + s);

        this.materials = new ArrayList<>();
        this.flags = new ArrayList<>();
        this.nbt = new ArrayList<>();
        this.contains = new ArrayList<>();
        this.nbtEquals = new HashMap<>();

        List<String> materials;
        List<String> flags;

        System.out.print("devbug AAA");
        System.out.println(s +".contains");
        System.out.print(s);
        System.out.print("prout " + configSection.getStringList("ruleA.contains"));
        System.out.println(configSection.getStringList(s +".contains"));
        System.out.println(configSection.getStringList(s +".has_nbt"));
        System.out.println(configSection.getStringList(s +".materials"));

        // try{
        this.contains = configSection.getStringList(s +".contains");
        this.nbt= configSection.getStringList(s +".has_nbt");

        materials = configSection.getStringList(s +".materials");
        flags = configSection.getStringList(s +".flags");

        for(String flag : flags){
            this.flags.add(ItemFlag.valueOf(flag));
        }

        for(String mat : materials){
            this.materials.add(Material.valueOf(mat));
        }


        /**  } catch(Exception e){
         System.out.print("§4[UltimateTracker] §cError: you have an error in your rules config section.");
         e.printStackTrace();
         }
         **/



    }
    public static boolean containsString(ItemStack i, List<String> strings){
        if(strings.isEmpty()) return true;
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
            if(name != null && name.contains(s)) return true;
        }

        return false;
    }

    public static boolean isOfMaterial(ItemStack i, List<Material> materials){
        if(materials.isEmpty()) return true;
        return materials.contains(i.getType());
    }

    public static boolean hasFlag(ItemStack i, List<ItemFlag> flags){
        if(flags.isEmpty()) return true;
        for(ItemFlag flag : i.getItemMeta().getItemFlags()){
            if(flags.contains(flag)) return true;
        }
        return false;
    }

    public static boolean hasNbt(ItemStack i, List<String> nbt){
        if(nbt.isEmpty()) return true;
        SafeNBT itemNbt = SafeNBT.get(i);
        for(String s : nbt){
            if(itemNbt.hasKey(s)) return true;
        }
        return false;
    }

    public static boolean hasKeyValueNbt(ItemStack i, HashMap<String,String> nbt){
        if(nbt.isEmpty()) return true;
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
        System.out.println("isofmaterial: " + isOfMaterial(itemStack, materials));
        System.out.println("hasFlag: " + hasFlag(itemStack, flags));
        System.out.print("hasNbt : " + hasNbt(itemStack, this.nbt));
        System.out.print("containsString : " + containsString(itemStack, this.contains));
        return isOfMaterial(itemStack, materials)
                && hasFlag(itemStack, this.flags)
                && hasNbt(itemStack, this.nbt)
                && hasKeyValueNbt(itemStack, this.nbtEquals)
                && containsString(itemStack, this.contains);
    }

    public void print(){
        System.out.print("TRACKING RULE N° " + this);
        System.out.print("MUST CONTAINS ONE OF THOSE WORDS:");
        for(String s : this.contains){
            System.out.println(s);
        }
        System.out.print("MUST BE OF ONE OF THOSE MATERIALS:");
        for(Material m : this.materials){
            System.out.println(m);
        }
        System.out.print("MUST HAVE ONE OF THOSE FLAGS:");
        for(ItemFlag f : this.flags){
            System.out.println(f);
        }
        System.out.print("MUST HAVE ONE OF THE FOLLOWING NBT/KEY ASSOCIATION:");
        for(String nbt : this.nbtEquals.keySet()){
            System.out.println(nbt + "," + this.nbtEquals.get(nbt));
        }
        System.out.print("MUST HAVE ONE OF THOSE NBT:");
        for(String nbt : this.nbt){
            System.out.println(nbt);
        }

    }
}
