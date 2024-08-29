package com.example.mackenz;

import java.util.List;

public interface MessageLoadListener {
    void onMessagesLoaded(List<Message> messages);
}