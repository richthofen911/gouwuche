package io.ap1.ecommerce;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;

/**
 * Created by admin on 23/02/16.
 */
public class PermissionHandler {
    public static String[] checkPermissions(Context context, String[] permissionNames){
        ArrayList<String> notGrantedPermissions = new ArrayList<>();
        for(String permissionName : permissionNames){
            if(ContextCompat.checkSelfPermission(context, permissionName) != PackageManager.PERMISSION_GRANTED)
                notGrantedPermissions.add(permissionName);
        }

        if(notGrantedPermissions.size() > 0)
            return notGrantedPermissions.toArray(new String[notGrantedPermissions.size()]);
        else
            return null;  // this means the required permissions have been granted already
    }

    public static void requestPermissions(Activity activity, String[] permissionNames, int requestCode){
        ActivityCompat.requestPermissions(activity, permissionNames, requestCode);
    }
}
