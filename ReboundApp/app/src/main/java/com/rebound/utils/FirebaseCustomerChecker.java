package com.rebound.utils;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.rebound.models.Customer.Customer;
import com.rebound.callback.FirebaseListCallback;

import java.util.ArrayList;

public class FirebaseCustomerChecker {

    public interface TakenCallback {
        void onResult(boolean isTaken);
        void onError(String error);
    }

    // Check if a username is taken (case-sensitive with Firebase key "Username")
    public static void isUsernameTaken(String username, TakenCallback callback) {
        FirebaseDatabase.getInstance()
                .getReference("User")
                .orderByChild("Username")
                .equalTo(username)
                .limitToFirst(1)
                .get()
                .addOnSuccessListener(snapshot -> callback.onResult(snapshot.exists()))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // Check if an email is taken (case-sensitive with Firebase key "Email")
    public static void isEmailTaken(String email, TakenCallback callback) {
        FirebaseDatabase.getInstance()
                .getReference("User")
                .orderByChild("Email")
                .equalTo(email)
                .limitToFirst(1)
                .get()
                .addOnSuccessListener(snapshot -> callback.onResult(snapshot.exists()))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // Check if a phone number is taken (manually search all users)
    public static void isPhoneTaken(String phone, String exceptEmail, TakenCallback callback) {
        getAllCustomersFromFirebase(new FirebaseListCallback<Customer>() {
            @Override
            public void onSuccess(ArrayList<Customer> customers) {
                for (Customer c : customers) {
                    if (String.valueOf(c.getPhoneNumber()).equals(phone)) {
                        if (exceptEmail == null || (c.getEmail() != null && !c.getEmail().equalsIgnoreCase(exceptEmail))) {
                            callback.onResult(true);
                            return;
                        }
                    }
                }
                callback.onResult(false);
            }

            @Override
            public void onFailure(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }

    // Get all customers from Firebase and pass into callback
    public static void getAllCustomersFromFirebase(FirebaseListCallback<Customer> callback) {
        FirebaseDatabase.getInstance()
                .getReference("User")
                .get()
                .addOnSuccessListener(dataSnapshot -> {
                    ArrayList<Customer> customers = new ArrayList<>();
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        Customer customer = userSnapshot.getValue(Customer.class);
                        if (customer != null) {
                            customers.add(customer);
                        }
                    }
                    callback.onSuccess(customers);
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e.getMessage() != null ? e.getMessage() : "Unknown error when fetching customers.");
                });
    }
}
