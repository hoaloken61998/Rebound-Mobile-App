package com.rebound.connectors;

import android.content.Context;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rebound.callback.FirebaseSingleCallback;
import com.rebound.models.Customer.Customer;
import com.rebound.models.Customer.ListCustomer;
import com.rebound.utils.SharedPrefManager;

import java.util.ArrayList;

public class CustomerConnector {
    private ListCustomer listCustomer;

    public CustomerConnector(Context context) {
        listCustomer = SharedPrefManager.getCustomerList(context);

        if (listCustomer == null) {
            listCustomer = new ListCustomer();
            SharedPrefManager.saveCustomerList(context, listCustomer);
        }
    }

    public CustomerConnector() {
    }

    public ArrayList<Customer> get_all_customers() {
        return listCustomer.getCustomers();
    }

    public void loginWithFirebase(String email, String pwd, com.rebound.callback.FirebaseLoginCallback callback) {
        com.rebound.connectors.FirebaseConnector.getAllItems(
                "User",
                com.rebound.models.Customer.Customer.class,
                new com.rebound.callback.FirebaseListCallback<com.rebound.models.Customer.Customer>() {
                    @Override
                    public void onSuccess(java.util.ArrayList<com.rebound.models.Customer.Customer> customers) {
                        for (com.rebound.models.Customer.Customer c : customers) {
                            if (c != null && c.getEmail() != null &&
                                    (c.getEmail().equalsIgnoreCase(email)) &&
                                    (String.valueOf(c.getPassword()).equals(pwd) || String.valueOf(c.getPassword()).equalsIgnoreCase(pwd))) {
                                callback.onSuccess(c);
                                return;
                            }
                        }
                        callback.onFailure("Invalid email or password.");
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        callback.onFailure(errorMessage);
                    }
                }
        );
    }

    public void addCustomerToFirebaseWithId(long userId, Customer customer, com.rebound.callback.FirebaseSingleCallback<Void> callback) {
        FirebaseDatabase.getInstance()
                .getReference("User")
                .child(String.valueOf(userId))
                .setValue(customer)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage() != null ? e.getMessage() : "Unknown error when adding user"));
    }

    public void updateCustomerInFirebase(Customer updatedCustomer, FirebaseSingleCallback<Void> callback) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("User");

        final String userIdStr;
        try {
            Object idObj = updatedCustomer.getUserID();
            if (idObj == null) {
                callback.onFailure("UserID is null");
                return;
            }

            if (idObj instanceof Number) {
                userIdStr = String.valueOf(((Number) idObj).longValue());
            } else {
                userIdStr = idObj.toString().replace(".0", "");
            }
        } catch (Exception e) {
            callback.onFailure("Invalid userID format");
            return;
        }

        Log.d("CustomerUpdate", "Looking for UserID to update: " + userIdStr);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean found = false;
                for (DataSnapshot userSnap : dataSnapshot.getChildren()) {
                    Object userIdObj = userSnap.child("UserID").getValue();
                    String firebaseUserId = userIdObj != null ? String.valueOf(userIdObj).replace(".0", "") : null;
                    Log.d("CustomerUpdate", "Found Firebase UserID: " + firebaseUserId);

                    if (firebaseUserId != null && firebaseUserId.equals(userIdStr)) {
                        userSnap.getRef().setValue(updatedCustomer)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("CustomerUpdate", "Updated user with ID: " + userIdStr);
                                    callback.onSuccess(null);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("CustomerUpdate", "Failed to update: " + e.getMessage());
                                    callback.onFailure(e.getMessage() != null ? e.getMessage() : "Unknown Firebase error");
                                });
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    Log.e("CustomerUpdate", "No matching user found for ID: " + userIdStr);
                    callback.onFailure("No matching user found to update.");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onFailure(databaseError.getMessage());
            }
        });
    }
}
