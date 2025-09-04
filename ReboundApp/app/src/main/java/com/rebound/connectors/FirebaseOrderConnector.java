package com.rebound.connectors;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rebound.callback.OrderFetchCallback;
import com.rebound.main.OrdersActivity;
import com.rebound.models.Orders.Order;
import com.rebound.models.Orders.OrderItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseOrderConnector {
    private static final String TAG = "FirebaseOrderConnector";
    private static final String PREF_NAME = "user_prefs";
    private static final String USER_ID_KEY = "user_id";


    public static void getOrdersForLoggedInUser(Context context, OrderFetchCallback orderFetchCallback) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        long userId = prefs.getLong(USER_ID_KEY, -1);
        Log.d(TAG, "getOrdersForLoggedInUser: USER_ID_KEY='" + USER_ID_KEY + "', userId=" + userId);
        if (userId == -1) {
            orderFetchCallback.onOrdersFetched(new ArrayList<>());
            return;
        }
        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("Order");
        // Query with long value for UserID
        ordersRef.orderByChild("UserID").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Order> userOrders = new ArrayList<>();
                        for (DataSnapshot orderSnap : snapshot.getChildren()) {
                            Order order = orderSnap.getValue(Order.class);
                            if (order != null) {
                                userOrders.add(order);
                                Log.d(TAG, "Fetched order with UserID: " + order.getUserID() + ", Status: " + order.getStatus());
                            }
                        }
                        orderFetchCallback.onOrdersFetched(userOrders);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        orderFetchCallback.onOrdersFetched(new ArrayList<>());
                    }
                });
    }

    public static void deleteOrderById(String orderId, final Runnable onSuccess, final Runnable onFailure) {
        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("Order");
        ordersRef.orderByChild("OrderID").equalTo(Long.valueOf(orderId))
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    boolean found = false;
                    for (DataSnapshot orderSnap : snapshot.getChildren()) {
                        orderSnap.getRef().removeValue();
                        found = true;
                    }
                    if (found && onSuccess != null) onSuccess.run();
                    else if (!found && onFailure != null) onFailure.run();
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    if (onFailure != null) onFailure.run();
                }
            });
    }



    public static void addOrder(Order order, String orderKey, final Runnable onSuccess, final Runnable onFailure) {
        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("Order");
        ordersRef.child(orderKey).setValue(order)
            .addOnSuccessListener(aVoid -> {
                if (onSuccess != null) onSuccess.run();
            })
            .addOnFailureListener(e -> {
                if (onFailure != null) onFailure.run();
            });
    }

    // Add an OrderItem to the 'OrderItem' node in Firebase
    public static void addOrderItem(OrderItem orderItem, final Runnable onSuccess, final Runnable onFailure) {
        DatabaseReference orderItemsRef = FirebaseDatabase.getInstance().getReference("OrderItem");
        // Find the max numeric key
        orderItemsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long maxKey = 0;
                for (DataSnapshot itemSnap : snapshot.getChildren()) {
                    try {
                        long key = Long.parseLong(itemSnap.getKey());
                        if (key > maxKey) maxKey = key;
                    } catch (Exception ignore) {}
                }
                long nextKey = maxKey + 1;
                orderItemsRef.child(String.valueOf(nextKey)).setValue(orderItem)
                    .addOnSuccessListener(aVoid -> {
                        if (onSuccess != null) onSuccess.run();
                    })
                    .addOnFailureListener(e -> {
                        if (onFailure != null) onFailure.run();
                    });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (onFailure != null) onFailure.run();
            }
        });
    }
    public static void updateOrderStatus(String orderId, String newStatus, Runnable onSuccess, Runnable onFailure) {
        Log.d("FirebaseOrderConnector", "🔄 Updating OrderID=" + orderId + " to Status=" + newStatus);

        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("Order");

        ordersRef.orderByChild("OrderID").equalTo(Long.valueOf(orderId)) // vì OrderID là số
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            Log.e("FirebaseOrderConnector", "OrderID not found in Firebase");
                            if (onFailure != null) onFailure.run();
                            return;
                        }

                        for (DataSnapshot orderSnap : snapshot.getChildren()) {
                            // ✅ Sửa đúng key là "Status" (chữ hoa)
                            orderSnap.getRef().child("Status").setValue(newStatus)
                                    .addOnSuccessListener(unused -> {
                                        Log.d("FirebaseOrderConnector", "Order Status updated successfully");
                                        if (onSuccess != null) onSuccess.run();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("FirebaseOrderConnector", "Failed to update Status", e);
                                        if (onFailure != null) onFailure.run();
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("FirebaseOrderConnector", "Firebase query cancelled", error.toException());
                        if (onFailure != null) onFailure.run();
                    }
                });
    }
}
