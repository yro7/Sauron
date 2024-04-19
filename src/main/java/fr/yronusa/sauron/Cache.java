package fr.yronusa.sauron;

import java.util.HashSet;

public class Cache {


    public static HashSet<TrackedItem> updatingItems;


    public static void initialize(){
        updatingItems = new HashSet<>();
    }
}
