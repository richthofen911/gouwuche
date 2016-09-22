package io.ap1.libbeaconmanagement.Utils;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.j256.ormlite.android.apptools.OrmLiteBaseService;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.perples.recosdk.RECOBeacon;
import com.perples.recosdk.RECOBeaconManager;
import com.perples.recosdk.RECOBeaconRegion;
import com.perples.recosdk.RECOErrorCode;
import com.perples.recosdk.RECORangingListener;
import com.perples.recosdk.RECOServiceConnectListener;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import io.ap1.libbeaconmanagement.Beacon;
import io.ap1.libbeaconmanagement.BeaconOperation;

// this class must extends OrmLiteBaseService because the subclass cannot extends that service, it need to extends this service
public class ServiceBeaconDetection extends OrmLiteBaseService<DatabaseHelper> implements
        RECOServiceConnectListener, RECORangingListener{
    private static final String TAG = "ServiceBeaconDetection";

    protected String idBundle = "undefined"; // will be app package name

    private final boolean DISCONTINUOUS_SCAN = false;
    protected boolean entered = false;
    protected int exitCount = 0;
    protected boolean exited = false;
    protected int rssiBorder = 0;
    protected int currentMinor = 0;
    protected boolean generalSearchMode = false;
    protected boolean isNewDetected = true;

    protected DatabaseHelper databaseHelper;
    protected Dao<Beacon, Integer> mBeaconDao;
    protected RuntimeExceptionDao<Beacon, Integer> mBeaconRuntimeExceptionDao;

    protected RECOBeaconManager mRecoManager = RECOBeaconManager.getInstance(this, false, false);
    protected ArrayList<RECOBeaconRegion> definedRegions;


    public ServiceBeaconDetection() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mRecoManager.setRangingListener(this);
        Log.e("binding BeaconManager", "");
        mRecoManager.bind(this);

        databaseHelper = DatabaseHelper.getHelper(this);
        try{
            mBeaconDao = databaseHelper.getBeaconDao();
        }catch (SQLException e){
            Log.e("sql exception", e.toString());
        }
        mBeaconRuntimeExceptionDao = databaseHelper.getBeaconRuntimeExceptionDao();

    }

    @Override
    public IBinder onBind(Intent intent) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter != null){
            bluetoothAdapter.enable(); // if Bluetooth Adapter exists, force enabling it.
            Log.e("Bluetooth enabled", "");
        }else
            Toast.makeText(getApplicationContext(), "Bluetooth chip not found", Toast.LENGTH_SHORT).show();

        Bundle regionDefinition = intent.getExtras();
        Log.e("extract settings", "search region configs");
        Log.e("uuid", regionDefinition.getString("uuid"));
        Log.e("major", regionDefinition.getInt("major") + "");
        Log.e("minor", regionDefinition.getInt("minor") + "");
        Log.e("borderValue", regionDefinition.getInt("borderValue") + "");
        Log.e("useGeneralSearchMode", regionDefinition.getBoolean("useGeneralSearchMode") + "");
        idBundle = regionDefinition.getString("idbundle");
        //idparent = regionDefinition.getString("idparent");
        //Log.e("idparent", idparent);
        defineRegionArgs(regionDefinition.getString("uuid"), regionDefinition.getInt("major"), regionDefinition.getInt("minor"), regionDefinition.getInt("borderValue"), regionDefinition.getBoolean("useGeneralSearchMode"));
        Log.e("definedRegion", definedRegions.get(0).getProximityUuid() + regionDefinition.getInt("major") + regionDefinition.getInt("minor"));
        return new BinderDetection(definedRegions);
    }

    //legal value for major and minor is between 0 ~ 65535, using -1 here means ignore this value when defining the region
    protected void defineRegionArgs(String uuid, int major, int minor, int borderValue, boolean useGeneralSearchMode){
        if(major < -1 || minor < -1)
            Toast.makeText(getApplicationContext(), "Illegal Major or Minor value was defined", Toast.LENGTH_SHORT).show();
        else {
            if(major == -1 && minor == -1)
                definedRegions = generateBeaconRegion(uuid);
            if(major >=0 && minor < -1)
                definedRegions = generateBeaconRegion(uuid, major);
            if(major >=0 && minor >= 0)
                definedRegions = generateBeaconRegion(uuid, major, minor);
            if(major == -1 && minor >=0)
                Toast.makeText(getApplicationContext(), "Major value cannot be -1 when Minor value is greater then -1", Toast.LENGTH_SHORT).show();
            rssiBorder = borderValue;
            generalSearchMode = useGeneralSearchMode;
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter != null){
            bluetoothAdapter.enable(); // if Bluetooth Adapter exists, force enabling it.
            Log.e("Bluetooth enabled", "");
        }else
            Toast.makeText(getApplicationContext(), "Bluetooth chip not found", Toast.LENGTH_SHORT).show();

        Bundle regionDefinition = intent.getExtras();
        defineRegionArgs(regionDefinition.getString("uuid"), regionDefinition.getInt("major"), regionDefinition.getInt("minor"), regionDefinition.getInt("borderValue"), regionDefinition.getBoolean("useGeneralSearchMode"));

        return START_STICKY;
    }


    public void start(ArrayList<RECOBeaconRegion> regions) {  // this has to be called after onServiceConnect()

        for(RECOBeaconRegion region : regions) {
            try {
                mRecoManager.startRangingBeaconsInRegion(region);
                Log.e(TAG, "start detecting: " + region.describeContents());

                //timerResortList.schedule(timerTaskResortList, 15000);
            } catch (RemoteException e) {
                Log.i("RECORangingActivity", "Remote Exception");
                e.printStackTrace();
            } catch (NullPointerException e) {
                Log.i("RECORangingActivity", "Null Pointer Exception");
                e.printStackTrace();
            }
        }
    }

    public void stop(ArrayList<RECOBeaconRegion> regions) {
        Log.e("stop detecting", "...");
        for(RECOBeaconRegion region : regions) {
            try {
                mRecoManager.stopRangingBeaconsInRegion(region);
                entered = false;
                //timerResortList.cancel();
            } catch (RemoteException e) {
                Log.i("RECORangingActivity", "Remote Exception");
                e.printStackTrace();
            } catch (NullPointerException e) {
                Log.i("RECORangingActivity", "Null Pointer Exception");
                e.printStackTrace();
            }
        }
    }

    private ArrayList<RECOBeaconRegion> generateBeaconRegion(String uuid) {
        ArrayList<RECOBeaconRegion> regions = new ArrayList<>();
        RECOBeaconRegion recoRegion;
        recoRegion = new RECOBeaconRegion(uuid, "Defined Region");
        regions.add(recoRegion);
        return regions;
    }

    private ArrayList<RECOBeaconRegion> generateBeaconRegion(String uuid, int major) {
        ArrayList<RECOBeaconRegion> regions = new ArrayList<>();
        RECOBeaconRegion recoRegion;
        recoRegion = new RECOBeaconRegion(uuid, major, "Defined Region");
        regions.add(recoRegion);
        return regions;
    }

    private ArrayList<RECOBeaconRegion> generateBeaconRegion(String uuid, int major, int minor) {
        ArrayList<RECOBeaconRegion> regions = new ArrayList<>();
        RECOBeaconRegion recoRegion;
        recoRegion = new RECOBeaconRegion(uuid, major, minor, "Defined Region");
        regions.add(recoRegion);
        return regions;
    }

    @Override
    public void onServiceConnect() {
        Log.e(TAG, "connected");
        Log.e("BeaconManager", "is bound");
        Log.e("isReadyToScan", "true");
        mRecoManager.setDiscontinuousScan(DISCONTINUOUS_SCAN);
        Intent intent = new Intent("beaconInOut");
        intent.putExtra("message", "readyToScan|yes");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        //start(definedRegions);
    }

    @Override
    public void onServiceFail(RECOErrorCode recoErrorCode) {
        Log.e(TAG, "RECO service fail: " + recoErrorCode.toString());
    }

    protected void actionOnEnter(RECOBeacon recoBeacon){}

    protected void actionOnEnterAp1Beacon(Beacon ap1Beacon){}

    protected void actionOnExit(RECOBeacon recoBeacon){}

    protected void actionOnExitAp1Beacon(Beacon ap1Beacon){}

    protected void actionOnRssiChanged(int index, String newRssi){}

    private void inOut(int theRssi, RECOBeacon recoBeacon){
        if(!generalSearchMode){ // the mode to find a specific target beacon
            if(theRssi > rssiBorder){ // if the beacon is detected and its rssi is strong enough, which means it is the beacon for the specific location, not a random one
                if(!entered){ //if haven't entered, do it
                    exitCount = 0;
                    entered = true;
                    exited = false;
                    Log.e(TAG, "enter beacon region: " + recoBeacon.getProximityUuid() + " :: " + recoBeacon.getMajor() + " :: " + recoBeacon.getMinor());
                    currentMinor = recoBeacon.getMinor();
                    actionOnEnter(recoBeacon);
                }else{
                    Log.e("entered already", ")");
                }
            }else{ // a beacon is detected but its rssi is too weak, won't play with it
                if(recoBeacon.getMinor() == currentMinor){
                    if(exitCount < 3){
                        exitCount++;
                    }else {
                        if(!exited){ // if haven't exited, do it
                            entered = false;
                            exited = true;
                            currentMinor = 0;
                            actionOnExit(recoBeacon);
                        }else {
                            Log.e("exited already", ")");
                        }
                    }
                }else{
                    Log.e("not this beacon", String.valueOf(recoBeacon.getMinor()));
                }
            }
        }else { // general scanning mode
            Beacon detectedBeacon = new Beacon();
            detectedBeacon.setUuid(recoBeacon.getProximityUuid());
            detectedBeacon.setMajor(String.valueOf(recoBeacon.getMajor()));
            detectedBeacon.setMinor(String.valueOf(recoBeacon.getMinor()));
            //Log.e(TAG, "inOut: new detected Rssi: " + recoBeacon.getRssi());
            detectedBeacon.setRssi(String.valueOf(recoBeacon.getRssi()));

            for(int i = 0; i < DataStore.detectedBeaconList.size(); i++){
                Beacon beaconInDetectedList = DataStore.detectedBeaconList.get(i);
                if(BeaconOperation.equals(detectedBeacon, beaconInDetectedList)){
                    isNewDetected = false;
                    //Log.e("beacon in list", "already");
                    // if absolute value > 3, determine the rssi has changed

                    String rssiThisTime = detectedBeacon.getRssi();
                    String rssiLastTime = beaconInDetectedList.getRssi();
                    if(rssiLastTime.equals(""))
                        rssiLastTime = "-60";
                    if(Math.abs(Integer.parseInt(rssiThisTime) - Integer.parseInt(rssiLastTime)) > 3){
                        //Log.e("but rssi", "has changed, need to resort list");
                        actionOnRssiChanged(i, detectedBeacon.getRssi());
                    }
                    break;
                }
            }
            if(isNewDetected){
                actionOnEnterAp1Beacon(detectedBeacon);
            }
            isNewDetected = true;  // reset isNewDetected flag to true;
        }
    }



    @Override
    public void didRangeBeaconsInRegion(Collection<RECOBeacon> recoBeacons, RECOBeaconRegion recoBeaconRegion) {
        synchronized (recoBeacons){
            for(RECOBeacon recoBeacon: recoBeacons){
                //Log.e("beacon detected, rssi", String.valueOf(recoBeacon.getRssi()));
                inOut(recoBeacon.getRssi(), recoBeacon);
            }
        }
    }

    @Override
    public void rangingBeaconsDidFailForRegion(RECOBeaconRegion recoBeaconRegion, RECOErrorCode recoErrorCode) {
        Log.e("RECO ranging error:", recoErrorCode.toString());
    }

    @Override
    public boolean onUnbind(Intent intent){
        try{
            mRecoManager.unbind();
            Log.e("Reco manager", "unbound");
        }catch (RemoteException e){
            Log.e("on destroy error", e.toString());
        }
        super.onUnbind(intent);

        return false;
    }

    @Override
    public void onDestroy(){
        if(mRecoManager.isBound()){
            try{
                mRecoManager.unbind();
                Log.e("Reco manager", "unbound");
            }catch (RemoteException e){
                Log.e("on destroy error", e.toString());
            }
        }
        super.onDestroy();
    }

    protected class BinderDetection extends Binder {
        protected ArrayList<RECOBeaconRegion> definedRegions;

        public BinderDetection(ArrayList<RECOBeaconRegion> definedRegions){
            this.definedRegions = definedRegions;
            if(this.definedRegions != null)
                Log.e("regions in base", "not null");
            else
                Log.e("regions in base", "null");

        }

        public void startScanning(){
            start(definedRegions);
        }

        public void stopScanning(){
            stop(definedRegions);
        }
    }
}
