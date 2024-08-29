package com.example.mackenz;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class signup extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private Button signupButton;
    private FirebaseConnector firebaseConnector;
    private List<String> userCoursesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.signup_password);
        confirmPasswordEditText = findViewById(R.id.confirm_password);
        signupButton = findViewById(R.id.button);

        firebaseConnector = new FirebaseConnector(this);

        // Load courses from Usercourses.txt
        userCoursesList = loadUserCourses();

        signupButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String confirmPassword = confirmPasswordEditText.getText().toString().trim();

            if (validateInputs(username, password, confirmPassword)) {
                checkIfUserExists(username, password);
            }
        });
    }

    private boolean validateInputs(String username, String password, String confirmPassword) {
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (username.contains(" ") || username.contains("_")) {
            Toast.makeText(this, "Username cannot contain spaces or underscores", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void checkIfUserExists(String username, String password) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(username);
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Toast.makeText(signup.this, "Username already exists", Toast.LENGTH_SHORT).show();
                    clearFields();
                } else {
                    createNewUser(username, password);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("signup", "Error checking username", error.toException());
            }
        });
    }

    private void clearFields() {
        usernameEditText.setText("");
        passwordEditText.setText("");
        confirmPasswordEditText.setText("");
    }

    private void createNewUser(String username, String password) {
        Random random = new Random();
        String profilePicUrl = String.valueOf(random.nextInt(5));

        // Get random courses
        List<String> selectedCourses = getRandomCourses();

        firebaseConnector.createNewUser(username, password, profilePicUrl, new CreateUserCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(signup.this, "Signup successful", Toast.LENGTH_SHORT).show();
                // Save user courses
                firebaseConnector.updateCourses(username, selectedCourses.toArray(new String[0]), new UpdateCoursesCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(signup.this, "Courses added", Toast.LENGTH_SHORT).show();
                        // Redirect to login
                        Intent intent = new Intent(signup.this, Login.class);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onFailure(Exception exception) {
                        Toast.makeText(signup.this, "Failed to add courses: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(Exception exception) {
                Toast.makeText(signup.this, "Signup failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<String> loadUserCourses() {
        List<String> courses = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open("Usercourses.txt")))) {
            String line;
            while ((line = reader.readLine()) != null) {
                courses.add(line.trim());
            }
        } catch (IOException e) {
            Log.e("signup", "Error reading Usercourses.txt", e);
        }
        return courses;
    }

    private List<String> getRandomCourses() {
        List<String> selectedCourses = new ArrayList<>();
        if (userCoursesList.size() <= 4) {
            selectedCourses.addAll(userCoursesList);
        } else {
            Collections.shuffle(userCoursesList);
            for (int i = 0; i < 4; i++) {
                selectedCourses.add(userCoursesList.get(i));
            }
        }
        return selectedCourses;
    }
}
