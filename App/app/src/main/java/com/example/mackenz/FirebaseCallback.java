package com.example.mackenz;

public interface FirebaseCallback {
    void onSuccess();
    void onFailure();
    void onError(Exception exception);
}

