package com.example.mackenz;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MessagesFragment extends Fragment {
    private RecyclerView usersRecyclerView;
    private UserAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_messages, container, false);
        usersRecyclerView = view.findViewById(R.id.usersRecyclerView);
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        String currentUser = getCurrentUsername();
        FirebaseConnector firebaseConnector = new FirebaseConnector(getContext());
        firebaseConnector.fetchAllMessagesForCurrentUser(currentUser, users -> {
            if (adapter == null) {
                adapter = new UserAdapter(getContext(), users, this::navigateToChat);
                usersRecyclerView.setAdapter(adapter);
            } else {
                adapter.updateUsers(users);
            }
        });

        return view;
    }

    private String getCurrentUsername() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.getString("username", "");
    }

    private void navigateToChat(String username) {
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        intent.putExtra("username", username);
        startActivity(intent);
    }
}
