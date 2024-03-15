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
    static ConfigurationSection configSection;
    List<String> contains;
    List<Material> materials;
    List<ItemFlag> flags;
    List<String> nbt;
    HashMap<String,String> nbtEquals;
    Boolean isUnbreakable;

    public TrackingRule(List<String> contains, List<Material> materials,
                        List<ItemFlag> flags, List<String> nbt,
                        HashMap<String, String> nbtEquals, Boolean isUnbreakable) {
        this.contains = contains;
        this.materials = materials;
        this.flags = flags;
        this.nbt = nbt;
        this.nbtEquals = nbtEquals;
        this.isUnbreakable = isUnbreakable;
    }

    public static List<TrackingRule> getTrackingRulesFromConfig(){
        configSection = Config.config.getConfigurationSection("rules");
        List<TrackingRule> res = new ArrayList<>();
        Set<String> rulesPath = configSection.getKeys(false);
        System.out.println(rulesPath);
        for(String s : rulesPath){
            res.add(new TrackingRule(s));
        }
        System.out.println("tracking rules : " + res);
        return res;
    }

    public TrackingRule(String s) {
        System.out.println("test2");
        this.materials = new ArrayList<>();
        this.flags = new ArrayList<>();
        this.nbt = new ArrayList<>();
        this.contains = new ArrayList<>();
        this.nbtEquals = new HashMap<>();
        this.isUnbreakable = null;

        List<String> materials;
        List<String> flags;

        this.isUnbreakable = configSection.getBoolean(s + ".unbreakable");
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

        this.print();

    }
    public static boolean containsString(ItemStack i, List<String> strings){
        System.out.println("contains one of string list : " + strings);
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

    public static boolean isUnbreakable(ItemStack i, Boolean isUnbreakable){
        if(isUnbreakable == null) return true;

        return(i.getItemMeta().isUnbreakable() == isUnbreakable.booleanValue());
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
        System.out.println("isofmaterial: " + isOfMaterial(itemStack, this.materials));
        System.out.println("hasFlag: " + hasFlag(itemStack, this.flags));
        System.out.println("hasNbt : " + hasNbt(itemStack, this.nbt));
        System.out.println("containsString : " + containsString(itemStack, this.contains));
        System.out.println("is unbreakable : " + isUnbreakable(itemStack, this.isUnbreakable));
        return isOfMaterial(itemStack, this.materials)
                && hasFlag(itemStack, this.flags)
                && hasNbt(itemStack, this.nbt)
                && hasKeyValueNbt(itemStack, this.nbtEquals)
                && containsString(itemStack, this.contains)
                && isUnbreakable(itemStack, this.isUnbreakable);
    }

    public void print(){
        System.out.println("TRACKING RULE N° " + this);
        System.out.println("MUST CONTAINS ONE OF THOSE WORDS:");
        for(String s : this.contains){
            System.out.println(s);
        }
        System.out.println("MUST BE OF ONE OF THOSE MATERIALS:");
        for(Material m : this.materials){
            System.out.println(m);
        }
        System.out.println("MUST HAVE ONE OF THOSE FLAGS:");
        for(ItemFlag f : this.flags){
            System.out.println(f);
        }
        System.out.println("MUST HAVE ONE OF THE FOLLOWING NBT/KEY ASSOCIATION:");
        for(String nbt : this.nbtEquals.keySet()){
            System.out.println(nbt + "," + this.nbtEquals.get(nbt));
        }
        System.out.println("MUST HAVE ONE OF THOSE NBT:");
        for(String nbt : this.nbt){
            System.out.println(nbt);
        }

        if(this.isUnbreakable){
            System.out.println("MUST BE UNBREAKABLE");
        }
        else if (this.isUnbreakable != null && this.isUnbreakable){
            System.out.println("MUST NOT BE UNBREAKABlE");
        }

    }
}
