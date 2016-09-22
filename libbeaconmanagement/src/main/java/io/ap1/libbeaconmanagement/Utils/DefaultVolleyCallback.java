package io.ap1.libbeaconmanagement.Utils;

import android.app.ProgressDialog;
import android.content.Context;

/**
 * Created by admin on 20/04/16.
 */
public class DefaultVolleyCallback {
    private ProgressDialog progressDialog;

    public DefaultVolleyCallback(){}

    public DefaultVolleyCallback(Context context, String message){
        //progressDialog = ProgressDialog.show(context, "", message, true );
        if(progressDialog != null)
            progressDialog.setCancelable(true);
    }

    public void onDelivered(final String result){
        if(progressDialog != null)
            progressDialog.cancel();
    }

    public void onException(final String e){
        if(progressDialog != null)
            progressDialog.cancel();
    }
}
