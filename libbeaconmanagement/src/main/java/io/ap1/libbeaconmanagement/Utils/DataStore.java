package io.ap1.libbeaconmanagement.Utils;

import java.util.ArrayList;

import io.ap1.libbeaconmanagement.Beacon;
import io.ap1.libbeaconmanagement.Company;

/**
 * Created by admin on 01/03/16.
 */
public class DataStore {

    public static ArrayList<Beacon> detectedBeaconList = new ArrayList<>();
    public static ArrayList<Beacon> detectedAndAddedBeaconList = new ArrayList<>();
    public static ArrayList<Beacon> registeredAndGroupedBeaconList = new ArrayList<>(); // grouped by company
    public static ArrayList<Beacon> beaconAllList = new ArrayList<>(); // all the beacons having the same idparent, grouped by company id
    public static ArrayList<Company> companyInList = new ArrayList<>();
    public static String urlBase = "http://159.203.15.175/filemaker";
}
