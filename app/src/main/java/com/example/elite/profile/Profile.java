package com.example.elite.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.elite.MainActivity;
import com.example.elite.R;
import com.example.elite.apps.AppsActivity;
import com.example.elite.models.User;
import com.example.elite.work.DirectorActivity;
import com.example.elite.work.WorkerActivity;
import com.example.elite.workhours.HoursFragmentDirector;
import com.example.elite.workhours.HoursFragmentWorker;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Profile extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private User currentUser;

    // UI Elements
    private CircularProgressIndicator progressIndicator;
    private MaterialCardView profileCard;
    private TextView textFullName;
    private TextView textEmail;
    private TextView textPhone;
    private TextView textPosition;
    private TextView textRole;
    private MaterialButton buttonLogout;
    private MaterialButton buttonEditProfile;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize UI
        initializeUI();
        setupBottomNavigation();
        loadUserProfile();
    }

    private void initializeUI() {
        progressIndicator = findViewById(R.id.progress_indicator);
        profileCard = findViewById(R.id.profile_card);
        textFullName = findViewById(R.id.text_full_name);
        textEmail = findViewById(R.id.text_email);
        textPhone = findViewById(R.id.text_phone);
        textPosition = findViewById(R.id.text_position);
        textRole = findViewById(R.id.text_role);
        buttonLogout = findViewById(R.id.button_logout);
        buttonEditProfile = findViewById(R.id.button_edit_profile);
        bottomNavigation = findViewById(R.id.bottom_navigation);

        buttonLogout.setOnClickListener(v -> logout());
        buttonEditProfile.setOnClickListener(v -> editProfile());
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_profile) {
                // Already on profile
                return true;
            } else if (itemId == R.id.nav_work) {
                navigateToWork();
                return true;
            } else if (itemId == R.id.nav_work_hour) {
                navigateToWorkHours();
                return true;
            } else if (itemId == R.id.nav_apps) {
                startActivity(new Intent(this, AppsActivity.class));
                return true;
            }
            return false;
        });

        // Set profile as selected
        bottomNavigation.setSelectedItemId(R.id.nav_profile);
    }

    private void loadUserProfile() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) {
            redirectToLogin();
            return;
        }

        showLoading(true);

        // Load user data from Firestore with real-time updates
        db.collection("users")
                .document(firebaseUser.getUid())
                .addSnapshotListener(this, (documentSnapshot, e) -> {
                    showLoading(false);
                    
                    if (e != null) {
                        Log.e("Profile", "Listen failed.", e);
                        Toast.makeText(this, "Error loading profile", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        User newUser = documentSnapshot.toObject(User.class);
                        if (newUser != null) {
                            newUser.setUid(firebaseUser.getUid());
                            newUser.setEmail(firebaseUser.getEmail());
                            
                            // Проверяем, изменилась ли должность
                            if (currentUser != null && !newUser.getPosition().equals(currentUser.getPosition())) {
                                Toast.makeText(this, "Ваша должность обновлена до: " + getPositionDisplayName(newUser.getPosition()), 
                                        Toast.LENGTH_LONG).show();
                            }
                            
                            currentUser = newUser;
                            displayUserInfo();
                        }
                    } else {
                        // User document doesn't exist, create with basic info
                        createUserDocument(firebaseUser);
                    }
                });
    }

    private void createUserDocument(FirebaseUser firebaseUser) {
        currentUser = new User(
                firebaseUser.getUid(),
                firebaseUser.getEmail(),
                "",
                "worker", // default position
                "", // firstName
                "" // lastName
        );
        currentUser.setPhone("");

        db.collection("users")
                .document(firebaseUser.getUid())
                .set(currentUser)
                .addOnSuccessListener(aVoid -> {
                    displayUserInfo();
                    Toast.makeText(this, "Profile created. Please update your information.", Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("Profile", "Error creating user document", e);
                    Toast.makeText(this, "Error creating profile", Toast.LENGTH_SHORT).show();
                });
    }

    private void displayUserInfo() {
        if (currentUser == null) return;

        textFullName.setText(currentUser.getFullName());
        textEmail.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "Not fill");
        
        String phone = currentUser.getPhone();
        textPhone.setText(phone != null && !phone.trim().isEmpty() ? phone : "Not fill");
        
        String position = currentUser.getPosition();
        textPosition.setText(position != null && !position.trim().isEmpty() ? position : "Not fill");
        
        String roleText = getPositionDisplayName(position);
        textRole.setText(roleText);
    }

    private String getPositionDisplayName(String position) {
        if ("director".equalsIgnoreCase(position)) {
            return "Director";
        } else if ("worker".equalsIgnoreCase(position)) {
            return "Worker";
        } else {
            return position != null && !position.trim().isEmpty() ? position : "Not fill";
        }
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

    private void navigateToWorkHours() {
        if (currentUser == null) {
            // Если данные пользователя еще не загружены, показываем сообщение и возвращаемся
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

    private void editProfile() {
        // TODO: Implement profile editing
        // Временно добавим очистку старых полей
        cleanupUserDocument();
        Toast.makeText(this, "Edit profile functionality coming soon", Toast.LENGTH_SHORT).show();
    }

    // Временная функция для очистки старых полей
    private void cleanupUserDocument() {
        if (currentUser != null) {
            String userId = currentUser.getUid();
            Map<String, Object> updates = new HashMap<>();
            updates.put("role", null); // Удаляем поле role
            updates.put("director", null); // Удаляем поле director
            updates.put("worker", null); // Удаляем поле worker
            updates.put("fullName", null); // Удаляем поле fullName
            
            db.collection("users").document(userId)
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "User document cleaned up", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Profile", "Failed to cleanup document", e);
                    });
        }
    }

    private void logout() {
        mAuth.signOut();
        redirectToLogin();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showLoading(boolean show) {
        progressIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
        profileCard.setVisibility(show ? View.GONE : View.VISIBLE);
    }
}