package com.example.elite;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HoursFragmentDirector extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hours_director);

        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        bottomNavigation = findViewById(R.id.bottom_navigation);
        
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, Profile.class));
                return true;
            } else if (itemId == R.id.nav_work) {
                startActivity(new Intent(this, DirectorActivity.class));
                return true;
            } else if (itemId == R.id.nav_work_hour) {
                // Already on work hours
                return true;
            }
            return false;
        });

        // Set work hour as selected
        bottomNavigation.setSelectedItemId(R.id.nav_work_hour);
    }
}