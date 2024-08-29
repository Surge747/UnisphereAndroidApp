package com.example.mackenz;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProfileFragment extends Fragment {

    private Spinner spinnerUsers;
    private ImageView profileImageView;
    private TextView profileUsername;
    private TextView profileCourse1;
    private TextView profileCourse2;
    private TextView profileCourse3;
    private TextView profileCourse4;
    private Button profileMessage;
    private Button profileBlock;

    private DatabaseReference usersRef;
    private List<String> userList;
    private Map<String, User> userMap;

    private Set<String> adminSet;
    private String currentUsername;
    private boolean isCurrentUserAdmin;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        spinnerUsers = view.findViewById(R.id.spinnerUsers);
        profileImageView = view.findViewById(R.id.imageView);
        profileUsername = view.findViewById(R.id.Profile_Username);
        profileCourse1 = view.findViewById(R.id.Profile_Course1);
        profileCourse2 = view.findViewById(R.id.Profile_Course2);
        profileCourse3 = view.findViewById(R.id.Profile_Course3);
        profileCourse4 = view.findViewById(R.id.Profile_Course4);
        profileMessage = view.findViewById(R.id.Profile_message);
        profileBlock = view.findViewById(R.id.Profile_block);

        userList = new ArrayList<>();
        userMap = new HashMap<>();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        currentUsername = sharedPreferences.getString("username", "Guest");

        loadAdmins();
        loadUsers();

        spinnerUsers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedUsername = userList.get(position);
                User selectedUser = userMap.get(selectedUsername);
                if (selectedUser != null) {
                    updateProfile(selectedUser);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        profileMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedUsername = (String) spinnerUsers.getSelectedItem();
                if (selectedUsername != null) {
                    if (isCurrentUserAdmin || !adminSet.contains(selectedUsername)) {
                        Intent intent = new Intent(getActivity(), ChatActivity.class);
                        intent.putExtra("username", selectedUsername);
                        startActivity(intent);
                    } else {
                        Toast.makeText(getContext(), "Can't message admins", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        profileBlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedUsername = (String) spinnerUsers.getSelectedItem();
                if (selectedUsername != null) {
                    if (isCurrentUserAdmin || !adminSet.contains(selectedUsername)) {
                        blockUser(selectedUsername);
                    } else {
                        Toast.makeText(getContext(), "Can't block admins", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        return view;
    }

    private void loadAdmins() {
        adminSet = new HashSet<>();
        isCurrentUserAdmin = false;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getContext().getAssets().open("Admin.txt")))) {
            String line;
            while ((line = reader.readLine()) != null) {
                adminSet.add(line.trim());
            }
            isCurrentUserAdmin = adminSet.contains(currentUsername);
        } catch (IOException e) {
            Log.e("ProfileFragment", "Error reading Admin.txt", e);
        }
    }

    private void loadUsers() {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                userMap.clear();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String username = userSnapshot.getKey();
                    User user = userSnapshot.getValue(User.class);
                    if (username != null && user != null && !username.equals(currentUsername)) {
                        userList.add(username);
                        userMap.put(username, user);
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, userList);
                spinnerUsers.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ProfileFragment", "Error loading users", error.toException());
            }
        });
    }

    private void updateProfile(User user) {
        profileUsername.setText(user.getUsername());
        List<String> courses = user.getCoursesTaken();
        profileCourse1.setText(courses.size() > 0 ? courses.get(0) : "");
        profileCourse2.setText(courses.size() > 1 ? courses.get(1) : "");
        profileCourse3.setText(courses.size() > 2 ? courses.get(2) : "");
        profileCourse4.setText(courses.size() > 3 ? courses.get(3) : "");

        // Load profile picture based on profilePicUrl value
        loadProfilePicture(user.getProfilePicUrl());
    }

    private void loadProfilePicture(String profilePicUrl) {
        int imageResource;
        switch (profilePicUrl) {
            case "0":
                imageResource = R.drawable.sharp_account_circle_24;
                break;
            case "1":
                imageResource = R.drawable.baseline_favorite_border_24;
                break;
            case "2":
                imageResource = R.drawable.baseline_near_me_24;
                break;
            case "3":
                imageResource = R.drawable.baseline_pest_control_rodent_24;
                break;
            case "4":
                imageResource = R.drawable.baseline_pets_24;
                break;
            default:
                imageResource = R.drawable.sharp_account_circle_24;
                break;
        }
        profileImageView.setImageResource(imageResource);
    }

    private void blockUser(String username) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Set<String> blockedUsers = sharedPreferences.getStringSet("blockedUsers", new HashSet<>());
        blockedUsers.add(username);
        editor.putStringSet("blockedUsers", blockedUsers);
        editor.apply();
        Toast.makeText(getContext(), "User blocked", Toast.LENGTH_SHORT).show();
    }
}
