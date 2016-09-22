package io.ap1.libbeaconmanagement;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.google.gson.Gson;
import com.perples.recosdk.RECOBeaconRegion;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import io.ap1.libbeaconmanagement.Utils.ApiCaller;
import io.ap1.libbeaconmanagement.Utils.DataStore;
import io.ap1.libbeaconmanagement.Utils.DefaultVolleyCallback;
import io.ap1.libbeaconmanagement.Utils.ServiceBeaconDetection;
import io.ap1.libbeaconmanagement.Utils.CallBackSyncData;

public class ServiceBeaconManagement<T extends RecyclerView.Adapter> extends ServiceBeaconDetection {
    private static final String TAG = "ServiceBeaconMngt";

    protected SharedPreferences spHashValue;
    //protected int idparent;

    protected Handler handler;

    protected boolean needToResort = false;
    protected boolean needToResetNearbyStatus = false;

    protected Timer timerResortList;
    protected TimerTask timerTaskResortList;

    private T t;
    protected String currentAdapter = "";
    protected static final String adapterTypeAdmin = "AdapterBeaconNearbyAdmin";
    protected static final String adapterTypeUser = "AdapterBeaconNearbyUser";

    public ServiceBeaconManagement() {
    }

    public void onCreate() {
        super.onCreate();

        handler = new Handler(getMainLooper());
        timerResortList = new Timer(); // cancel it in onDestroy()
        spHashValue = getApplication().getSharedPreferences("HashValue.sp", 0);
    }

    @Override
    public IBinder onBind(Intent intent) {
        super.onBind(intent);

        return new BinderManagement(definedRegions);
    }

    @Override
    protected synchronized void actionOnEnterAp1Beacon(Beacon ap1Beacon){
        List<Beacon> beaconQueried = databaseHelper.queryBeacons(ap1Beacon);
        if(beaconQueried != null){
            // if beaconQueried is in local db, add it to detected&registerd list

            for(Beacon beacon : beaconQueried){
                Log.e(TAG, "actionOnEnterAp1Beacon: " + beacon.getMajor() + "-" + beacon.getMinor() + ": " + beacon.getIdparent() + " is in localDB");
                String nickname = beacon.getNickname();
                if(nickname == null)
                    beacon.setNickname("");
                else
                    beacon.setNickname(nickname);
                beacon.setUrlfar(beacon.getUrlfar());
                beacon.setUrlnear(beacon.getUrlnear());
                addToDetectedAndRegisteredList(DataStore.detectedAndAddedBeaconList.size(), beacon);
                /*
                if(beacon.getIdparent().equals(String.valueOf(idparent))){
                    Log.e(TAG, "actionOnEnterAp1Beacon: idparent match: " + idparent);
                    String nickname = beacon.getNickname();
                    if(nickname == null)
                        beacon.setNickname("");
                    else
                        beacon.setNickname(nickname);
                    beacon.setUrlfar(beacon.getUrlfar());
                    beacon.setUrlnear(beacon.getUrlnear());
                    addToDetectedAndRegisteredList(DataStore.detectedAndAddedBeaconList.size(), beacon);
                }else{
                    beacon.setNickname("Inactive");
                }
                */
                //addToDetectedList(DataStore.detectedBeaconList.size(), beacon);
            }
        }else
            ap1Beacon.setNickname("Inactive");
        addToDetectedList(DataStore.detectedBeaconList.size(), ap1Beacon);

        displayDetectedAndRegisteredBeaconsList("NewBeacon");
    }

    @Override
    protected void actionOnRssiChanged(int index, String newRssi){
        Beacon tmpBeacon = DataStore.detectedBeaconList.get(index);
        tmpBeacon.setRssi(newRssi);   // override the beacon's rssi in detectedBeaconList
        if(!needToResort)
            needToResort = true;

        int registeredListSize = DataStore.detectedAndAddedBeaconList.size();
        for(int j = 0; j < registeredListSize; j++){
            if(BeaconOperation.equals(tmpBeacon, DataStore.detectedAndAddedBeaconList.get(j))){
                DataStore.detectedAndAddedBeaconList.get(j).setRssi(newRssi); // override the beacon's rssi in detected and registeredBeaconList
                if(!needToResetNearbyStatus)
                    needToResetNearbyStatus = true;
            }
        }
    }

