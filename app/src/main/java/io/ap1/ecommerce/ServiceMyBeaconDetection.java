package io.ap1.ecommerce;

import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.perples.recosdk.RECOBeacon;
import com.perples.recosdk.RECOBeaconRegion;

import java.util.ArrayList;

import io.ap1.libbeaconmanagement.Utils.ServiceBeaconDetection;

public class ServiceMyBeaconDetection extends ServiceBeaconDetection {
    private static final String TAG = "ServiceMyBeaconDetect";

    public ServiceMyBeaconDetection() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        super.onBind(intent);

        return new BinderMyBeaconDetection(definedRegions);
    }

    @Override
    protected void actionOnEnter(RECOBeacon recoBeacon){
        Log.e(TAG, "actionOnEnter: " + recoBeacon.getProximityUuid() + ":" + recoBeacon.getMajor() + ":" + recoBeacon.getMinor());
        String message = "enter|" + recoBeacon.getProximityUuid() + "|" + recoBeacon.getMajor() + "|" + recoBeacon.getMinor();
        broadcastLocally("beaconInOut", message);
    }

    @Override
    protected void actionOnExit(RECOBeacon recoBeacon){
        String message = "exit|empty";
        broadcastLocally("beaconInOut", message);
    }

    private void broadcastLocally(String intentName, String intentData){
        Intent intent = new Intent(intentName);
        intent.putExtra("message", intentData);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    protected class BinderMyBeaconDetection extends BinderDetection {

        public BinderMyBeaconDetection(ArrayList<RECOBeaconRegion> definedRegions){
            super(definedRegions);
        }

        public void startScanning(){
            super.startScanning();
        }

        public void stopScanning(){
            super.stopScanning();
        }
    }
}
