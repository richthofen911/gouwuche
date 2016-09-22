package io.ap1.libbeaconmanagement.Utils;

import android.app.ProgressDialog;
import android.content.Context;

/**
 * Created by admin on 16/10/15.
 */
public class CallBackSyncData {
    private ProgressDialog progressDialog;

    public CallBackSyncData(Context context, String message){
        progressDialog = ProgressDialog.show(context, "", message, true );
        progressDialog.setCancelable(true);
    }

    public void onSuccess(){
        if(progressDialog != null)
            progressDialog.cancel();
    }

    public void onFailure(String cause){
        if(progressDialog != null)
            progressDialog.cancel();
    }
}
