package com.example.elite.apps;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.elite.R;
import com.example.elite.adapters.WorkerHoursAdapter;
import com.example.elite.models.User;
import com.example.elite.models.WorkEntry;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WorkerInfoActivity extends AppCompatActivity {
    private static final String TAG = "WorkerInfoActivity";
    public static final String EXTRA_WORKER = "extra_worker";

    private TextView textWorkerName;
    private TextView textWorkerPosition;
    private TextView textTotalHours;
    private Button btnPreviousMonth;
    private Button btnCurrentMonth;
    private RecyclerView recyclerViewHours;
    private ProgressBar progressBar;
    private TextView emptyTextView;

    private WorkerHoursAdapter hoursAdapter;
    private List<WorkEntry> workEntries;
    private FirebaseFirestore db;
    private User worker;

    private int currentMonth;
    private int currentYear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_info);

        // Get worker from intent
        worker = (User) getIntent().getSerializableExtra(EXTRA_WORKER);
        if (worker == null) {
            Toast.makeText(this, "Error: Worker data not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize current month and year
        Calendar calendar = Calendar.getInstance();
        currentMonth = calendar.get(Calendar.MONTH);
        currentYear = calendar.get(Calendar.YEAR);

        db = FirebaseFirestore.getInstance();

        initializeUI();
        setupToolbar();
        setupRecyclerView();
        setupButtons();
        displayWorkerInfo();
        loadWorkerHours();
    }

    private void initializeUI() {
        textWorkerName = findViewById(R.id.text_worker_name);
        textWorkerPosition = findViewById(R.id.text_worker_position);
        textTotalHours = findViewById(R.id.text_total_hours);
        btnPreviousMonth = findViewById(R.id.btn_previous_month);
        btnCurrentMonth = findViewById(R.id.btn_current_month);
        recyclerViewHours = findViewById(R.id.recycler_view_hours);
        progressBar = findViewById(R.id.progress_bar);
        emptyTextView = findViewById(R.id.empty_text_view);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Worker Info");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        workEntries = new ArrayList<>();
        hoursAdapter = new WorkerHoursAdapter(this, workEntries);
        recyclerViewHours.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewHours.setAdapter(hoursAdapter);
    }

    private void setupButtons() {
        btnPreviousMonth.setOnClickListener(v -> {
            currentMonth--;
            if (currentMonth < 0) {
                currentMonth = 11;
                currentYear--;
            }
            updateMonthButtons();
            loadWorkerHours();
        });

        btnCurrentMonth.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            currentMonth = calendar.get(Calendar.MONTH);
            currentYear = calendar.get(Calendar.YEAR);
            updateMonthButtons();
            loadWorkerHours();
        });

        updateMonthButtons();
    }

    private void updateMonthButtons() {
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, currentMonth);
        calendar.set(Calendar.YEAR, currentYear);

        btnPreviousMonth.setText(getPreviousMonthText());
        btnCurrentMonth.setText(monthFormat.format(calendar.getTime()));
    }

    private String getPreviousMonthText() {
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, currentMonth);
        calendar.set(Calendar.YEAR, currentYear);
        calendar.add(Calendar.MONTH, -1);
        return monthFormat.format(calendar.getTime());
    }

    private void displayWorkerInfo() {
        String fullName = worker.getFullName();
        if (fullName != null && !fullName.isEmpty()) {
            textWorkerName.setText(fullName);
        } else {
            textWorkerName.setText("Unknown Worker");
        }

        String position = worker.getPosition();
        if (position != null && !position.isEmpty()) {
            textWorkerPosition.setText(position.toUpperCase());
        } else {
            textWorkerPosition.setText("N/A");
        }
    }

    private void loadWorkerHours() {
        progressBar.setVisibility(View.VISIBLE);
        emptyTextView.setVisibility(View.GONE);
        recyclerViewHours.setVisibility(View.GONE);

        // Calculate start and end dates for the selected month
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.set(Calendar.YEAR, currentYear);
        startCalendar.set(Calendar.MONTH, currentMonth);
        startCalendar.set(Calendar.DAY_OF_MONTH, 1);
        startCalendar.set(Calendar.HOUR_OF_DAY, 0);
        startCalendar.set(Calendar.MINUTE, 0);
        startCalendar.set(Calendar.SECOND, 0);
        startCalendar.set(Calendar.MILLISECOND, 0);

        Calendar endCalendar = Calendar.getInstance();
        endCalendar.set(Calendar.YEAR, currentYear);
        endCalendar.set(Calendar.MONTH, currentMonth);
        endCalendar.set(Calendar.DAY_OF_MONTH, endCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        endCalendar.set(Calendar.HOUR_OF_DAY, 23);
        endCalendar.set(Calendar.MINUTE, 59);
        endCalendar.set(Calendar.SECOND, 59);
        endCalendar.set(Calendar.MILLISECOND, 999);

        Date startDate = startCalendar.getTime();
        Date endDate = endCalendar.getTime();

        Log.d(TAG, "Loading hours for worker: " + worker.getUid() + 
              " from " + startDate + " to " + endDate);

        // Load all entries for this worker and filter locally to avoid index requirement
        db.collection("workEntries")
                .whereEqualTo("userId", worker.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressBar.setVisibility(View.GONE);
                    workEntries.clear();

                    if (queryDocumentSnapshots.isEmpty()) {
                        emptyTextView.setVisibility(View.VISIBLE);
                        emptyTextView.setText("No work hours found for this month");
                        textTotalHours.setText("Total: 0.0 hours");
                    } else {
                        double totalHours = 0;
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            WorkEntry entry = document.toObject(WorkEntry.class);
                            entry.setId(document.getId());
                            
                            // Filter by date range locally
                            if (entry.getWorkDate() != null && 
                                !entry.getWorkDate().before(startDate) && 
                                !entry.getWorkDate().after(endDate)) {
                                workEntries.add(entry);
                                totalHours += entry.getHoursWorked();
                            }
                        }
                        
                        if (workEntries.isEmpty()) {
                            emptyTextView.setVisibility(View.VISIBLE);
                            emptyTextView.setText("No work hours found for this month");
                            textTotalHours.setText("Total: 0.0 hours");
                        } else {
                            // Sort by date (ascending: 01, 02, 03, etc.)
                            workEntries.sort((entry1, entry2) -> {
                                if (entry1.getWorkDate() == null) return 1;
                                if (entry2.getWorkDate() == null) return -1;
                                return entry1.getWorkDate().compareTo(entry2.getWorkDate());
                            });
                            
                            recyclerViewHours.setVisibility(View.VISIBLE);
                            hoursAdapter.notifyDataSetChanged();
                            textTotalHours.setText(String.format(Locale.getDefault(), 
                                    "Total: %.1f hours", totalHours));
                            Log.d(TAG, "Loaded " + workEntries.size() + " work entries, total: " + totalHours + " hours");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    emptyTextView.setVisibility(View.VISIBLE);
                    emptyTextView.setText("Error loading work hours");
                    Log.e(TAG, "Error loading work hours", e);
                    Toast.makeText(this, "Error loading work hours: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}
