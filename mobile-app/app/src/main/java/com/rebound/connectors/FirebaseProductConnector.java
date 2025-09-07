package com.rebound.connectors;

import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rebound.callback.FirebaseListCallback;
import com.rebound.callback.FirebaseSingleCallback;

import java.util.ArrayList;

public class FirebaseProductConnector {

    /**
     * Fetches products from a specified node filtered by categoryId field.
     * @param node The database node/table name (e.g., "Product").
     * @param categoryId The category ID to filter by (e.g., "1", "3").
     * @param clazz The Class of the POJO to map the data to (e.g., ProductItem.class).
     * @param callback The callback to be invoked with the result.
     * @param <T> The generic type of the POJO.
     */

    /**
     * Fetches products from a specified node filtered by numeric categoryId field.
     * @param node The database node/table name (e.g., "Product").
     * @param categoryId The numeric category ID to filter by (e.g., 1, 3).
     * @param clazz The Class of the POJO to map the data to (e.g., ProductItem.class).
     * @param callback The callback to be invoked with the result.
     * @param <T> The generic type of the POJO.
     */
    public static <T> void getProductsByCategoryNumber(@NonNull String node, long categoryId, @NonNull final Class<T> clazz, @NonNull final FirebaseListCallback<T> callback) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(node);
        databaseReference.orderByChild("CategoryID").equalTo(categoryId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        ArrayList<T> itemList = new ArrayList<>();
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                T item = snapshot.getValue(clazz);
                                if (item != null) {
                                    itemList.add(item);
                                }
                            }
                        }
                        callback.onSuccess(itemList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        callback.onFailure(databaseError.getMessage());
                    }
                });
    }

    /**
     * Fetches all products from a specified node (e.g., "Product").
     * @param node The database node/table name.
     * @param clazz The Class of the POJO to map the data to (e.g., ProductItem.class).
     * @param callback The callback to be invoked with the result.
     * @param <T> The generic type of the POJO.
     */
    public static <T> void getAllProducts(@NonNull String node, @NonNull final Class<T> clazz, @NonNull final FirebaseListCallback<T> callback) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(node);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<T> itemList = new ArrayList<>();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        T item = snapshot.getValue(clazz);
                        if (item != null) {
                            itemList.add(item);
                        }
                    }
                }
                callback.onSuccess(itemList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onFailure(databaseError.getMessage());
            }
        });
    }

    /**
     * Fetches the count of sold products by ProductID.
     * @param productId The product ID to filter by.
     * @param callback The callback to be invoked with the sold count result.
     */
    public static void getSoldCountByProductId(Object productId, FirebaseSingleCallback<Integer> callback) {
        FirebaseDatabase.getInstance().getReference("OrderItem")
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    int soldCount = 0;
                    Object productIdStr = productId != null ? productId.toString() : null;
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Object itemProductId = snapshot.child("ProductID").getValue();
                        if (itemProductId != null && productIdStr != null && itemProductId.toString().equals(productIdStr)) {
                            Object orderId = snapshot.child("OrderID").getValue();
                            if (orderId != null) {
                                soldCount++;
                            }
                        }
                    }
                    callback.onSuccess(soldCount);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    callback.onFailure(error.getMessage());
                }
            });
    }
}
