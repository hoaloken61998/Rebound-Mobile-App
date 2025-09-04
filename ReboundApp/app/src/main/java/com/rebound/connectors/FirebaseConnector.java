package com.rebound.connectors;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rebound.callback.FirebaseListCallback;
import com.rebound.callback.FirebaseSingleCallback;

import java.util.ArrayList;

/**
 * A general-purpose utility class to interact with the Firebase Realtime Database.
 * It uses generics to work with any POJO (Plain Old Java Object).
 */
public final class FirebaseConnector {

    // Private constructor to prevent instantiation of this utility class.
    private FirebaseConnector() {}

    /**
     * Fetches a single item from a specified node by its ID.
     *
     * @param node The database node/table name (e.g., "User", "Product").
     * @param itemId The unique ID of the item to fetch.
     * @param clazz The Class of the POJO to map the data to (e.g., User.class).
     * @param callback The callback to be invoked with the result.
     * @param <T> The generic type of the POJO.
     */
    public static <T> void getSingleItemById(@NonNull String node, @NonNull String itemId, @NonNull final Class<T> clazz, @NonNull final FirebaseSingleCallback<T> callback) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(node).child(itemId);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    try {
                        T item = dataSnapshot.getValue(clazz);
                        if (item != null) {
                            callback.onSuccess(item);
                        } else {
                            callback.onFailure("Failed to deserialize the object.");
                        }
                    } catch (Exception e) {
                        // Show error and snapshot as Toast for debugging (run on UI thread)
                        android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
                        handler.post(() -> {
                            Toast.makeText(
                                com.rebound.ReboundApplication.getInstance().getApplicationContext(),
                                "Firebase mapping error: " + e.getMessage() + "\nSnapshot: " + dataSnapshot.getValue(),
                                Toast.LENGTH_LONG
                            ).show();
                        });
                        callback.onFailure("Mapping error: " + e.getMessage());
                    }
                } else {
                    callback.onFailure("Item with ID " + itemId + " not found in node " + node + ".");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Show error as Toast for debugging (run on UI thread)
                android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
                handler.post(() -> {
                    Toast.makeText(
                        com.rebound.ReboundApplication.getInstance().getApplicationContext(),
                        "Firebase cancelled: " + databaseError.getMessage(),
                        Toast.LENGTH_LONG
                    ).show();
                });
                callback.onFailure(databaseError.getMessage());
            }
        });
    }

    /**
     * Fetches all items from a specified node.
     *
     * @param node The database node/table name (e.g., "User", "Product").
     * @param clazz The Class of the POJO to map the data to (e.g., User.class).
     * @param callback The callback to be invoked with the result.
     * @param <T> The generic type of the POJO.
     */
    public static <T> void getAllItems(@NonNull String node, @NonNull final Class<T> clazz, @NonNull final FirebaseListCallback<T> callback) {
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
                // Return the list, which will be empty if the node doesn't exist or has no children.
                callback.onSuccess(itemList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onFailure(databaseError.getMessage());
            }
        });
    }


}