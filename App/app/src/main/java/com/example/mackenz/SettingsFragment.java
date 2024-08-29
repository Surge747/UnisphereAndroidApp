package com.example.mackenz;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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

import java.util.List;

public class SettingsFragment extends Fragment {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button signOutButton;
    private Button changePasswordButton;
    private SharedPreferences sharedPreferences;
    private TextView course1TextView;
    private TextView course2TextView;
    private TextView course3TextView;
    private TextView course4TextView;
    private DatabaseReference usersRef;
    private String currentUsername;

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        usernameEditText = view.findViewById(R.id.settings_username);
        passwordEditText = view.findViewById(R.id.Settings_password);
        signOutButton = view.findViewById(R.id.settings_sign_out);
        changePasswordButton = view.findViewById(R.id.Settings_change);
        course1TextView = view.findViewById(R.id.course1);
        course2TextView = view.findViewById(R.id.course2);
        course3TextView = view.findViewById(R.id.course3);
        course4TextView = view.findViewById(R.id.course4);

        sharedPreferences = getActivity().getSharedPreferences("UserPrefs", getContext().MODE_PRIVATE);
        currentUsername = sharedPreferences.getString("username", "Guest");

        signOutButton.setOnClickListener(v -> signOut());
        changePasswordButton.setOnClickListener(v -> changePassword());

        // Load the user's courses from Firebase
        loadUserCourses();

        return view;
    }

    private void loadUserCourses() {
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUsername);
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        List<String> courses = user.getCoursesTaken();
                        course1TextView.setText(courses.size() > 0 ? courses.get(0) : "");
                        course2TextView.setText(courses.size() > 1 ? courses.get(1) : "");
                        course3TextView.setText(courses.size() > 2 ? courses.get(2) : "");
                        course4TextView.setText(courses.size() > 3 ? courses.get(3) : "");
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to load courses", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error loading courses: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void signOut() {
        // Clear user credentials from SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("username");
        editor.remove("password");
        editor.remove("rememberMe");
        editor.apply();

        // Show a confirmation message
        Toast.makeText(getActivity(), "Signed out successfully", Toast.LENGTH_SHORT).show();

        // Navigate back to LoginActivity
        Intent intent = new Intent(getActivity(), Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear the activity stack
        startActivity(intent);
    }

    private void changePassword() {
        String username = usernameEditText.getText().toString();
        String newPassword = passwordEditText.getText().toString();

        if (!newPassword.isEmpty()) {
            FirebaseConnector firebaseConnector = new FirebaseConnector(getContext());
            firebaseConnector.updateUserPassword(username, newPassword, new FirebaseCallback() {
                @Override
                public void onSuccess() {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("password", newPassword);  // Save new password
                    editor.apply();

                    Toast.makeText(getActivity(), "Password updated successfully", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure() {
                    Toast.makeText(getActivity(), "Failed to update password", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(Exception exception) {
                    Toast.makeText(getActivity(), "Error: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(getActivity(), "Please enter a new password", Toast.LENGTH_SHORT).show();
        }
    }
}
