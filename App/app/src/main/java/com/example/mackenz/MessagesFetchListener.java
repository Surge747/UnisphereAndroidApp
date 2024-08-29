package com.example.mackenz;

import java.util.List;

public interface MessagesFetchListener {
    void onMessagesFetched(List<String> userNames);
}
