package com.example.mackenz;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.CheckBox;
import android.widget.Toast;
import android.content.Intent;

import com.example.mackenz.LoginCallback;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.IOException;

public class Login extends AppCompatActivity {
    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private CheckBox rememberMeCheckBox;
    private FirebaseConnector firebaseConnector;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize SharedPreferences to store and retrieve user preferences
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        // Linking UI elements from XML to Java objects
        usernameEditText = findViewById(R.id.login_username);
        passwordEditText = findViewById(R.id.login_password);
        loginButton = findViewById(R.id.login_login_button);
        rememberMeCheckBox = findViewById(R.id.Login_Checkbox);

        // Initializing connection to Firebase for authentication
        firebaseConnector = new FirebaseConnector(this);

        // Check if credentials are saved and auto-login if possible
        checkCredentialsAndAutoLogin();

        // Set up login button click listener
        loginButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            attemptLogin(username, password);
        });

        // Setup and handle navigation to the signup activity
        Button signupButton = findViewById(R.id.login_signup_button);
        signupButton.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, signup.class);
            startActivity(intent);
        });
    }

    private void checkCredentialsAndAutoLogin() {
        // Retrieve stored login credentials and check if "remember me" is checked
        String username = sharedPreferences.getString("username", null);
        String password = sharedPreferences.getString("password", null);
        boolean rememberMe = sharedPreferences.getBoolean("rememberMe", false);

        // Auto-fill and login if "remember me" was selected
        if (rememberMe && username != null && password != null) {
            usernameEditText.setText(username);
            passwordEditText.setText(password);
            rememberMeCheckBox.setChecked(true);
            loginWithFirebase(username, password);
        }
    }

    private void attemptLogin(String username, String password) {
        // Check for empty fields before attempting login
        if (!username.isEmpty() && !password.isEmpty()) {
            saveCredentials(username, password, rememberMeCheckBox.isChecked()); // Optionally save credentials based on checkbox
            loginWithFirebase(username, password);
        } else {
            Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
        }
    }

    private void loginWithFirebase(String username, String password) {
        // Use FirebaseConnector to attempt user login and handle callbacks
        firebaseConnector.loginUser(username, password, new LoginCallback() {
            @Override
            public void onSuccess() {
                // Inform user of success and navigate to MainActivity
                if(rememberMeCheckBox.isChecked()){
                    Toast.makeText(Login.this, "Credentials saved", Toast.LENGTH_SHORT).show();
                }
                Toast.makeText(Login.this, "Login successful", Toast.LENGTH_SHORT).show();
                saveCredentials(username, password, rememberMeCheckBox.isChecked());
                Intent intent = new Intent(Login.this, MainActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure() {
                // Inform user of failure
                Toast.makeText(Login.this, "Login failed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Exception exception) {
                // Handle exceptions during login process
                Toast.makeText(Login.this, "Error: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveCredentials(String username, String password, boolean rememberMe) {
        // Save or update user credentials in SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("username", username);
        editor.putString("password", password);
        editor.putBoolean("rememberMe", rememberMe);
        editor.apply();  // Use apply() to save asynchronously
    }

}
