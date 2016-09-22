package io.ap1.ecommerce;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.provider.SyncStateContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.ap1.libbeaconmanagement.Utils.*;

public class ActivityMain extends AppCompatActivity {
    private static final String TAG = "ActivityMain";

    private final boolean DISCONTINUOUS_SCAN = false;
    private boolean entered = false;
    private int exitCount = 0;
    private boolean exited = false;
    private int rssiBorder = -100;
    private int currentMinor = 0;

    public List<Fragment> fragments = new ArrayList<>();
    private RadioGroup radioGroup;
    private RadioButton radioButton_cart;
    private RadioButton radioButton_settings;
    public String useremail;
    private TextView tv_email;
    private static Context contextApplication;

    public FragmentManager fragmentManager;
    private FragmentCart fragmentCart;

    protected ServiceMyBeaconDetection.BinderMyBeaconDetection binderMyBeaconDetection;

    private BroadcastReceiver mMessageReceiver;
    private ServiceConnection connMyBeaconDetection;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        useremail = getIntent().getStringExtra("email");
        Log.e(TAG, "onCreate: useremail: " + useremail);

        //fragmentCart = new FragmentCart();
        fragmentManager = getSupportFragmentManager();
        //fragmentManager.beginTransaction();
        fragmentCart = (FragmentCart) fragmentManager.findFragmentById(R.id.fragment_cart);
        //fragments.add(fragmentCart);

        connMyBeaconDetection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.e(TAG, "onServiceConnected: ");
                binderMyBeaconDetection = (ServiceMyBeaconDetection.BinderMyBeaconDetection) service;
                binderMyBeaconDetection.startScanning();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.e(TAG, "onServiceConnected: ");
            }
        };

        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String rawMessage = intent.getStringExtra("message");
                //Log.e(TAG, "onReceive: " + rawMessage);
                String[] message = rawMessage.split("\\|");
                //Log.e(TAG, "onReceive: message[0]: " + message[0]);
                if(message[0].equals("enter")){ // onEnter
                    Toast.makeText(getApplicationContext(), "beacon detected", Toast.LENGTH_SHORT).show();
                    //Log.e(TAG, "onReceive: checking in");
                    checkIn(useremail, message[1], message[2], message[3]);
                }else if(message[0].equals("exit")){ // onExit
                    Toast.makeText(getApplicationContext(), R.string.beacon_region_leave, Toast.LENGTH_SHORT).show();
                    fragmentCart.queryQueueAgain = false;
                    fragmentCart.doLeaveQueue(useremail);
                }else { // on ready to scan
                    binderMyBeaconDetection.startScanning();
                }
            }
        };

        String[] notGrantedPermission = PermissionHandler.checkPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION});
        if(notGrantedPermission != null)
            PermissionHandler.requestPermissions(this, notGrantedPermission, Constant.PERMISSION_REQUEST_CODE_ACCESS_FINE_LOCATION);
        else {
            bindServiceMyBeaconDetection();
        }

    }

    @Override
    public void onResume(){
        super.onResume();
        // Register mMessageReceiver to receive messages.
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("beaconInOut"));
    }

    @Override
    protected void onPause() {
        // Unregister since the activity is not visible
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onPause();
    }

    protected void bindServiceMyBeaconDetection(){
        Log.e(TAG, "binding ServiceMyBeaconDetection");
        Bundle beaconInfo = new Bundle();
        beaconInfo.putString("uuid", Constant.UUID_AprilBrother);
        beaconInfo.putInt("major", 777);
        beaconInfo.putInt("minor", 3);
        beaconInfo.putInt("borderValue", rssiBorder);
        beaconInfo.putBoolean("useGeneralSearchMode", false);
        //beaconInfo.putString("idparent", "11");
        bindService(new Intent(ActivityMain.this, ServiceMyBeaconDetection.class).putExtras(beaconInfo), connMyBeaconDetection, BIND_AUTO_CREATE);
    }

    public void checkIn(String email, String uuid, String major, String minor){
        Log.e(TAG, "checkIn: ");
        ApiCaller.getInstance(getApplicationContext()).setAPI(Constant.server_url, "/kiosk/api/checkin.php?",
                "email=" + email + "&uuid=" + uuid + "&major=" + major + "&minor=" + minor, null, Request.Method.GET)
                .exec(new DefaultVolleyCallback(this, "Processing"){
                    @Override
                    public void onDelivered(String result){
                        super.onDelivered(result);
                        Log.e(TAG, "checkin onDelivered: " + result);
                        try{
                            JSONObject jsonObject = new JSONObject(result);
                            if(jsonObject.getBoolean("result")) {
                                Toast.makeText(getApplicationContext(), R.string.beacon_region_enter, Toast.LENGTH_SHORT).show();
                                fragmentCart.queryQueueAgain = true;
                                fragmentCart.doNextQueryQueue();
                            } else
                                Toast.makeText(getApplicationContext(), R.string.beacon_region_fail_to_enter, Toast.LENGTH_SHORT).show();
                        }catch (JSONException e){
                            Log.e(TAG, "delete beacon request onDelivered: " + e.toString());
                        }
                    }
                    @Override
                    public void onException(final String e){
                        super.onException(e);
                        Toast.makeText(ActivityMain.this, e, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case Constant.PERMISSION_REQUEST_CODE_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    bindServiceMyBeaconDetection();
                } else {
                    Snackbar.make(findViewById(R.id.main_screen), "Location permission is required to display beacon detection results and to show the beacon map",
                            Snackbar.LENGTH_LONG)
                            .setAction("OK", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    PermissionHandler.requestPermissions(ActivityMain.this,
                                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                            Constant.PERMISSION_REQUEST_CODE_ACCESS_FINE_LOCATION);
                                }
                            })
                            .show();
                }
            }
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();

        if(binderMyBeaconDetection != null && binderMyBeaconDetection.isBinderAlive())
            unbindService(connMyBeaconDetection);
        DataStore.items.clear();
        DataStore.itemFingerprints.clear();
        DataStore.itemIds.clear();
        fragmentCart.doCheckout(useremail);
        fragmentCart.doLeaveQueue(useremail);
    }
}
