package io.ap1.ecommerce;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import io.ap1.libbeaconmanagement.Utils.ApiCaller;
import io.ap1.libbeaconmanagement.Utils.DefaultVolleyCallback;

public class FragmentCart extends android.support.v4.app.Fragment {
    private final static String TAG = "FragmentCart";

    protected Context mContext;

    private Timer timer = new Timer();
    private String lastQueryCartResult = "";
    public boolean queryDBAgain = false;
    public boolean queryQueueAgain = false;
    //private String userEmail;
    private ImageButton imageButton_checkout;
    private ImageButton imageButton_clearall;

    private RecyclerView recyclerView_messages;
    public static AdapterCartItems adapterCartItems;
    public static TextView tv_totalPrice;

    public FragmentCart() {
        super();
        // All subclasses of Fragment must include a public empty constructor. See Android docs.
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container);
        //btn_checkout = (Button) view.findViewById(R.id.btn_checkout);
        //btn_clearAll = (Button) view.findViewById(R.id.btn_clearAll);
        //userEmail = ((ActivityMain)getActivity()).useremail;

        imageButton_checkout = (ImageButton) view.findViewById(R.id.imagebutton_checkout);
        imageButton_clearall = (ImageButton) view.findViewById(R.id.imagebutton_clearall);
        tv_totalPrice = (TextView) view.findViewById(R.id.tv_price_number);
        recyclerView_messages = (RecyclerView) view.findViewById(R.id.recyclerview_messages);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView_messages.setLayoutManager(linearLayoutManager);
        recyclerView_messages.setHasFixedSize(true);
        adapterCartItems = new AdapterCartItems(DataStore.items, getActivity());
        recyclerView_messages.setAdapter(adapterCartItems);

