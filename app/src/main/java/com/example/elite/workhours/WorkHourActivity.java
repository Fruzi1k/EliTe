package com.example.elite.workhours;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.elite.R;
import com.example.elite.auth.Login;
import com.example.elite.profile.Profile;
import com.example.elite.work.WorkerActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class WorkHourActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    FirebaseAuth auth;
    FirebaseUser user;
    TextView textView;
    MaterialButton buttonClockIn, buttonClockOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_hour);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        textView = findViewById(R.id.textview_work_hour_user);
        buttonClockIn = findViewById(R.id.button_clock_in);
        buttonClockOut = findViewById(R.id.button_clock_out);

        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        } else {
            textView.setText("Work Hours for: " + user.getEmail());
        }

        // Clock In/Out button listeners
        buttonClockIn.setOnClickListener(v -> {
            Toast.makeText(this, "Clocked In!", Toast.LENGTH_SHORT).show();
            // Here you would implement actual clock-in logic
        });

        buttonClockOut.setOnClickListener(v -> {
            Toast.makeText(this, "Clocked Out!", Toast.LENGTH_SHORT).show();
            // Here you would implement actual clock-out logic
        });

        // Initialize Bottom Navigation
        bottomNavigationView = findViewById(R.id.bottom_navigation_work_hour);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, Profile.class));
                return true;
            } else if (itemId == R.id.nav_work) {
                // Navigate to work section based on user role
                navigateToWorkSection();
                return true;
            } else if (itemId == R.id.nav_work_hour) {
                // Already on work hour page
                return true;
            }
            return false;
        });

        // Set default selection
        bottomNavigationView.setSelectedItemId(R.id.nav_work_hour);
    }

    private void navigateToWorkSection() {
        // For now, let's assume we navigate to WorkerActivity
        // In a real app, you would check user role and navigate accordingly
        startActivity(new Intent(this, WorkerActivity.class));
        finish();
    }
}