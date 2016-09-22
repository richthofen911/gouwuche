package io.ap1.ecommerce;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.SyncStateContract;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;

import org.json.JSONException;
import org.json.JSONObject;

import io.ap1.libbeaconmanagement.Utils.ApiCaller;
import io.ap1.libbeaconmanagement.Utils.DefaultVolleyCallback;
//import project.richthofen911.callofdroidy.http.HTTPBuilder;
//import project.richthofen911.callofdroidy.parser.ParseJSON;


public class ActivityLogin extends Activity {
    private static final String TAG = "ActivityLogin";

    EditText et_email_login;
    EditText et_password_login;
    ImageButton imageButton_loginFemale;
    ImageButton imageButton_loginMale;
    ImageButton imageButton_loginGeneral;
    final static String id_female = "female@test.com";
    final static String id_male = "male@test.com";
    final static String id_general = "generic@test.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        et_email_login = (EditText) findViewById(R.id.et_email_login);
        et_password_login = (EditText) findViewById(R.id.et_password_login);
        String versionNumber = android.os.Build.VERSION.RELEASE.substring(0, 3);
        Log.e("os version", versionNumber);
        if(Float.parseFloat(versionNumber) < 4.4){
            Toast.makeText(getApplicationContext(), R.string.login_device_version_too_low, Toast.LENGTH_LONG).show();
            finish();
        }
        ImageView iv_logologin = (ImageView) findViewById(R.id.iv_logo_login);

        imageButton_loginFemale = (ImageButton) findViewById(R.id.login_female);
        imageButton_loginMale = (ImageButton) findViewById(R.id.login_male);
        imageButton_loginGeneral = (ImageButton) findViewById(R.id.login_general);

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            Toast.makeText(this, R.string.no_bluetooth_chip_found, Toast.LENGTH_SHORT).show();
            finish();
        } else if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
            // mBluetoothAdapter.disable();
        }

        imageButton_loginFemale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login(id_female);
            }
        });

        imageButton_loginMale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login(id_male);
            }
        });

        imageButton_loginGeneral.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login(id_general);
            }
        });

    }

    private void login(final String idEmail){
        ApiCaller.getInstance(getApplicationContext()).setAPI(Constant.server_url, "/kiosk/api/login.php?", "useremail=" + idEmail + "&password=qwer1234", null, Request.Method.GET)
                .exec(new DefaultVolleyCallback(this, "Processing"){
                    @Override
                    public void onDelivered(String result){
                        super.onDelivered(result);
                        Log.e(TAG, "onDelivered: " + result);
                        try{
                            JSONObject jsonObject = new JSONObject(result);
                            if(jsonObject.getBoolean("result")) {
                                Intent goToMain = new Intent(ActivityLogin.this, ActivityMain.class);
                                goToMain.putExtra("email", idEmail);
                                startActivity(goToMain);
                            } else
                                Toast.makeText(getApplicationContext(), R.string.login_fail, Toast.LENGTH_SHORT).show();
                        }catch (JSONException e){
                            Log.e(TAG, "delete beacon request onDelivered: " + e.toString());
                        }
                    }
                    @Override
                    public void onException(final String e){
                        super.onException(e);
                        Toast.makeText(ActivityLogin.this, e, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
