package io.ap1.ecommerce;

import java.util.ArrayList;

public class DataStore {
    private static boolean checkedInAlready = false;
    private static Item item;
    public static ArrayList<Item> items = new ArrayList<>();
    public static ArrayList<String> itemFingerprints = new ArrayList<>();
    public static ArrayList<String> itemIds = new ArrayList<>();
    public static String userEmail = "";

    public static boolean getInoutStatus(){
        return checkedInAlready;
    }
    public static Item getAItem() {
        return item;
    }

    public static void setInoutStatus(boolean status){
        checkedInAlready = status;
    }
    public static void setMessageUrl(Item aItem){
        item= aItem;
    }
}