    protected void checkRemoteBeaconHash(final String urlPath, final CallBackSyncData callBackSyncData) {
        Map<String, String> postParams = new HashMap<>();
        postParams.put("hash", spHashValue.getString("hashBeacon", "empty"));
        postParams.put("idbundle", idBundle);

        ApiCaller.getInstance(getApplicationContext()).setAPI(DataStore.urlBase, urlPath, null, postParams, Request.Method.POST).exec(
                new DefaultVolleyCallback() {
                    @Override
                    public void onDelivered(String result) {
                        Log.e(TAG, "resp check beacon hash: " + result);
                        if (result.equals("1")) {
                            Log.e(TAG, "beacon hash local == remote");
                            DataStore.beaconAllList = groupBeaconsByCompany((ArrayList<Beacon>) databaseHelper.queryForAllBeacons());
                            // DataStore.registeredAndGroupedBeaconList = groupBeaconsByCompany();
                            callBackSyncData.onSuccess();
                        } else {
                            Log.e(TAG, "beacon hash local != remote");
                            try {
                                JSONObject jsonObject = new JSONObject(result);
                                String remoteBeaconHash = jsonObject.getString("hash");
                                databaseHelper.deleteAllBeacons(ServiceBeaconManagement.this); //drop the old beacon table and create a new one
                                clearDetectedBeaconList(); // clear current detected beacon list display
                                DataStore.detectedBeaconList.clear();
                                JSONArray beaconSetRemote = jsonObject.getJSONArray("beacons");
                                updateLocalBeaconDB(beaconSetRemote, callBackSyncData, remoteBeaconHash);
                            } catch (JSONException e) {
                                callBackSyncData.onFailure(e.toString());
                            }
                        }
                    }

                    @Override
                    public void onException(final String e) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ServiceBeaconManagement.this, e, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
    }

    protected void checkRemoteCompanyHash(final String urlPath, final CallBackSyncData callBackUpdateCompanySet){
        Map<String, String> postParams = new HashMap<>();
        postParams.put("hash", spHashValue.getString("hashCompany", "empty"));
        postParams.put("idbundle", idBundle);

        ApiCaller.getInstance(getApplicationContext()).setAPI(DataStore.urlBase, urlPath, null, postParams, Request.Method.POST).exec(
                new DefaultVolleyCallback() {
                    @Override
                    public void onDelivered(String result) {
                        Log.e(TAG, "resp check company hash: " + result);
                        if (result.equals("1")) {
                            Log.e(TAG, "company hash local == remote");
                            DataStore.beaconAllList = (ArrayList<Beacon>) databaseHelper.queryForAllBeacons();
                            callBackUpdateCompanySet.onSuccess();
                        } else {
                            Log.e(TAG, "company hash local != remote");
                            try {
                                JSONObject jsonObject = new JSONObject(result);
                                String remoteCompanyHash = jsonObject.getString("hash");
                                databaseHelper.deleteAllCompanies(); //drop the old company table and create a new one
                                JSONArray companySetRemote = jsonObject.getJSONArray("companies");
                                updateLocalCompanyDB(companySetRemote, callBackUpdateCompanySet, remoteCompanyHash);
                            } catch (JSONException e) {
                                callBackUpdateCompanySet.onFailure(e.toString());
                            }
                        }
                    }

                    @Override
                    public void onException(final String e) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ServiceBeaconManagement.this, e, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
    }

    protected void updateLocalBeaconDB(JSONArray newBeaconSet, final CallBackSyncData callBackSyncData, String remoteBeaconHash){ //save all beacons into local DB one by one
        Log.e(TAG, "updateing local beacons table...");
        Gson gson = new Gson(); //use Gson to parse a Beacon JSONObject to a POJO
        int newBeaconSetLength = newBeaconSet.length();
        for(int i = 0; i < newBeaconSetLength; i++){
            try{
                Beacon beaconFromNewRemoteSet = gson.fromJson(newBeaconSet.getJSONObject(i).toString(), Beacon.class);
                databaseHelper.saveBeacon(beaconFromNewRemoteSet); //add all the beacons to the new DB
            }catch (JSONException e) {
                Log.e("Beacons traversal error", e.toString());
            }
        }
        spHashValue.edit().putString("hashBeacon", remoteBeaconHash).apply();
        // DataStore.registeredAndGroupedBeaconList = groupBeaconsByCompany();
        DataStore.beaconAllList = groupBeaconsByCompany((ArrayList<Beacon>) databaseHelper.queryForAllBeacons());
        Log.e(TAG, "update beacon info success");
        callBackSyncData.onSuccess();
    }

    protected void updateLocalCompanyDB(JSONArray newCompanySet, final CallBackSyncData callBackUpdateCompanySet, String remoteCompanyHash){
        Log.e(TAG, "update local companies table...");
        Gson gson = new Gson();
        int newCompanySetLength = newCompanySet.length();
        for(int i = 0; i < newCompanySetLength; i++){
            try{
                Company companyFromRemoteSet = gson.fromJson(newCompanySet.getJSONObject(i).toString(), Company.class);
                databaseHelper.saveCompany(companyFromRemoteSet);
            }catch (JSONException e){
                Log.e(TAG, "update company err " + e.toString());
                callBackUpdateCompanySet.onFailure(e.toString());
            }
        }
        spHashValue.edit().putString("hashCompany", remoteCompanyHash).apply();
        Log.e(TAG, "update company info success");
        callBackUpdateCompanySet.onSuccess();
    }

    public ArrayList<Beacon> getBeaconsWithAppIdParent(String idparent){
        return (ArrayList<Beacon>) databaseHelper.queryForBeaconsWithAppIdParent(idparent);
    }

    protected void saveUrlContent(String beaconUrl, String urlContent){ //save url content as local html file
        FileOutputStream outputStream;
        try{
            String fileName = beaconUrl.replace(":", "");
            fileName = fileName.replace("/", "");
            fileName = fileName.replace(".", "");
            File fileUrlContent = new File(getExternalCacheDir().getPath() + "/" + fileName + ".html");
            if(!fileUrlContent.exists()){
                outputStream = new FileOutputStream(new File(getExternalCacheDir().getPath() + "/" + fileName + ".html"));
                outputStream.write(urlContent.getBytes());
                outputStream.close();
                Log.e("write url content", "success");
            }else
                Log.e("write url content", "file exited already");  //may need to modify here for updating file ******
        }catch (Exception e){
            Log.e("write url content", "error");
        }
    }

    protected void addToDetectedList(int position, Beacon newBeacon) {
        DataStore.detectedBeaconList.add(newBeacon);
        Log.e("new detected beacon", "added");

        Log.e("adapter type", currentAdapter);
        if(currentAdapter.equals(adapterTypeAdmin)){
            t.notifyItemInserted(position);
            if (position != DataStore.detectedBeaconList.size() - 1) {
                Log.e("notifyItemRangeChanged", "position extends");
                t.notifyItemRangeChanged(position, DataStore.detectedBeaconList.size() - position);
            }
        }
    }

    protected void addToDetectedAndRegisteredList(int position, Beacon newBeacon){
        DataStore.detectedAndAddedBeaconList.add(newBeacon);
        Log.e("new registered beacon", "added");

        if(currentAdapter.equals(adapterTypeUser))
            t.notifyItemInserted(position);
            if(position != DataStore.detectedAndAddedBeaconList.size() - 1){
                t.notifyItemRangeChanged(position, DataStore.detectedAndAddedBeaconList.size() - position);
            }
    }

    protected final void deleteFromDetectedList(int pos){
        DataStore.detectedBeaconList.remove(pos);
        /*
        t.notifyItemRemoved(pos);
        if(pos != detectedBeaconList.size() - 1) {
            t.notifyItemRangeChanged(pos, detectedBeaconList.size() - pos);
        }
        */
    }

    // this method is called when updating/reseting local beacon db
    protected void clearDetectedBeaconList(){
        for(int i = DataStore.detectedBeaconList.size() - 1; i >= 0; i--){
            deleteFromDetectedList(i);
        }
    }

    // use registeredBeacon as input and return beacons grouped by companyid
    protected ArrayList<Beacon> groupBeaconsByCompany(ArrayList<Beacon> ungroupedBeacons){
        // ArrayList<Beacon> ungroupedRegisterdBeacons = getBeaconsWithAppIdParent(String.valueOf(idparent));
        Map<String, ArrayList<Beacon>> groupedBeacons = new HashMap<String, ArrayList<Beacon>>();
        for (Beacon beacon: ungroupedBeacons) {
            String key = beacon.getIdcompany();
            if (groupedBeacons.get(key) == null) {
                groupedBeacons.put(key, new ArrayList<Beacon>());
                Beacon groupDivider = new Beacon(); // create a fake beacon as a group divider for different companies
                groupDivider.setIdcompany(key);
                groupDivider.setNickname("groupDivider");
                groupedBeacons.get(key).add(groupDivider);
            }
            groupedBeacons.get(key).add(beacon);
        }

        ArrayList<Beacon> groupedBeaconsList = new ArrayList<>();
        for(ArrayList<Beacon> beaconGroup : groupedBeacons.values())
                groupedBeaconsList.addAll(beaconGroup);

        /*
        ArrayList<Beacon> groupedBeaconsList = new ArrayList<>();
        for(ArrayList<Beacon> beaconGroup : groupedBeacons.values()){
            Beacon groupDivider = new Beacon(); // create a fake beacon as a group divider for different companies
            String idCompany = (beaconGroup.get(0)).getIdcompany();
            groupDivider.setIdcompany(idCompany);
            groupDivider.setNickname("groupDivider");
            groupedBeaconsList.add(groupDivider);
            groupedBeaconsList.addAll(beaconGroup);
        }

        for(Beacon beacon : groupedBeaconsList)
            Log.e(TAG, "groupRegisteredBeacon: " + beacon.getIdcompany() + ":" + beacon.getMajor() + "-" + beacon.getMinor());
        */
        return groupedBeaconsList;
    }

    public void setAdapter(@NonNull T t){
        this.t = t;
        currentAdapter = t.getClass().getSimpleName();
        timerTaskResortList = new TimerTask() {
            @Override
            public void run() {
                if(needToResort){
                    Log.e("resorting list", "...");
                    Collections.sort(DataStore.detectedBeaconList); // resort beacon sequence by rssi
                    //displayDetectedBeaconsList("afterResort");
                    needToResort = false;
                    //if(currentAdapter.equals(adapterTypeAdmin))
                    //ServiceBeaconManagement.this.t.notifyItemRangeChanged(0, ServiceBeaconManagement.this.t.getItemCount());
                }
                if(needToResetNearbyStatus){
                    //if(currentAdapter.equals(adapterTypeUser))
                    //    ServiceBeaconManagement.this.t.notifyItemRangeChanged(0, ServiceBeaconManagement.this.t.getItemCount());
                    needToResetNearbyStatus = false;
                }
                ServiceBeaconManagement.this.t.notifyItemRangeChanged(0, ServiceBeaconManagement.this.t.getItemCount());
            }
        };
    }

    public T getAdapter(){
        return t;
    }

    // this method is just for debug
    private void displayDetectedAndRegisteredBeaconsList(String step){
        StringBuilder stringBuilder = new StringBuilder();
        for(Beacon beacon : DataStore.detectedAndAddedBeaconList){
            stringBuilder.append(beacon.getMajor()).append("-").append(beacon.getMinor()).append("\n");
        }
        Log.e(TAG, "D&R beacons: " + stringBuilder.toString());
    }


    public void onDestroy(){
        super.onDestroy();
        timerResortList.cancel();
        timerResortList.purge();
    }

    public class BinderManagement extends ServiceBeaconDetection.BinderDetection {
        public BinderManagement(ArrayList<RECOBeaconRegion> definedRegions){
            super(definedRegions);
        }

        /*
        public void getIdparent(){
            try {
                ApplicationInfo appInfo = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
                idparent = appInfo.metaData.getInt("idparent");
                Log.e(TAG, "App idparent" + idparent);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        */

        @Override
        public void startScanning(){
            super.startScanning();
            timerResortList.schedule(timerTaskResortList, 15000, 15000);
        }

        @Override
        public void stopScanning(){
            super.stopScanning();
            timerResortList.cancel();
        }

        public void getRemoteBeaconHash(String apiPath, CallBackSyncData callBackSyncData){
            checkRemoteBeaconHash(apiPath, callBackSyncData);
        }

        public void getRemoteCompanyHash(String apiPath, CallBackSyncData callBackUpdateCompanySet){
            checkRemoteCompanyHash(apiPath, callBackUpdateCompanySet);
        }

        public void setListAdapter(T t){ //must set an adapter, it cannot be null
            setAdapter(t);
        }

        public ArrayList<Beacon> getBeaconInAllPlaces(){
            return DataStore.beaconAllList;
        }

        public ArrayList<Beacon> getBeaconDetected(){
            return DataStore.detectedBeaconList;
        }

        public ArrayList<Beacon> getBeaconDetectedAndRegistered(){
            return DataStore.detectedAndAddedBeaconList;
        }

        // return beacons that (have same idparent as registered in this app)
        public ArrayList<Beacon> getMyBeacons(){
            return DataStore.registeredAndGroupedBeaconList;
        }

        public T getListAdapter(){
            return getAdapter();
        }

        public void setUrlBase(String url){
            DataStore.urlBase = url;
        }

        public String getUrlBase(){
            return DataStore.urlBase;
        }
    }
}
