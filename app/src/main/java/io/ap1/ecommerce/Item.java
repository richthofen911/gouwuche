package io.ap1.ecommerce;

/**
 * Created by admin on 15/06/15.
 */
public class Item {
    private String itemId;
    private String itemImage;
    private String itemName;
    private String itemQuantity;
    private String itemPrice;
    private String itemFingerprint;

    public Item(String itemId, String itemImage, String itemName, String itemQuantity, String itemPrice){
        this.itemId = itemId;
        this.itemImage = itemImage;
        this.itemName = itemName;
        this.itemQuantity = itemQuantity;
        this.itemPrice = itemPrice;
        this.itemFingerprint = itemId+itemQuantity;
    }

    public String getItemId(){
        return itemId;
    }

    public String getItemImage(){
        return itemImage;
    }

    public String getItemName(){
        return itemName;
    }

    public String getItemQuantity(){
        return itemQuantity;
    }

    public String getItemPrice(){
        return itemPrice;
    }

    public String getItemFingerprint() {return itemFingerprint;}

    public String toString(){
        return itemId + itemName + itemQuantity;
    }
}
