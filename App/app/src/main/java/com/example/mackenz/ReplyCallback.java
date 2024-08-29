package com.example.mackenz;

interface ReplyCallback {
    void onReplyAdded();

    void onFailure(Exception exception);
}