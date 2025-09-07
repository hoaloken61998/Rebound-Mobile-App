package com.rebound.models.Cart;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.lang.reflect.Type;
import com.google.gson.reflect.TypeToken;

public class CartItem {
    private String name;
    private String variant;
    private int price;
    private int quantity;
    private int imageResId;

    public CartItem(String name, String variant, int price, int quantity, int imageResId) {
        this.name = name;
        this.variant = variant;
        this.price = price;
        this.quantity = quantity;
        this.imageResId = imageResId;
    }

    public String getName() { return name; }
    public String getVariant() { return variant; }

    public int getPrice() { return price; }

    public int getQuantity() { return quantity; }
    public int getImageResId() { return imageResId; }

    public void setQuantity(int quantity) { this.quantity = quantity; }
    public int getTotalPrice() { return price * quantity; }

    public static void saveCart(Context context, String email, ArrayList<CartItem> cartItems) {
        SharedPreferences prefs = context.getSharedPreferences("cart_data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(cartItems);
        editor.putString(email, json);
        editor.apply();
    }

    public static ArrayList<CartItem> getCart(Context context, String email) {
        SharedPreferences prefs = context.getSharedPreferences("cart_data", Context.MODE_PRIVATE);
        String json = prefs.getString(email, null);
        if (json != null) {
            Type type = new TypeToken<ArrayList<CartItem>>() {}.getType();
            return new Gson().fromJson(json, type);
        }
        return new ArrayList<>();
    }
}