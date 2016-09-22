package io.ap1.ecommerce;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;

public class AdapterCartItems extends RecyclerView.Adapter<ViewHolderCartItem>{

    public ArrayList<Item> items = new ArrayList<>();
    private Context context;

    public AdapterCartItems(ArrayList<Item> newItems, Context context){
        items = newItems;
        this.context = context;
    }

    public static double totalPrice = 0;

    @Override
    public ViewHolderCartItem onCreateViewHolder(ViewGroup viewGroup, int viewType){
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item,viewGroup,false);
        return new ViewHolderCartItem(view);
    }

    @Override
    public void onBindViewHolder(ViewHolderCartItem viewHolder, int position) {
        //Do something to bind data with the viewHolder
        viewHolder.setIsRecyclable(false);
        String urlThumbnail = items.get(position).getItemImage();
        if(!urlThumbnail.equals("Image")){//if it is not the first default record
            if(!items.get(position).getItemId().equals("total")) {//if it is not the last json object for total price
                Picasso.with(context).load(urlThumbnail).into(viewHolder.iv_thumbnail);
                //show itemName
                try {
                    String itemNameChin = URLDecoder.decode(items.get(position).getItemName(), "utf-8");
                    viewHolder.tv_itemName.setText(itemNameChin);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                //show itemQuantity
                viewHolder.tv_itemQuantity.setText(items.get(position).getItemQuantity());
                //set itemId to the invisible view just for saving the data
                viewHolder.tv_itemId.setText(items.get(position).getItemId());
                //show itemPrice with two digits after decimal point
                viewHolder.tv_itemPrice.setText("￥ " + items.get(position).getItemPrice());
                viewHolder.tv_itemdelete.setText("X");
            }else{
                viewHolder.iv_thumbnail.setMaxHeight(0);
                viewHolder.tv_itemName.setHeight(0);
                viewHolder.tv_itemQuantity.setHeight(0);
                viewHolder.tv_itemPrice.setHeight(0);
                FragmentCart.tv_totalPrice.setText(items.get(position).getItemQuantity());
            }

        }else {// if it is the first default record, just show the title
            viewHolder.iv_thumbnail.setBackgroundColor(Color.parseColor("#FEFAFA"));
            try {
                String itemNameChin = URLDecoder.decode(items.get(position).getItemName(), "utf-8");
                viewHolder.tv_itemName.setText(R.string.item_name);
            }catch (UnsupportedEncodingException e){
                e.printStackTrace();
            }
            viewHolder.tv_itemQuantity.setText(R.string.item_quantity);
            viewHolder.tv_itemPrice.setText(R.string.item_price);
            viewHolder.tv_itemId.setText(items.get(position).getItemId());
        }
        viewHolder.selfPosition = position;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

}
