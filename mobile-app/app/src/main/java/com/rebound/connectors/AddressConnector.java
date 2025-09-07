package com.rebound.connectors;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rebound.callback.AddressCallback;
import com.rebound.models.Cart.Address;

public class AddressConnector {
    public void getDefaultAddressForUser(Context context, Long userId, AddressCallback callback) {
        DatabaseReference addressRef = FirebaseDatabase.getInstance().getReference("Address");
        addressRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Address found = null;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Use PascalCase 'UserID' to match the database
                    Object userIdObj = snapshot.child("UserID").getValue();
                    boolean userIdMatches = false;
                    if (userIdObj != null) {
                        try {
                            Long userIdLong = Long.parseLong(String.valueOf(userIdObj));
                            if (userIdLong.equals(userId)) {
                                userIdMatches = true;
                            }
                        } catch (Exception ignored) {}
                    }

                    if (userIdMatches) {
                        Address address = snapshot.getValue(Address.class);
                        if (address != null && "true".equalsIgnoreCase(address.getIsDefault())) {
                            found = address;
                            break;
                        }
                        if (address != null && found == null) {
                            found = address;
                        }
                    }
                }
                if (found != null) {
                    callback.onAddressLoaded(found);
                } else {
                    callback.onError("No address found for user");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError(databaseError.getMessage());
            }
        });
    }

    private void addNewAddress(DatabaseReference addressRef, Address address, AddressCallback callback) {
        addressRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long maxKey = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        long currentKey = Long.parseLong(snapshot.getKey());
                        if (currentKey > maxKey) {
                            maxKey = currentKey;
                        }
                    } catch (NumberFormatException e) {
                        Log.w("AddressConnector", "Found non-numeric key, ignoring: " + snapshot.getKey());
                    }
                }

                long newKeyAndId = maxKey + 1;
                address.setAddressID(newKeyAndId);

                addressRef.child(String.valueOf(newKeyAndId)).setValue(address)
                        .addOnSuccessListener(aVoid -> {
                            if (callback != null) callback.onAddressLoaded(address);
                        })
                        .addOnFailureListener(e -> {
                            if (callback != null) callback.onError(e.getMessage());
                        });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (callback != null) callback.onError(databaseError.getMessage());
            }
        });
    }

    public void updateAddress(Address address, AddressCallback callback) {
        if (address == null) {
            if (callback != null) callback.onError("Address is null");
            return;
        }

        DatabaseReference addressRef = FirebaseDatabase.getInstance().getReference("Address");

        if (address.getAddressID() == null) {
            Log.d("AddressConnector", "AddressID is null, adding new address: " + address);
            addNewAddress(addressRef, address, callback);
            return;
        }

        addressRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean updated = false;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Object idObj = snapshot.child("AddressID").getValue();
                    Log.d("AddressConnector", "Checking snapshot key: " + snapshot.getKey() + ", AddressID: " + idObj);
                    if (idObj != null && String.valueOf(idObj).equals(String.valueOf(address.getAddressID()))) {
                        Log.d("AddressConnector", "Updating address with AddressID: " + address.getAddressID() + ", Address fields: " + addressToString(address));
                        snapshot.getRef().setValue(address)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("AddressConnector", "Successfully updated address: " + address);
                                    if (callback != null) callback.onAddressLoaded(address);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("AddressConnector", "Failed to update address: " + e.getMessage());
                                    if (callback != null) callback.onError(e.getMessage());
                                });
                        updated = true;
                        break;
                    }
                }

                if (!updated) {
                    Log.w("AddressConnector", "AddressID " + address.getAddressID() + " not found for update, adding as a new address. Address object: " + address);
                    addNewAddress(addressRef, address, callback);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (callback != null) callback.onError(databaseError.getMessage());
            }
        });
    }

    private String addressToString(Address address) {
        if (address == null) return "null";
        return "{" +
                "AddressID=" + address.getAddressID() +
                ", UserID=" + address.getUserID() +
                ", Name='" + address.getReceiverName() + '\'' +
                ", Phone='" + address.getReceiverPhone() + '\'' +
                ", Street='" + address.getStreet() + '\'' +
                ", IsDefault='" + address.getIsDefault() + '\'' +
                '}';
    }
}