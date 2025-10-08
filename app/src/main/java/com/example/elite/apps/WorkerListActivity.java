package com.example.elite.apps;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.elite.R;
import com.example.elite.adapters.WorkerAdapter;
import com.example.elite.models.User;
import com.example.elite.profile.Profile;
import com.example.elite.work.DirectorActivity;
import com.example.elite.workhours.HoursFragmentDirector;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class WorkerListActivity extends AppCompatActivity {
    private static final String TAG = "WorkerListActivity";

    private RecyclerView recyclerViewWorkers;
    private WorkerAdapter workerAdapter;
    private List<User> workersList;
    private ProgressBar progressBar;
    private TextView emptyTextView;
    private BottomNavigationView bottomNavigation;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_list);

        initializeFirebase();
        initializeUI();
        setupToolbar();
        setupRecyclerView();
        setupBottomNavigation();
        loadWorkers();
    }

    private void initializeFirebase() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    private void initializeUI() {
        recyclerViewWorkers = findViewById(R.id.recycler_view_workers);
        progressBar = findViewById(R.id.progress_bar);
        emptyTextView = findViewById(R.id.empty_text_view);
        bottomNavigation = findViewById(R.id.bottom_navigation);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Worker List");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        workersList = new ArrayList<>();
        workerAdapter = new WorkerAdapter(this, workersList);
        recyclerViewWorkers.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewWorkers.setAdapter(workerAdapter);
    }

    private void setupBottomNavigation() {
        // Set apps as selected first, before setting listener
        bottomNavigation.setSelectedItemId(R.id.nav_apps);
        
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, Profile.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_work) {
                startActivity(new Intent(this, DirectorActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_work_hour) {
                startActivity(new Intent(this, HoursFragmentDirector.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_apps) {
                finish(); // Just close this activity to return to AppsActivity
                return true;
            }
            return false;
        });
    }

    private void loadWorkers() {
        progressBar.setVisibility(View.VISIBLE);
        emptyTextView.setVisibility(View.GONE);
        recyclerViewWorkers.setVisibility(View.GONE);

        db.collection("users")
                .whereEqualTo("position", "worker")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressBar.setVisibility(View.GONE);
                    workersList.clear();

                    if (queryDocumentSnapshots.isEmpty()) {
                        emptyTextView.setVisibility(View.VISIBLE);
                        emptyTextView.setText("No workers found");
                    } else {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            User worker = document.toObject(User.class);
                            workersList.add(worker);
                        }
                        recyclerViewWorkers.setVisibility(View.VISIBLE);
                        workerAdapter.notifyDataSetChanged();
                        Log.d(TAG, "Loaded " + workersList.size() + " workers");
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    emptyTextView.setVisibility(View.VISIBLE);
                    emptyTextView.setText("Error loading workers");
                    Log.e(TAG, "Error loading workers", e);
                    Toast.makeText(this, "Error loading workers: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload workers when returning to this activity
        loadWorkers();
    }
}
