package com.example.elite;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HoursFragmentDirector extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;
    private CalendarView calendarView;
    private FloatingActionButton fabAddHours;
    private MaterialButton buttonFilterWeek, buttonFilterMonth;
    private TextView textWeeklyHours, textMonthlyHours, textSelectedDate, textNoEntries;
    private RecyclerView recyclerWorkEntries;
    
    // Firebase
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;
    
    // Data
    private List<WorkEntry> workEntries;
    private List<Building> buildings;
    private WorkEntryAdapter workEntryAdapter;
    private Date selectedDate;
    private String currentUserId;
    private String currentUserName;
    private String currentUserPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hours_director);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        currentUser = auth.getCurrentUser();
        
        if (currentUser == null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }
        
        currentUserId = currentUser.getUid();
        selectedDate = new Date();
        workEntries = new ArrayList<>();
        buildings = new ArrayList<>();
        
        initViews();
        setupRecyclerView();
        setupClickListeners();
        setupBottomNavigation();
        loadUserInfo();
        loadBuildings();
        loadWorkEntries();
        updateStatistics();
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
    
    private void initViews() {
        calendarView = findViewById(R.id.calendar_view);
        fabAddHours = findViewById(R.id.fab_add_hours);
        buttonFilterWeek = findViewById(R.id.button_filter_week);
        buttonFilterMonth = findViewById(R.id.button_filter_month);
        textWeeklyHours = findViewById(R.id.text_weekly_hours);
        textMonthlyHours = findViewById(R.id.text_monthly_hours);
        textSelectedDate = findViewById(R.id.text_selected_date);
        textNoEntries = findViewById(R.id.text_no_entries);
        recyclerWorkEntries = findViewById(R.id.recycler_work_entries);
        
        // Set initial date
        updateSelectedDateText();
    }
    
    private void setupRecyclerView() {
        workEntryAdapter = new WorkEntryAdapter(workEntries, this, new WorkEntryAdapter.OnWorkEntryActionListener() {
            @Override
            public void onWorkEntryEdit(WorkEntry workEntry) {
                showAddWorkHoursDialog(workEntry);
            }

            @Override
            public void onWorkEntryClick(WorkEntry workEntry) {
                // Show details if needed
                showWorkEntryDetails(workEntry);
            }
        });
        
        recyclerWorkEntries.setLayoutManager(new LinearLayoutManager(this));
        recyclerWorkEntries.setAdapter(workEntryAdapter);
    }
    
    private void setupClickListeners() {
        fabAddHours.setOnClickListener(v -> showAddWorkHoursDialog(null));
        
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth);
            selectedDate = calendar.getTime();
            updateSelectedDateText();
            loadWorkEntriesForDate(selectedDate);
        });
        
        buttonFilterWeek.setOnClickListener(v -> {
            // Filter for current week
            filterWorkEntriesForPeriod("week");
        });
        
        buttonFilterMonth.setOnClickListener(v -> {
            // Filter for current month
            filterWorkEntriesForPeriod("month");
        });
    }
    
    private void loadUserInfo() {
        firestore.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String firstName = documentSnapshot.getString("firstName");
                        String lastName = documentSnapshot.getString("lastName");
                        String position = documentSnapshot.getString("position");
                        
                        currentUserName = firstName + " " + lastName;
                        currentUserPosition = position;
                        
                        Log.d("HoursDirector", "User loaded: " + currentUserName + " - " + currentUserPosition);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("HoursDirector", "Error loading user info", e);
                    Toast.makeText(this, "Error loading user information", Toast.LENGTH_SHORT).show();
                });
    }
    
    private void loadBuildings() {
        firestore.collection("buildings")
                .orderBy("name")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    buildings.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Building building = document.toObject(Building.class);
                        building.setId(document.getId());
                        buildings.add(building);
                    }
                    Log.d("HoursDirector", "Loaded " + buildings.size() + " buildings");
                })
                .addOnFailureListener(e -> {
                    Log.e("HoursDirector", "Error loading buildings", e);
                });
    }
    
    private void loadWorkEntries() {
        firestore.collection("workEntries")
                .whereEqualTo("userId", currentUserId)
                .orderBy("workDate", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    workEntries.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        WorkEntry workEntry = document.toObject(WorkEntry.class);
                        workEntry.setId(document.getId());
                        workEntries.add(workEntry);
                    }
                    
                    Log.d("HoursDirector", "Loaded " + workEntries.size() + " work entries");
                    loadWorkEntriesForDate(selectedDate);
                    updateStatistics();
                })
                .addOnFailureListener(e -> {
                    Log.e("HoursDirector", "Error loading work entries", e);
                    Toast.makeText(this, "Error loading work entries", Toast.LENGTH_SHORT).show();
                });
    }
    
    private void loadWorkEntriesForDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date startOfDay = calendar.getTime();
        
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        Date startOfNextDay = calendar.getTime();
        
        List<WorkEntry> entriesForDate = new ArrayList<>();
        for (WorkEntry entry : workEntries) {
            if (entry.getWorkDate().getTime() >= startOfDay.getTime() && 
                entry.getWorkDate().getTime() < startOfNextDay.getTime()) {
                entriesForDate.add(entry);
            }
        }
        
        updateWorkEntriesDisplay(entriesForDate);
    }
    
    private void updateWorkEntriesDisplay(List<WorkEntry> entries) {
        if (entries.isEmpty()) {
            textNoEntries.setVisibility(View.VISIBLE);
            recyclerWorkEntries.setVisibility(View.GONE);
        } else {
            textNoEntries.setVisibility(View.GONE);
            recyclerWorkEntries.setVisibility(View.VISIBLE);
            workEntryAdapter.updateWorkEntries(entries);
        }
    }
    
    private void updateSelectedDateText() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        textSelectedDate.setText(sdf.format(selectedDate));
    }
    
    private void updateStatistics() {
        Calendar calendar = Calendar.getInstance();
        
        // Calculate weekly hours
        calendar.setTime(new Date());
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date weekStart = calendar.getTime();
        
        calendar.add(Calendar.WEEK_OF_YEAR, 1);
        Date weekEnd = calendar.getTime();
        
        double weeklyHours = calculateHoursForPeriod(weekStart, weekEnd);
        
        // Calculate monthly hours
        calendar.setTime(new Date());
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date monthStart = calendar.getTime();
        
        calendar.add(Calendar.MONTH, 1);
        Date monthEnd = calendar.getTime();
        
        double monthlyHours = calculateHoursForPeriod(monthStart, monthEnd);
        
        // Update UI
        textWeeklyHours.setText(String.format(Locale.getDefault(), "%.1f hrs", weeklyHours));
        textMonthlyHours.setText(String.format(Locale.getDefault(), "%.1f hrs", monthlyHours));
    }
    
    private double calculateHoursForPeriod(Date start, Date end) {
        double totalHours = 0;
        for (WorkEntry entry : workEntries) {
            if (entry.getWorkDate().getTime() >= start.getTime() && 
                entry.getWorkDate().getTime() < end.getTime()) {
                totalHours += entry.getHoursWorked();
            }
        }
        return totalHours;
    }
    
    private void filterWorkEntriesForPeriod(String period) {
        Calendar calendar = Calendar.getInstance();
        Date start, end;
        
        if ("week".equals(period)) {
            // Current week
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            start = calendar.getTime();
            
            calendar.add(Calendar.WEEK_OF_YEAR, 1);
            end = calendar.getTime();
        } else {
            // Current month
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            start = calendar.getTime();
            
            calendar.add(Calendar.MONTH, 1);
            end = calendar.getTime();
        }
        
        List<WorkEntry> filteredEntries = new ArrayList<>();
        for (WorkEntry entry : workEntries) {
            if (entry.getWorkDate().getTime() >= start.getTime() && 
                entry.getWorkDate().getTime() < end.getTime()) {
                filteredEntries.add(entry);
            }
        }
        
        updateWorkEntriesDisplay(filteredEntries);
        
        String periodText = "week".equals(period) ? "This Week" : "This Month";
        textSelectedDate.setText(periodText);
    }
    
    private void showAddWorkHoursDialog(WorkEntry existingEntry) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_work_hours, null);
        
        TextInputEditText editWorkDate = dialogView.findViewById(R.id.edit_work_date);
        AutoCompleteTextView editProject = dialogView.findViewById(R.id.edit_project);
        TextInputEditText editHours = dialogView.findViewById(R.id.edit_hours);
        TextInputEditText editDescription = dialogView.findViewById(R.id.edit_description);
        TextView textUserInfo = dialogView.findViewById(R.id.text_user_info);
        
        // Set user info
        if (currentUserName != null && currentUserPosition != null) {
            textUserInfo.setText(currentUserName + " - " + currentUserPosition);
        }
        
        // Setup project dropdown
        List<String> projectNames = new ArrayList<>();
        for (Building building : buildings) {
            projectNames.add(building.getName());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, projectNames);
        editProject.setAdapter(adapter);
        
        // Set current date
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date dateToShow = existingEntry != null ? existingEntry.getWorkDate() : selectedDate;
        editWorkDate.setText(sdf.format(dateToShow));
        
        // Pre-fill if editing
        if (existingEntry != null) {
            editProject.setText(existingEntry.getProjectName());
            editHours.setText(String.valueOf(existingEntry.getHoursWorked()));
            editDescription.setText(existingEntry.getDescription());
        }
        
        // Date picker
        editWorkDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            if (existingEntry != null) {
                calendar.setTime(existingEntry.getWorkDate());
            } else {
                calendar.setTime(selectedDate);
            }
            
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                Calendar selectedCalendar = Calendar.getInstance();
                selectedCalendar.set(year, month, dayOfMonth);
                editWorkDate.setText(sdf.format(selectedCalendar.getTime()));
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });
        
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(existingEntry == null ? "Add Work Hours" : "Edit Work Hours")
                .setView(dialogView)
                .setPositiveButton(existingEntry == null ? "Add" : "Update", null)
                .setNegativeButton("Cancel", null)
                .create();
        
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String dateStr = editWorkDate.getText().toString().trim();
                String projectName = editProject.getText().toString().trim();
                String hoursStr = editHours.getText().toString().trim();
                String description = editDescription.getText().toString().trim();
                
                if (validateWorkHoursInput(dateStr, projectName, hoursStr)) {
                    try {
                        Date workDate = sdf.parse(dateStr);
                        double hours = Double.parseDouble(hoursStr);
                        
                        // Find project ID
                        String projectId = "";
                        for (Building building : buildings) {
                            if (building.getName().equals(projectName)) {
                                projectId = building.getId();
                                break;
                            }
                        }
                        
                        if (existingEntry == null) {
                            addWorkEntry(workDate, projectId, projectName, hours, description);
                        } else {
                            updateWorkEntry(existingEntry, workDate, projectId, projectName, hours, description);
                        }
                        
                        dialog.dismiss();
                    } catch (Exception e) {
                        Toast.makeText(this, "Invalid date or hours format", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });
        
        dialog.show();
    }
    
    private boolean validateWorkHoursInput(String date, String project, String hours) {
        if (date.isEmpty()) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        if (project.isEmpty()) {
            Toast.makeText(this, "Please select a project", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        if (hours.isEmpty()) {
            Toast.makeText(this, "Please enter hours worked", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        try {
            double hoursValue = Double.parseDouble(hours);
            if (hoursValue <= 0 || hoursValue > 24) {
                Toast.makeText(this, "Hours must be between 0 and 24", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid hours format", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        return true;
    }
    
    private void addWorkEntry(Date workDate, String projectId, String projectName, double hours, String description) {
        Map<String, Object> workEntry = new HashMap<>();
        workEntry.put("userId", currentUserId);
        workEntry.put("userName", currentUserName);
        workEntry.put("userPosition", currentUserPosition);
        workEntry.put("projectId", projectId);
        workEntry.put("projectName", projectName);
        workEntry.put("workDate", workDate);
        workEntry.put("hoursWorked", hours);
        workEntry.put("description", description);
        workEntry.put("createdAt", new Date());
        workEntry.put("updatedAt", new Date());
        
        firestore.collection("workEntries")
                .add(workEntry)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Work hours added successfully", Toast.LENGTH_SHORT).show();
                    loadWorkEntries(); // Reload all entries
                })
                .addOnFailureListener(e -> {
                    Log.e("HoursDirector", "Error adding work entry", e);
                    Toast.makeText(this, "Failed to add work hours", Toast.LENGTH_SHORT).show();
                });
    }
    
    private void updateWorkEntry(WorkEntry existingEntry, Date workDate, String projectId, String projectName, double hours, String description) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("projectId", projectId);
        updates.put("projectName", projectName);
        updates.put("workDate", workDate);
        updates.put("hoursWorked", hours);
        updates.put("description", description);
        updates.put("updatedAt", new Date());
        
        firestore.collection("workEntries").document(existingEntry.getId())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Work hours updated successfully", Toast.LENGTH_SHORT).show();
                    loadWorkEntries(); // Reload all entries
                })
                .addOnFailureListener(e -> {
                    Log.e("HoursDirector", "Error updating work entry", e);
                    Toast.makeText(this, "Failed to update work hours", Toast.LENGTH_SHORT).show();
                });
    }
    
    private void showWorkEntryDetails(WorkEntry workEntry) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat timeSdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        
        String details = "Date: " + sdf.format(workEntry.getWorkDate()) + "\n" +
                        "Project: " + workEntry.getProjectName() + "\n" +
                        "Hours: " + workEntry.getHoursWorked() + "\n" +
                        "Worker: " + workEntry.getUserName() + " (" + workEntry.getUserPosition() + ")\n" +
                        "Reported: " + sdf.format(workEntry.getCreatedAt()) + " at " + timeSdf.format(workEntry.getCreatedAt());
        
        if (workEntry.getDescription() != null && !workEntry.getDescription().trim().isEmpty()) {
            details += "\n\nDescription:\n" + workEntry.getDescription();
        }
        
        new AlertDialog.Builder(this)
                .setTitle("Work Entry Details")
                .setMessage(details)
                .setPositiveButton("Edit", (dialog, which) -> showAddWorkHoursDialog(workEntry))
                .setNegativeButton("Close", null)
                .setNeutralButton("Delete", (dialog, which) -> confirmDeleteWorkEntry(workEntry))
                .show();
    }
    
    private void confirmDeleteWorkEntry(WorkEntry workEntry) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Work Entry")
                .setMessage("Are you sure you want to delete this work entry?")
                .setPositiveButton("Delete", (dialog, which) -> deleteWorkEntry(workEntry))
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void deleteWorkEntry(WorkEntry workEntry) {
        firestore.collection("workEntries").document(workEntry.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Work entry deleted", Toast.LENGTH_SHORT).show();
                    loadWorkEntries(); // Reload all entries
                })
                .addOnFailureListener(e -> {
                    Log.e("HoursDirector", "Error deleting work entry", e);
                    Toast.makeText(this, "Failed to delete work entry", Toast.LENGTH_SHORT).show();
                });
    }
}