package com.rebound.data;



import com.rebound.R;
import com.rebound.models.Cart.CartItem;

import java.util.ArrayList;
import java.util.List;

public class CartData {
    private List<CartItem> items;
    private int deliveryFee = 35000;
    private int discount = 0;

    public CartData() {
        items = new ArrayList<>();
        items.add(new CartItem("TOTE EARRINGS", "Silver", 250000, 1, R.mipmap.earrings1));
        items.add(new CartItem("ANIL HUGGIES", "Silver", 500000, 1, R.mipmap.earrings2));
        items.add(new CartItem("WEEKENDER FLASH", "Gold", 1875000, 2, R.mipmap.earrings3));
    }

    public List<CartItem> getItems() { return items; }
    public int getSubTotal() {
        int total = 0;
        for (CartItem item : items) total += item.getTotalPrice();
        return total;
    }
    public int getTotal() { return getSubTotal() + deliveryFee - discount; }
    public int getDeliveryFee() { return deliveryFee; }
    public int getDiscount() { return discount; }
    public void setDiscount(int discount) { this.discount = discount; }
}