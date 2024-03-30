package fr.yronusa.sauron.SafeNBTAPI;/*

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


import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;

public enum SafeNBTBaseType{

    BYTE(byte.class, "Byte"),
    BYTE_ARRAY(byte[].class, "ByteArray"),
    DOUBLE(double.class, "Double"),
    FLOAT(float.class, "Float"),
    INT(int.class, "Int"),
    INT_ARRAY(int[].class, "IntArray"),
    LONG(long.class, "Long"),
    SHORT(short.class, "Short"),
    STRING(String.class, "String");

    private Class<?> innerClazz;
    private Class<?> nbtBaseClass;
    private String name;

    <T> SafeNBTBaseType(Class<T> innerClazz, String name){
        try{
            this.innerClazz = innerClazz;
            String version = "net.minecraft.server." + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
            if(VersionManager.greaterOrEqual(1, 17)){
                this.nbtBaseClass = Class.forName("net.minecraft.nbt.NBTBase");
            }
            else{
                this.nbtBaseClass = Class.forName(version + ".NBTTag" + name);
            }
            this.name = name;
        }
        catch(Exception ex){ex.printStackTrace();}
    }

    public static SafeNBTBaseType get(Class<?> clazz){
        for(SafeNBTBaseType type : values()){
            if(type.innerClazz.equals(clazz)){
                return type;
            }
        }

        if(clazz == Float.class){return FLOAT;}
        else if(clazz == Integer.class){return INT;}
        return null;
    }

    public String getName(){
        return this.name;
    }

    public <T> Object make(T value){
        try{
            Constructor m = nbtBaseClass.getConstructor(innerClazz);
            m.setAccessible(true);
            Object o = m.newInstance(value);
            m.setAccessible(false);

            return o;
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    public static SafeNBTBaseType getByClass(Class<?> clazz){
        for(SafeNBTBaseType t : values()){
            if(t.innerClazz.equals(clazz)){return t;}
        }
        return null;
    }

    public static SafeNBTBaseType getByNBTBaseClass(Class<?> clazz){
        for(SafeNBTBaseType t : values()){
            if(t.nbtBaseClass.equals(clazz)){return t;}
        }
        return null;
    }

    public static SafeNBTBaseType getFromObject(@NotNull Object o){
        SafeNBTBaseType t = getByClass(o.getClass());
        if(t != null){return t;}

        try{
            return getByClass((Class) o.getClass().getField("TYPE").get(null));
        }
        catch(Exception ignore){}
        return null;
    }
}