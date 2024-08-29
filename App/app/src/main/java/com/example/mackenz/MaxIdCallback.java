package com.example.mackenz;

public interface MaxIdCallback {
    void onMaxIdFetched(int maxId);
    void onError(String error);
}