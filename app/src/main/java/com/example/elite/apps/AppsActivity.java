package com.example.elite.apps;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.elite.R;
import com.example.elite.apps.WorkerListActivity;
import com.example.elite.models.User;
import com.example.elite.profile.Profile;
import com.example.elite.work.DirectorActivity;
import com.example.elite.work.WorkerActivity;
import com.example.elite.workhours.HoursFragmentDirector;
import com.example.elite.workhours.HoursFragmentWorker;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;




public class AppsActivity extends AppCompatActivity {
    private User currentUser;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseFirestore db;
    private static final String TAG = "AppsActivity";

    // UI Elements
    private BottomNavigationView bottomNavigation;
    private MaterialCardView cardWorkers;
    private MaterialCardView cardFacture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apps);

        initializeFirebase();
        initializeUI();
        loadUserData();
        setupClickListeners();
        setupBottomNavigation();
    }

    private void initializeFirebase() {
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
    }

    private void loadUserData() {
        if (user == null) {
            Log.e(TAG, "User not authenticated");
            finish();
            return;
        }

        String userId = user.getUid();
        Log.d(TAG, "Loading user data for ID: " + userId);
        
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    currentUser = documentSnapshot.toObject(User.class);
                    Log.d(TAG, "User data loaded successfully. Position: " + 
                        (currentUser != null ? currentUser.getPosition() : "null"));
                    setupButtonVisibility();
                } else {
                    Log.e(TAG, "User document does not exist");
                    Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show();
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error loading user data", e);
                Toast.makeText(this, "Error loading user data", Toast.LENGTH_SHORT).show();
            });
    }

    private void setupButtonVisibility() {
        if (currentUser != null) {
            // Workers button видна только для директора
            if (currentUser.isDirector()) {
                cardWorkers.setVisibility(View.VISIBLE);
                Log.d(TAG, "Director role - showing workers button");
            } else {
                cardWorkers.setVisibility(View.GONE);
                Log.d(TAG, "Worker role - hiding workers button");
            }
            
            // Facture button видна для всех
            cardFacture.setVisibility(View.VISIBLE);
        }
    }

    private void setupClickListeners() {
        cardWorkers.setOnClickListener(v -> {
            Toast.makeText(this, "Button clicked!", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Workers button clicked");
            Log.d(TAG, "Current user: " + (currentUser != null ? currentUser.getEmail() : "null"));
            Log.d(TAG, "Is director: " + (currentUser != null ? currentUser.isDirector() : "N/A"));
            
            if (currentUser == null) {
                Log.w(TAG, "User data not loaded yet");
                Toast.makeText(this, "Loading user data, please wait...", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (currentUser.isDirector()) {
                Log.d(TAG, "Opening Worker List Activity");
                Toast.makeText(this, "Opening Worker List...", Toast.LENGTH_SHORT).show();
                // Открыть список работников
                try {
                    Intent intent = new Intent(this, WorkerListActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Error starting WorkerListActivity", e);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            } else {
                Log.w(TAG, "User is not a director");
                Toast.makeText(this, "Access denied. Directors only.", Toast.LENGTH_SHORT).show();
            }
        });

        cardFacture.setOnClickListener(v -> {
            // TODO: Открыть активность управления счетами
            Toast.makeText(this, "Opening Facture Management", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, Profile.class));
                return true;
            } else if (itemId == R.id.nav_work) {
                navigateToWork();
                return true;
            } else if (itemId == R.id.nav_work_hour) {
                navigateToWorkHours();
                return true;
            } else if (itemId == R.id.nav_apps) {
                // Already on profile
                return true;
            }
            return false;
        });

        // Set apps as selected
        bottomNavigation.setSelectedItemId(R.id.nav_apps);
    }

    private void navigateToWorkHours() {
        if (currentUser == null) {
            Toast.makeText(this, "Loading user data, please wait...", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent;
        if (currentUser.isDirector()) {
            intent = new Intent(this, HoursFragmentDirector.class);
        } else {
            intent = new Intent(this, HoursFragmentWorker.class);
        }
        startActivity(intent);
    }

    private void navigateToWork() {

            if (currentUser == null) {
                // Если данные пользователя еще не загружены, показываем сообщение и возвращаемся
                Toast.makeText(this, "Loading user data, please wait...", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent;
            if (currentUser.isDirector()) {
                intent = new Intent(this, DirectorActivity.class);
            } else {
                intent = new Intent(this, WorkerActivity.class);
            }
            startActivity(intent);

    }

    private void initializeUI() {
        bottomNavigation = findViewById(R.id.bottom_navigation);
        cardWorkers = findViewById(R.id.card_workers);
        cardFacture = findViewById(R.id.card_facture);
    }
}
