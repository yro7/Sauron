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

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

public class SafeNBTList{

    private static final String version = "net.minecraft.server." + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    private static final String cbVersion = "org.bukkit.craftbukkit." + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    private static Class<?> tagListClass;
    private static Class<?> nbtBaseClass;

    private final Object tagList;
    private static final List<Method> getMethods = new ArrayList<>();

    static{
        try{
            if(VersionManager.greaterOrEqual(1, 17)){
                tagListClass = Class.forName("net.minecraft.nbt.NBTTagList");
                nbtBaseClass = Class.forName("net.minecraft.nbt.NBTBase");
            }
            else{
                tagListClass = Class.forName(version + ".NBTTagList");
                nbtBaseClass = Class.forName(version + ".NBTBase");
            }

            for(Method m : tagListClass.getDeclaredMethods()){
                if(m.getReturnType().equals(Void.TYPE) || m.getParameterCount() != 1 || !m.getParameterTypes()[0].equals(int.class)){continue;}
                if(m.getName().equalsIgnoreCase("remove")){continue;}
                getMethods.add(m);
            }
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public Object getTagList(){
        return tagList;
    }

    public SafeNBTList(){
        this(null);
    }

    public SafeNBTList(Object tagCompound){
        Object toSet = tagCompound;
        if(tagCompound == null){
            try{
                toSet = tagListClass.newInstance();
            }
            catch(Exception ex){
                ex.printStackTrace();
            }
        }
        this.tagList = toSet;
    }

    public boolean isEmpty(){
        try{
            Method m = tagListClass.getMethod("isEmpty");
            m.setAccessible(true);
            Object r = m.invoke(this.tagList);
            m.setAccessible(false);
            return r instanceof Boolean ? (Boolean) r : true;
        }
        catch(Exception ex){
            ex.printStackTrace();
            return true;
        }
    }

    public int size(){
        try{
            Method m = tagListClass.getMethod("size");
            m.setAccessible(true);
            Object r = m.invoke(this.tagList);
            m.setAccessible(false);
            return (Integer) r;
        }
        catch(Exception ex){
            ex.printStackTrace();
            return 0;
        }
    }

    public List<Object> values(){
        try{
            List<Object> res = new ArrayList<>();

            for(int i = 0; i < size(); i++){
                Object o = null;
                for(Method m : getMethods){
                    m.setAccessible(true);
                    o = m.invoke(this.tagList, i);
                    m.setAccessible(false);
                    if(o != null){
                        if(o instanceof Number && ((Number) o).intValue() == 0){continue;}
                        if(o instanceof String && ((String) o).length() == 0){continue;}
                        if(SafeNBT.tagCompoundClass.isInstance(o)){
                            SafeNBT s = new SafeNBT(o);
                            if(s.getKeys().isEmpty()){continue;}
                        }
                        if(tagListClass.isInstance(o)){
                            SafeNBTList s = new SafeNBTList(o);
                            if(s.isEmpty()){continue;}
                        }
                        if(o.getClass().isArray()){
                            if(Array.getLength(o) == 0){
                                continue;
                            }
                        }
                        break;
                    }
                }

                if(SafeNBT.tagCompoundClass.isInstance(o)){
                    o = new SafeNBT(o);
                }
                else if(tagListClass.isInstance(o)){
                    o = new SafeNBTList(o);
                }

                res.add(o);
            }

            return res;
        }
        catch(Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

    public void add(SafeNBT value){
        add(value.getTagCompund());
    }

    public <T> void add(SafeNBTBaseType type, T value){
        add(type.make(value));
    }

    public <T> void add(SafeNBTBaseType type, T... values){
        for(T value : values){
            add(type, value);
        }
    }

    public <T> void addGeneric(T value){
        if(value == null){return;}
        SafeNBTBaseType type = SafeNBTBaseType.get(value.getClass());
        if(type == null){return;}
        add(type, value);
    }

    public <T> void add(T... values){
        SafeNBTBaseType type = values.length > 0 ? SafeNBTBaseType.getByClass(values[0].getClass()) : null;
        if(type != null){
            add(type, values);
        }
    }

    private void add(Object nbt){
        try{
            Method m = AbstractList.class.getMethod("add", Object.class);
            m.setAccessible(true);
            m.invoke(tagList, nbt);
            m.setAccessible(false);
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }
}