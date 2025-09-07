package com.rebound.utils;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.rebound.models.Main.NotificationItem;
import com.rebound.callback.FirebaseListCallback;

import java.util.ArrayList;

public class FirebaseNotificationFetcher {

    public static void getNotificationsByUserID(Double userID, FirebaseListCallback<NotificationItem> callback) {
        if (userID == null) {
            callback.onFailure("UserID is null");
            return;
        }

        FirebaseDatabase.getInstance()
                .getReference("Notification")
                .orderByChild("UserID")
                .equalTo(userID)
                .get()
                .addOnSuccessListener(dataSnapshot -> {
                    ArrayList<NotificationItem> result = new ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        NotificationItem item = snapshot.getValue(NotificationItem.class);
                        if (item != null) {
                            result.add(item);
                        }
                    }
                    callback.onSuccess(result);
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e.getMessage() != null ? e.getMessage() : "Failed to fetch notifications.");
                });
    }
}
