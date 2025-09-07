package com.rebound.callback;

// A generic callback for fetching a single object from Firebase.
public interface FirebaseSingleCallback<T> {
    void onSuccess(T result);
    void onFailure(String errorMessage);
}