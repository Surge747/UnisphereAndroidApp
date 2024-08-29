package com.example.mackenz;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            int id = item.getItemId();
            if (id == R.id.Settings) {
                selectedFragment = new SettingsFragment();
            } else if (id == R.id.Forum) {
                selectedFragment = new ForumFragment();
            } else if (id == R.id.New_Post) {
                selectedFragment = new New_PostFragment();
            } else if (id == R.id.Message) {
                selectedFragment = new MessagesFragment();
            } else if (id == R.id.Profile) {
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
                return true;
            }

            return false;
        });

        // Set initial fragment on creation
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ForumFragment()).commit();
        }
    }
}
