package com.example.mackenz;

public interface MaxPostIdCallback {
    void onMaxIdFetched(long maxId);
    void onError(Exception e);
}

