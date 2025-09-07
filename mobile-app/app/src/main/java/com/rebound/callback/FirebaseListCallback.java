package com.rebound.callback;

import java.util.ArrayList;

// A generic callback for fetching a list of objects from Firebase.
public interface FirebaseListCallback<T> {
    void onSuccess(ArrayList<T> result);
    void onFailure(String errorMessage);
}