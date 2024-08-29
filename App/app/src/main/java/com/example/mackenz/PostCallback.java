package com.example.mackenz;

public interface PostCallback {
    void onPostAdded();
    void onPostFailed(String errorMessage);
}
