package fr.yronusa.sauron.SafeNBTAPI;
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


import org.bukkit.Bukkit;

public class VersionManager {

    public static final int MAJOR_VERSION;
    public static final int MINOR_VERSION;
    public static final int UPDATE_VERSION;
    public static final String VERSION_STRING;
    public static final String VERSION_STRING_FULL;

    public static boolean greaterOrEqual(int major, int minor){
        return MAJOR_VERSION > major || (MAJOR_VERSION >= major && MINOR_VERSION >= minor);
    }

    public static boolean greaterOrEqual(int major, int minor, int update){
        return MAJOR_VERSION > major || (MAJOR_VERSION >= major && (MINOR_VERSION > minor || (MINOR_VERSION >= minor && UPDATE_VERSION >= update)));
    }

    static{
        int major = 1;
        int minor = 17;
        int update = 0;

        try{
            String v = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
            String[] vs = v.split("_");

            try{
                major = Integer.parseInt(vs[0].replaceAll("[^0-9]", ""));
                minor = Integer.parseInt(vs[1].replaceAll("[^0-9]", ""));
                update = vs.length > 2 ? Integer.parseInt(vs[2].replaceAll("[^0-9]", "")) : 0;
            }
            catch(Exception ex){ex.printStackTrace();}
        }
        catch(Exception ignore){}
        finally{
            MAJOR_VERSION = major;
            MINOR_VERSION = minor;
            UPDATE_VERSION = update;

            VERSION_STRING = MAJOR_VERSION + "." + MINOR_VERSION;
            VERSION_STRING_FULL = UPDATE_VERSION == 0 ? VERSION_STRING : MAJOR_VERSION + "." + MINOR_VERSION + "." + UPDATE_VERSION;
        }
    }

}