        imageButton_checkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptConfirmCheckout();
            }
        });

        imageButton_clearall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doClearCart();
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        mContext = context;
    }

    public void doNextQueryCart(){
        ApiCaller.getInstance(getContext().getApplicationContext()).setAPI(Constant.server_url, "/kiosk/api/getcart.php?", "useremail=" + ((ActivityMain)getActivity()).useremail, null, Request.Method.GET)
                .exec(new DefaultVolleyCallback(getActivity(), "Processing"){
                    @Override
                    public void onDelivered(String result){
                        super.onDelivered(result);
                        Log.e(TAG, "query cart, onDelivered: " + result);
                        if(!result.equals(lastQueryCartResult)){
                            try{
                                JSONArray jsonArray = new JSONArray(result);
                                ArrayList<JSONObject> jsonObjects = new ArrayList<>();
                                for(int i = 0; i < jsonArray.length(); i++)
                                    jsonObjects.add(jsonArray.getJSONObject(i));
                                Item newItem = null;
                                for (JSONObject jsonObject: jsonObjects){
                                    //try{
                                    newItem = new Item(
                                            jsonObject.getString("itemid"), jsonObject.getString("thumbnail"),
                                            jsonObject.getString("itemname"),jsonObject.getString("quantity"),
                                            jsonObject.getString("price"));
                                    String thisFingerprint = newItem.getItemFingerprint();
                                    String thisItemId = newItem.getItemId();
                                    //add new item to the list
                                    if(!DataStore.itemIds.contains(thisItemId)){
                                        DataStore.itemIds.add(thisItemId);
                                        //extend the items list with the last record and fill the new item in the last but one position
                                        if(DataStore.items.size() >= 2){
                                            addData(DataStore.items.size() - 1, DataStore.items.get(DataStore.items.size() - 1));
                                            DataStore.items.set(DataStore.items.size() - 2, newItem);
                                            for(int i = 0; i < DataStore.items.size(); i++){
                                                Log.e("items content", DataStore.items.get(i).toString());
                                            }
                                        }else {
                                            addData(DataStore.items.size(), newItem);
                                            for(int i = 0; i < DataStore.items.size(); i++){
                                                Log.e("items content", DataStore.items.get(i).toString());
                                            }
                                        }
                                    }else{
                                        if(!DataStore.itemFingerprints.contains(thisFingerprint)){//for the same itemid but quantity changhed
                                            for(int i = 0; i < DataStore.items.size(); i++){
                                                if(DataStore.items.get(i).getItemId().equals(thisItemId)){
                                                    DataStore.items.set(i, newItem);

                                                    adapterCartItems.notifyItemChanged(i);
                                                }
                                            }
                                        }
                                    }
                                }

                            }catch (JSONException e){
                                Toast.makeText(getContext(), "JSON parsing error: " + e.toString(), Toast.LENGTH_SHORT).show();
                            }
                            lastQueryCartResult = result;
                        }else
                            Log.e(TAG, "shopping cart didn't change");
                        if(queryDBAgain){
                            timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    Log.e("do next query cart", "");
                                    doNextQueryCart();
                                }
                            }, 1000);
                        }else
                            Log.e("Query is stopped", "");
                    }
                    @Override
                    public void onException(final String e){
                        super.onException(e);
                        Toast.makeText(getActivity(), e, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void doNextQueryQueue(){
        ApiCaller.getInstance(getContext().getApplicationContext()).setAPI(Constant.server_url, "/kiosk/api/queue.php", null, null, Request.Method.GET)
                .exec(new DefaultVolleyCallback(getActivity(), "Processing"){
                    @Override
                    public void onDelivered(String result){
                        super.onDelivered(result);
                        Log.e(TAG, "query queue, onDelivered: " + result);
                        try{
                            JSONObject jsonObject = new JSONObject(result);
                            if(jsonObject.getBoolean("result")){
                                Log.e(TAG, "query queue, onDelivered: my email: " + ((ActivityMain)getActivity()).useremail);
                                if((jsonObject.getString("useremail").equals(((ActivityMain)getActivity()).useremail))){
                                    queryQueueAgain = false;
                                    promptIfStartShopping();
                                }
                            }
                        }catch (JSONException e){
                            Log.e(TAG, "JSON parsing error: " + e.toString());
                        }
                        if(queryQueueAgain){
                            timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    Log.e(TAG, "do next query queue");
                                    doNextQueryQueue();
                                }
                            }, 2000);
                        }else
                            Log.e(TAG, "exit, not in queue");
                    }
                    @Override
                    public void onException(final String e){
                        super.onException(e);
                        Toast.makeText(getActivity(), e, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void doStartShopping(){
        ApiCaller.getInstance(getContext().getApplicationContext()).setAPI(Constant.server_url, "/kiosk/api/kiosk.php?", "action=start&useremail=" + ((ActivityMain)getActivity()).useremail, null, Request.Method.GET)
                .exec(new DefaultVolleyCallback(getActivity(), "Processing"){
                    @Override
                    public void onDelivered(String result){
                        super.onDelivered(result);
                        Log.e(TAG, "start shopping, onDelivered: " + result);
                        try{
                            JSONObject jsonObject = new JSONObject(result);
                            if(jsonObject.getString("result").equals("true")){
                                queryDBAgain = true;
                                doNextQueryCart();
                            }else
                                Toast.makeText(getContext(), getResources().getString(R.string.fail_to_start), Toast.LENGTH_SHORT).show();
                        }catch (JSONException e){
                            Log.e(TAG, "JSON parsing error: " + e.toString());
                        }
                    }
                    @Override
                    public void onException(final String e){
                        super.onException(e);
                        Toast.makeText(getActivity(), e, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void doClearCart(){
        ApiCaller.getInstance(getContext().getApplicationContext()).setAPI(Constant.server_url, "/kiosk/api/cart.php?", "action=removeall", null, Request.Method.GET)
                .exec(new DefaultVolleyCallback(getActivity(), "Processing"){
                    @Override
                    public void onDelivered(String result){
                        super.onDelivered(result);
                        Log.e(TAG, "clear cart, onDelivered: " + result);
                        try{
                            JSONObject jsonObject = new JSONObject(result);
                            if(jsonObject.getBoolean("result")) {
                                for(int i = DataStore.items.size() - 1; i > 0; i--){
                                    DataStore.items.remove(i);
                                    adapterCartItems.notifyItemRemoved(i);
                                }

                                for(int i = DataStore.itemIds.size() - 1; i > 0; i--){
                                    DataStore.itemIds.remove(i);
                                }
                                for(int i = DataStore.itemFingerprints.size() - 1; i > 0; i--){
                                    DataStore.itemFingerprints.remove(i);
                                }

                                doNextQueryCart();
                            } else
                                Toast.makeText(getContext(), "fail to clear cart", Toast.LENGTH_SHORT).show();
                        }catch (JSONException e){
                            Log.e(TAG, "delete beacon request onDelivered: " + e.toString());
                        }
                    }
                    @Override
                    public void onException(final String e){
                        super.onException(e);
                        Toast.makeText(getActivity(), e, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void doLeaveQueue(String email){
        ApiCaller.getInstance(mContext.getApplicationContext()).setAPI(Constant.server_url, "/kiosk/api/leavequeue.php?", "email=" + email, null, Request.Method.GET)
                .exec(new DefaultVolleyCallback(mContext, "Processing"){
                    @Override
                    public void onDelivered(String result){
                        super.onDelivered(result);
                        Log.e(TAG, "leave queue, onDelivered: " + result);
                        try{
                            JSONObject jsonObject = new JSONObject(result);
                            if(jsonObject.getBoolean("result"))
                                Toast.makeText(mContext, R.string.thank_you_for_your_purchase, Toast.LENGTH_SHORT).show();
                        }catch (JSONException e){
                            Log.e(TAG, "JSON parsing error: " + e.toString());
                        }
                    }
                    @Override
                    public void onException(final String e){
                        super.onException(e);
                        Toast.makeText(getActivity(), e, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void doCheckout(String email){
        ApiCaller.getInstance(mContext.getApplicationContext()).setAPI(Constant.server_url, "/kiosk/api/cart.php?", "action=checkout&email=" + email, null, Request.Method.GET)
                .exec(new DefaultVolleyCallback(mContext, "Processing"){
                    @Override
                    public void onDelivered(String result){
                        super.onDelivered(result);
                        Log.e(TAG, "checkout, onDelivered: " + result);
                    }
                    @Override
                    public void onException(final String e){
                        super.onException(e);
                        Toast.makeText(getActivity(), e, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void promptIfStartShopping(){
        AlertDialog.Builder promptBuilder = new AlertDialog.Builder(getActivity());
        promptBuilder.setTitle("")
                .setMessage(R.string.if_start_shopping)
                .setPositiveButton(R.string.button_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        doStartShopping();
                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton(R.string.button_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        doLeaveQueue(((ActivityMain)getActivity()).useremail);
                        dialogInterface.dismiss();
                    }
                });
        final AlertDialog alertDialog =promptBuilder.create();
        alertDialog.show();
    }

    public void promptConfirmCheckout(){
        AlertDialog.Builder promptBuilder = new AlertDialog.Builder(getActivity());
        promptBuilder.setTitle("")
                .setMessage(R.string.confirm_purchase)
                .setPositiveButton(R.string.button_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        queryDBAgain = false;
                        doCheckout(((ActivityMain)getActivity()).useremail);
                        doClearCart();
                        startActivity(new Intent(getActivity(), ActivityCheckout.class));
                        dialogInterface.dismiss();
                        //ActivityOnlineUserList.this.start(definedRegions);
                    }
                })
                .setNegativeButton(R.string.button_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        //ActivityOnlineUserList.this.start(definedRegions);
                    }
                });
        final AlertDialog alertDialog =promptBuilder.create();
        alertDialog.show();
    }

    public void addData(int pos, Item newItem){
        DataStore.items.add(pos, newItem);
        adapterCartItems.notifyItemInserted(pos);
        if (pos != DataStore.items.size() - 1) {
            adapterCartItems.notifyItemRangeChanged(pos, DataStore.items.size() - pos);
        }
    }
}
