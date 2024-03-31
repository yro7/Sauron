/*

   Copyright 2019-2021 jojodmo

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

*/

package fr.yronusa.sauron.SafeNBTAPI;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class SafeNBT {

    private static final String version = "net.minecraft.server." + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    private static final String cbVersion = "org.bukkit.craftbukkit." + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    static Class<?> tagCompoundClass;
    private static Class<?> nbtBaseClass;
    private static Class<?> nmsItemstackClass;
    private static Class<?> craftItemstackClass;
    static Class<?> mojangsonParserClass;

    // MODIFIED VERSION

    public static Method m;

    public static Method hasKeyMethod;
    public static Method getCompound;
    private final Object tagCompund;
    public static Method nmsGetMethod;

    public static Method nbtSetMethod;



    static{
        try{

            if(VersionManager.greaterOrEqual(1, 17)){
                tagCompoundClass = Class.forName("net.minecraft.nbt.NBTTagCompound");
                nbtBaseClass = Class.forName("net.minecraft.nbt.NBTBase");
                nmsItemstackClass = Class.forName("net.minecraft.world.item.ItemStack");
                mojangsonParserClass = Class.forName("net.minecraft.nbt.MojangsonParser");
            }
            else{
                tagCompoundClass = Class.forName(version + ".NBTTagCompound");
                nbtBaseClass = Class.forName(version + ".NBTBase");
                nmsItemstackClass = Class.forName(version + ".ItemStack");
                mojangsonParserClass = Class.forName(version + ".MojangsonParser");
            }
            craftItemstackClass = Class.forName(cbVersion + ".inventory.CraftItemStack");

            m = craftItemstackClass.getMethod("asNMSCopy", ItemStack.class);
            getCompound = nmsItemstackClass.getMethod("getTag");
            hasKeyMethod = tagCompoundClass.getMethod("hasKey", String.class);
            nmsGetMethod = craftItemstackClass.getMethod("asNMSCopy", ItemStack.class);
            nbtSetMethod = nmsItemstackClass.getMethod("setTag", tagCompoundClass);
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public SafeNBT() {
        this(null);
    }

    public SafeNBT(Object tagCompound) {
        Object toSet = tagCompound;
        if(tagCompound == null){
            try{
                toSet = tagCompoundClass.newInstance();
            }
            catch(Exception ex){
                ex.printStackTrace();
            }
        }
        this.tagCompund = toSet;
    }

    public Object getTagCompund(){
        return tagCompund;
    }

    @Nullable
    public SafeNBT getCompoundNullable(String key){
        try{
            return getCompoundThrows(key);
        }
        catch(Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

    public SafeNBT getCompoundThrows(String key) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException{
        Method m = tagCompoundClass.getMethod("getCompound", String.class);
        m.setAccessible(true);
        Object r = m.invoke(this.tagCompund, key);
        m.setAccessible(false);
        return r == null ? null : new SafeNBT(r);
    }

    @NotNull
    public SafeNBT getCompound(String key) {
        SafeNBT nbt = getCompoundNullable(key);
        return nbt == null ? null : new SafeNBT();
    }

    @Nullable
    public SafeNBTList getListNullable(String key){
        try{
            return getListThrows(key);
        }
        catch(Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

    public SafeNBTList getListThrows(String key)  throws NoSuchMethodException, InvocationTargetException, IllegalAccessException{
        Method m = tagCompoundClass.getMethod("get", String.class);
        m.setAccessible(true);
        Object r = m.invoke(this.tagCompund, key);
        m.setAccessible(false);
        return r == null ? null : new SafeNBTList(r);
    }

    public void setObject(String key, Object o) throws NoSuchMethodException {
        if(o instanceof String){setString(key, (String) o);}
        else if(o instanceof Integer){setInt(key, (Integer) o);}
        else if(o instanceof Double){setDouble(key, (Double) o);}
        else if(o instanceof Long){setLong(key, (Long) o);}
        else if(o instanceof List){
            SafeNBTList list = new SafeNBTList();
            for(Object e : (List) o){
                if(e instanceof Map){
                    SafeNBT mapNBT = new SafeNBT();
                    for(Object k : ((Map) e).keySet()){
                        if(k instanceof String){
                            Object v = ((Map) e).get(k);
                            mapNBT.setObject((String) k, v);
                        }
                    }
                    list.add(mapNBT);
                }
                else{
                    list.addGeneric(e);
                }
            }
            set(key, list);
        }
    }

    @NotNull
    public SafeNBTList getList(String key){
        SafeNBTList nbt = getListNullable(key);
        return nbt == null ? null : new SafeNBTList();
    }

    public String getString(String key){
        try{
            Method m = tagCompoundClass.getMethod("getString", String.class);
            m.setAccessible(true);
            Object r = m.invoke(this.tagCompund, key);
            m.setAccessible(false);
            return r instanceof String ? (String) r : null;
        }
        catch(Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

    public void setString(String key, String value){
        try{
            Method m = tagCompoundClass.getMethod("setString", String.class, String.class);
            m.setAccessible(true);
            m.invoke(this.tagCompund, key, value);
            m.setAccessible(false);
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public Integer getInt(String key){
        try{
            Method m = tagCompoundClass.getMethod("getInt", String.class);
            m.setAccessible(true);
            Object r = m.invoke(this.tagCompund, key);
            m.setAccessible(false);
            return r instanceof Integer ? (Integer) r : null;
        }
        catch(Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

    public void setInt(String key, Integer value){
        try{
            Method m = tagCompoundClass.getMethod("setInt", String.class, int.class);
            m.setAccessible(true);
            m.invoke(this.tagCompund, key, value);
            m.setAccessible(false);
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public void setShort(String key, Short value){
        try{
            Method m = tagCompoundClass.getMethod("setShort", String.class, short.class);
            m.setAccessible(true);
            m.invoke(this.tagCompund, key, value);
            m.setAccessible(false);
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public void setBoolean(String key, Boolean value){
        try{
            Method m = tagCompoundClass.getMethod("setBoolean", String.class, boolean.class);
            m.setAccessible(true);
            m.invoke(this.tagCompund, key, value);
            m.setAccessible(false);
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public void setDouble(String key, Double value){
        try{
            Method m = tagCompoundClass.getMethod("setDouble", String.class, double.class);
            m.setAccessible(true);
            m.invoke(this.tagCompund, key, value);
            m.setAccessible(false);
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public Long getLong(String key){
        try{
            Method m = tagCompoundClass.getMethod("getLong", String.class);
            m.setAccessible(true);
            Object r = m.invoke(this.tagCompund, key);
            m.setAccessible(false);
            return r instanceof Long ? (Long) r : null;
        }
        catch(Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

    public void setLong(String key, Long value){
        try{
            Method m = tagCompoundClass.getMethod("setLong", String.class, long.class);
            m.setAccessible(true);
            m.invoke(this.tagCompund, key, value);
            m.setAccessible(false);
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public void set(String key, SafeNBT value){
        try{
            Method m = tagCompoundClass.getMethod("set", String.class, nbtBaseClass);
            m.setAccessible(true);
            m.invoke(this.tagCompund, key, value.tagCompund);
            m.setAccessible(false);
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public void set(String key, SafeNBTList value){
        try{
            Method m = tagCompoundClass.getMethod("set", String.class, nbtBaseClass);
            m.setAccessible(true);
            m.invoke(this.tagCompund, key, value.getTagList());
            m.setAccessible(false);
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public void set(String key, SafeNBTBaseType type, Object value){
        try{
            Object toPut = type.make(value);
            Method m = tagCompoundClass.getMethod("set", String.class, nbtBaseClass);
            m.setAccessible(true);
            m.invoke(this.tagCompund, key, toPut);
            m.setAccessible(false);
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }


    public void setStrings(Map<String, String> map){
        try{
            Method m = tagCompoundClass.getMethod("setString", String.class, String.class);
            m.setAccessible(true);
            map.forEach((String key, String value) -> {
                try{
                    m.invoke(this.tagCompund, key, value);
                }
                catch(Exception ex){ex.printStackTrace();}
            });

            m.setAccessible(false);
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public boolean hasKey(String key){
        try{
            hasKeyMethod.setAccessible(true);
            Object o = hasKeyMethod.invoke(this.tagCompund, key);
            hasKeyMethod.setAccessible(false);

            return o instanceof Boolean && (Boolean) o;
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

    public ItemStack apply(ItemStack item){
        try{
            nmsGetMethod.setAccessible(true);
            Object nmsStack = nmsGetMethod.invoke(null, item);
            nmsGetMethod.setAccessible(false);

            nbtSetMethod.setAccessible(true);
            nbtSetMethod.invoke(nmsStack, this.tagCompund);
            nbtSetMethod.setAccessible(false);

            Method m = craftItemstackClass.getMethod("asBukkitCopy", nmsItemstackClass);
            m.setAccessible(true);
            Object o = m.invoke(null, nmsStack);
            m.setAccessible(false);

            return o instanceof ItemStack ? (ItemStack) o : null;
        }
        catch(Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

    public static SafeNBT get(ItemStack item){
        try{
            m.setAccessible(true);
            Object nmsStack = m.invoke(null, item);
            m.setAccessible(false);

            getCompound.setAccessible(true);
            Object nbtCompound = getCompound.invoke(nmsStack);
            getCompound.setAccessible(false);

            return new SafeNBT(nbtCompound);
        }
        catch(Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

    public Set<String> getKeys(){
        try{
            Map m = null;
            if(VersionManager.greaterOrEqual(1, 17)){
                try{
                    Field f = tagCompoundClass.getDeclaredField("x");
                    f.setAccessible(true);
                    m = (Map) f.get(tagCompund);
                    f.setAccessible(false);
                }
                catch(Exception ignore){}
                for(Field f : tagCompoundClass.getDeclaredFields()){
                    if(f.getType() == Map.class){
                        f.setAccessible(true);
                        m = (Map) f.get(tagCompund);
                        f.setAccessible(false);
                        break;
                    }
                }
            }
            else{
                Field f = tagCompoundClass.getDeclaredField("map");
                f.setAccessible(true);
                m = (Map) f.get(tagCompund);
                f.setAccessible(false);
            }

            return (Set<String>) m.keySet();
        }
        catch(Exception ex){
            ex.printStackTrace();
            return new HashSet<>();
        }

    }

    public String toString(){
        return "SafeNBT(" + compoundString() + ")";
    }

    public String compoundString(){return Objects.toString(tagCompund);}
}