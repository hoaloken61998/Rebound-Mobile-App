package com.rebound.callback;

import com.rebound.models.Customer.Customer;

public interface FirebaseLoginCallback {
    void onSuccess(Customer customer);
    void onFailure(String errorMessage);
}
