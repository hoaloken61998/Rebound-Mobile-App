package com.rebound.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rebound.models.Orders.Order;

import java.util.ArrayList;
import java.util.List;

public class OrderManager {

    private static final String PREFS_NAME = "OrderPrefs";

    private static OrderManager instance;
    private final SharedPreferences sharedPreferences;
    private final Gson gson = new Gson();
    private String userEmail = "";

    private OrderManager(Context context) {
        sharedPreferences = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static void init(Context context) {
        if (instance == null) {
            instance = new OrderManager(context);
        }
    }

    public static OrderManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("OrderManager chưa được init(Context)!");
        }
        return instance;
    }

    public void setUserEmail(String email) {
        this.userEmail = email;
    }

    private String getOrderKey() {
        return "orders_" + userEmail;
    }

    public void addOrder(Order newOrder) {
        List<Order> current = getOrders();
        current.add(newOrder);
        saveOrders(current);
    }

    public void deleteOrder(Order orderToDelete) {
        List<Order> current = getOrders();
        current.remove(orderToDelete); // ensure equals() implemented in Order
        saveOrders(current);
    }

    public void updateOrderStatus(Order updatedOrder) {
        List<Order> orders = getOrders();
        boolean updated = false;
        for (int i = 0; i < orders.size(); i++) {
            if (updatedOrder.getOrderID() != null && updatedOrder.getOrderID().equals(orders.get(i).getOrderID())) {
                orders.set(i, updatedOrder);
                updated = true;
                break;
            }
        }
        if (updated) {
            saveOrders(orders); // Chỉ lưu nếu có cập nhật
        }
    }

    public List<Order> getOrders() {
        String json = sharedPreferences.getString(getOrderKey(), null);
        if (json == null) return new ArrayList<>();
        List<Order> orders = gson.fromJson(json, new TypeToken<List<Order>>() {}.getType());
        return orders != null ? orders : new ArrayList<>();
    }

    private void saveOrders(List<Order> orders) {
        sharedPreferences.edit()
                .putString(getOrderKey(), gson.toJson(orders))
                .apply();
    }
}
