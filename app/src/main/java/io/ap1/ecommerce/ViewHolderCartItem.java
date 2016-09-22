package io.ap1.ecommerce;

import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;

import org.json.JSONException;
import org.json.JSONObject;

import io.ap1.libbeaconmanagement.Utils.ApiCaller;
import io.ap1.libbeaconmanagement.Utils.DefaultVolleyCallback;

public class ViewHolderCartItem extends RecyclerView.ViewHolder{
    private static final String TAG = "ViewHolderCartItem";

    public ImageView iv_thumbnail;
    public TextView tv_itemName;
    public TextView tv_itemQuantity;
    public TextView tv_itemPrice;
    public TextView tv_itemdelete;
    public TextView tv_itemId;
    public int selfPosition;

    public ViewHolderCartItem(View rootview){
        super(rootview);
        iv_thumbnail = (ImageView) rootview.findViewById(R.id.iv_item_thumbnail);
        tv_itemName = (TextView) rootview.findViewById(R.id.tv_item_name);
        tv_itemQuantity = (TextView) rootview.findViewById(R.id.tv_item_quantity);
        tv_itemPrice = (TextView) rootview.findViewById(R.id.tv_item_price);
        tv_itemdelete = (TextView) rootview.findViewById(R.id.tv_item_delete);
        tv_itemId = (TextView) rootview.findViewById(R.id.tv_itemid);

        tv_itemdelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                //doDeleteOneItem();
                ApiCaller.getInstance(v.getContext().getApplicationContext()).setAPI(Constant.server_url,
                        "/kiosk/api/cart.php?", "action=remove&itemid=" + tv_itemId.getText().toString(), null, Request.Method.GET)
                        .exec(new DefaultVolleyCallback(v.getContext(), "Processing"){
                            @Override
                            public void onDelivered(String result){
                                super.onDelivered(result);
                                Log.e(TAG, "onDelivered: " + result);
                                try{
                                    JSONObject jsonObject = new JSONObject(result);
                                    if(jsonObject.getBoolean("result")) {
                                        for(int i = 1; i < DataStore.items.size(); i++){
                                            if(DataStore.items.get(i).getItemId().equals(tv_itemId.getText().toString())){
                                                deleteData(i);
                                                for(int j = 0; j < DataStore.items.size(); j++) {
                                                    Log.e("items after delete", DataStore.items.get(j).toString());
                                                }
                                                break;
                                            }
                                        }
                                        for(int i = 0; i < DataStore.itemIds.size(); i++){
                                            if(DataStore.itemIds.get(i).equals(tv_itemId.getText().toString())){
                                                DataStore.itemIds.remove(i);
                                                break;
                                            }
                                        }
                                        for(int i = 0; i < DataStore.itemFingerprints.size(); i++){
                                            if(DataStore.itemFingerprints.get(i).equals(tv_itemId.getText().toString())){
                                                DataStore.itemFingerprints.remove(i);
                                                break;
                                            }
                                        }
                                        Toast.makeText(v.getContext(), R.string.delete_from_cart, Toast.LENGTH_SHORT).show();
                                    } else
                                        Toast.makeText(v.getContext(), "Delete request error", Toast.LENGTH_SHORT).show();
                                }catch (JSONException e){
                                    Log.e(TAG, "json parsing error" + e.toString());
                                }
                            }
                            @Override
                            public void onException(final String e){
                                super.onException(e);
                                Toast.makeText(v.getContext(), e, Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    // delete the item data from adapter's data source
    public void deleteData(int pos){
        DataStore.items.remove(pos);
        FragmentCart.adapterCartItems.notifyItemRemoved(pos);
        if (pos != DataStore.items.size() - 1) {
            FragmentCart.adapterCartItems.notifyItemRangeChanged(pos, DataStore.items.size() - pos);
        }
    }
}
