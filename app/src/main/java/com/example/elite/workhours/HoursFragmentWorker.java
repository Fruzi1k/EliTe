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

public class HoursFragmentWorker extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;
    private FloatingActionButton fabAddHours;
    private TextView textWeeklyHours, textMonthlyHours, textEmptyState;
    private CalendarView calendarView;
    private MaterialButton btnQuickAddHours, btnRefresh;
    
    // Firebase
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;
    
    // Data
    private List<WorkEntry> workEntries;
    private List<Building> buildings;
    private Map<String, List<WorkEntry>> workEntriesMap; // Группировка по датам
    private String currentUserId;
    private String currentUserName;
    private String currentUserPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("HoursFragmentWorker", "onCreate started");
        
        try {
            setContentView(R.layout.activity_hours_worker);

            // Initialize Firebase
            auth = FirebaseAuth.getInstance();
            firestore = FirebaseFirestore.getInstance();
            currentUser = auth.getCurrentUser();
            
            if (currentUser == null) {
                Log.w("HoursFragmentWorker", "No current user, redirecting to MainActivity");
                startActivity(new Intent(this, MainActivity.class));
                finish();
                return;
            }
            
            currentUserId = currentUser.getUid();
            workEntries = new ArrayList<>();
            buildings = new ArrayList<>();
            workEntriesMap = new HashMap<>();
            
            Log.d("HoursFragmentWorker", "Initializing views and components");
            initViews();
            setupCalendar();
            setupBottomNavigation();
            loadUserInfo();
            loadBuildings();
            testFirestoreConnection();
            loadWorkEntries();
            
            Log.d("HoursFragmentWorker", "onCreate completed successfully");
        } catch (Exception e) {
            Log.e("HoursFragmentWorker", "Error in onCreate", e);
            Toast.makeText(this, "Error initializing work hours", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void initViews() {
        try {
            Log.d("HoursFragmentWorker", "Initializing views");
            
            fabAddHours = findViewById(R.id.fab_add_hours);
            btnQuickAddHours = findViewById(R.id.btn_quick_add_hours);
            btnRefresh = findViewById(R.id.btn_refresh);
            textWeeklyHours = findViewById(R.id.text_weekly_hours);
            textMonthlyHours = findViewById(R.id.text_monthly_hours);
            textEmptyState = findViewById(R.id.text_empty_state);
            calendarView = findViewById(R.id.calendar_view);
            
            if (fabAddHours == null) {
                Log.e("HoursFragmentWorker", "fab_add_hours not found in layout");
            } else {
                fabAddHours.setOnClickListener(v -> {
                    Log.d("HoursFragmentWorker", "FAB clicked");
                    showAddWorkHoursDialog();
                });
            }
            
            if (btnQuickAddHours == null) {
                Log.e("HoursFragmentWorker", "btn_quick_add_hours not found in layout");
            } else {
                btnQuickAddHours.setOnClickListener(v -> {
                    Log.d("HoursFragmentWorker", "Quick Add Hours button clicked");
                    showAddWorkHoursDialog();
                });
            }
            
            if (btnRefresh == null) {
                Log.e("HoursFragmentWorker", "btn_refresh not found in layout");
            } else {
                btnRefresh.setOnClickListener(v -> {
                    Log.d("HoursFragmentWorker", "Refresh button clicked");
                    Toast.makeText(this, "Refreshing work entries...", Toast.LENGTH_SHORT).show();
                    loadWorkEntries();
                });
            }
            
            if (textWeeklyHours == null) {
                Log.e("HoursFragmentWorker", "text_weekly_hours not found in layout");
            }
            if (textMonthlyHours == null) {
                Log.e("HoursFragmentWorker", "text_monthly_hours not found in layout");
            }
            if (calendarView == null) {
                Log.e("HoursFragmentWorker", "calendar_view not found in layout");
            }
            if (textEmptyState == null) {
                Log.e("HoursFragmentWorker", "text_empty_state not found in layout");
            }
            
            Log.d("HoursFragmentWorker", "Views initialized successfully");
        } catch (Exception e) {
            Log.e("HoursFragmentWorker", "Error initializing views", e);
        }
    }
    
    private void setupCalendar() {
        try {
            Log.d("HoursFragmentWorker", "Setting up Calendar");
            
            if (calendarView == null) {
                Log.e("HoursFragmentWorker", "CalendarView is null, cannot setup");
                return;
            }
            
            calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
                @Override
                public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);
                    String dateKey = formatDateKey(selectedDate.getTime());
                    
                    Log.d("HoursFragmentWorker", "Date selected: " + dateKey);
                    showWorkEntriesForDate(dateKey, selectedDate.getTime());
                }
            });
            
            Log.d("HoursFragmentWorker", "Calendar setup completed");
        } catch (Exception e) {
            Log.e("HoursFragmentWorker", "Error setting up Calendar", e);
        }
    }
    
    private String formatDateKey(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(date);
    }
    
    private void showWorkEntriesForDate(String dateKey, Date selectedDate) {
        List<WorkEntry> entriesForDate = workEntriesMap.get(dateKey);
        
        if (entriesForDate == null || entriesForDate.isEmpty()) {
            // Нет записей для этой даты - предложить создать
            showCreateWorkEntryDialog(selectedDate);
        } else {
            // Показать записи для этой даты
            showWorkEntriesDialog(entriesForDate, selectedDate);
        }
    }
    
    private void showCreateWorkEntryDialog(Date selectedDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        
        new AlertDialog.Builder(this)
                .setTitle("No Work Entry")
                .setMessage("No work entry found for " + sdf.format(selectedDate) + ".\nWould you like to add work hours for this date?")
                .setPositiveButton("Add Hours", (dialog, which) -> {
                    // Открыть диалог добавления с предустановленной датой
                    showAddWorkHoursDialogForDate(selectedDate);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void showWorkEntriesDialog(List<WorkEntry> entries, Date selectedDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        StringBuilder message = new StringBuilder();
        message.append("Work entries for ").append(sdf.format(selectedDate)).append(":\n\n");
        
        double totalHours = 0;
        for (WorkEntry entry : entries) {
            message.append("• ").append(entry.getProjectName()).append("\n");
            message.append("  Hours: ").append(entry.getHoursWorked()).append("\n");
            if (entry.getDescription() != null && !entry.getDescription().trim().isEmpty()) {
                message.append("  Description: ").append(entry.getDescription()).append("\n");
            }
            message.append("\n");
            totalHours += entry.getHoursWorked();
        }
        
        message.append("Total hours: ").append(totalHours);
        
        new AlertDialog.Builder(this)
                .setTitle("Work Entries")
                .setMessage(message.toString())
                .setPositiveButton("Add More", (dialog, which) -> {
                    showAddWorkHoursDialogForDate(selectedDate);
                })
                .setNegativeButton("Close", null)
                .show();
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
                    }
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
                });
    }
    
    private void testFirestoreConnection() {
        Log.d("HoursFragmentWorker", "Testing Firestore connection...");
        
        firestore.collection("workEntries")
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d("HoursFragmentWorker", "Firestore connection test successful. Collection exists.");
                    Log.d("HoursFragmentWorker", "Sample query returned " + queryDocumentSnapshots.size() + " documents");
                })
                .addOnFailureListener(e -> {
                    Log.e("HoursFragmentWorker", "Firestore connection test failed", e);
                    Toast.makeText(this, "Database connection error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
    
    private void groupWorkEntriesByDate() {
        workEntriesMap.clear();
        
        for (WorkEntry entry : workEntries) {
            String dateKey = formatDateKey(entry.getWorkDate());
            
            if (!workEntriesMap.containsKey(dateKey)) {
                workEntriesMap.put(dateKey, new ArrayList<>());
            }
            workEntriesMap.get(dateKey).add(entry);
        }
        
        Log.d("HoursFragmentWorker", "Grouped work entries into " + workEntriesMap.size() + " dates");
    }
    
    private void showAddWorkHoursDialogForDate(Date selectedDate) {
        // Создаем диалог добавления с предустановленной датой
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
        
        // Set selected date
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        editWorkDate.setText(sdf.format(selectedDate));
        
        // Date picker
        editWorkDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(selectedDate);
            
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                Calendar selectedCalendar = Calendar.getInstance();
                selectedCalendar.set(year, month, dayOfMonth);
                editWorkDate.setText(sdf.format(selectedCalendar.getTime()));
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });
        
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Add Work Hours")
                .setView(dialogView)
                .setPositiveButton("Submit", null)
                .setNegativeButton("Cancel", null)
                .create();
        
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String dateStr = editWorkDate.getText().toString().trim();
                String projectName = editProject.getText().toString().trim();
                String hoursStr = editHours.getText().toString().trim();
                String description = editDescription.getText().toString().trim();
                
                if (validateInput(dateStr, projectName, hoursStr)) {
                    try {
                        Date workDate = sdf.parse(dateStr);
                        double hours = Double.parseDouble(hoursStr);
                        
                        String projectId = "";
                        for (Building building : buildings) {
                            if (building.getName().equals(projectName)) {
                                projectId = building.getId();
                                break;
                            }
                        }
                        
                        addWorkEntry(workDate, projectId, projectName, hours, description);
                        dialog.dismiss();
                    } catch (Exception e) {
                        Toast.makeText(this, "Invalid date or hours format", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });
        
        dialog.show();
    }
    
    private void loadWorkEntries() {
        Log.d("HoursFragmentWorker", "Loading work entries for user: " + currentUserId);
        
        // Try simple approach first - load all work entries and filter on client side
        firestore.collection("workEntries")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d("HoursFragmentWorker", "Successfully loaded " + queryDocumentSnapshots.size() + " total work entries from Firestore");
                    workEntries.clear();
                    
                    int userEntriesCount = 0;
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            WorkEntry workEntry = document.toObject(WorkEntry.class);
                            workEntry.setId(document.getId());
                            
                            // Filter on client side for this user's entries
                            if (currentUserId != null && currentUserId.equals(workEntry.getUserId())) {
                                workEntries.add(workEntry);
                                userEntriesCount++;
                                Log.d("HoursFragmentWorker", "Added work entry: " + workEntry.getProjectName() + " - " + workEntry.getHoursWorked() + " hours");
                            }
                        } catch (Exception e) {
                            Log.e("HoursFragmentWorker", "Error parsing work entry document: " + document.getId(), e);
                        }
                    }
                    
                    Log.d("HoursFragmentWorker", "Total work entries for current user: " + userEntriesCount);
                    
                    // Группируем записи по датам
                    groupWorkEntriesByDate();
                    
                    // Show/hide empty state
                    if (textEmptyState != null) {
                        if (workEntries.isEmpty()) {
                            Log.d("HoursFragmentWorker", "No work entries, showing empty state");
                            textEmptyState.setVisibility(View.VISIBLE);
                        } else {
                            Log.d("HoursFragmentWorker", "Work entries found, hiding empty state");
                            textEmptyState.setVisibility(View.GONE);
                        }
                    }
                    
                    updateStatistics();
                })
                .addOnFailureListener(e -> {
                    Log.e("HoursFragmentWorker", "Failed to load work entries", e);
                    Log.e("HoursFragmentWorker", "Error details: " + e.getMessage());
                    
                    // Try the original approach as fallback
                    Log.d("HoursFragmentWorker", "Trying fallback approach with whereEqualTo");
                    loadWorkEntriesWithFilter();
                });
    }
    
    private void loadWorkEntriesWithFilter() {
        Log.d("HoursFragmentWorker", "Loading work entries with filter for user: " + currentUserId);
        
        firestore.collection("workEntries")
                .whereEqualTo("userId", currentUserId)
                .limit(50)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d("HoursFragmentWorker", "Successfully loaded " + queryDocumentSnapshots.size() + " work entries with filter");
                    workEntries.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            WorkEntry workEntry = document.toObject(WorkEntry.class);
                            workEntry.setId(document.getId());
                            workEntries.add(workEntry);
                            Log.d("HoursFragmentWorker", "Added work entry: " + workEntry.getProjectName() + " - " + workEntry.getHoursWorked() + " hours");
                        } catch (Exception e) {
                            Log.e("HoursFragmentWorker", "Error parsing work entry document: " + document.getId(), e);
                        }
                    }
                    
                    Log.d("HoursFragmentWorker", "Total work entries loaded: " + workEntries.size());
                    
                    // Группируем записи по датам
                    groupWorkEntriesByDate();
                    
                    // Show/hide empty state
                    if (textEmptyState != null) {
                        if (workEntries.isEmpty()) {
                            Log.d("HoursFragmentWorker", "No work entries, showing empty state");
                            textEmptyState.setVisibility(View.VISIBLE);
                        } else {
                            Log.d("HoursFragmentWorker", "Work entries found, hiding empty state");
                            textEmptyState.setVisibility(View.GONE);
                        }
                    }
                    
                    updateStatistics();
                })
                .addOnFailureListener(e -> {
                    Log.e("HoursFragmentWorker", "Failed to load work entries with filter", e);
                    Log.e("HoursFragmentWorker", "Error details: " + e.getMessage());
                    Toast.makeText(this, "Failed to load work entries: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
    
    private void updateStatistics() {
        Calendar calendar = Calendar.getInstance();
        
        // Weekly hours
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        Date weekStart = calendar.getTime();
        calendar.add(Calendar.WEEK_OF_YEAR, 1);
        Date weekEnd = calendar.getTime();
        
        double weeklyHours = calculateHoursForPeriod(weekStart, weekEnd);
        
        // Monthly hours
        calendar.setTime(new Date());
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        Date monthStart = calendar.getTime();
        calendar.add(Calendar.MONTH, 1);
        Date monthEnd = calendar.getTime();
        
        double monthlyHours = calculateHoursForPeriod(monthStart, monthEnd);
        
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
    
    private void showAddWorkHoursDialog() {
        showAddWorkHoursDialog(null);
    }
    
    private void showAddWorkHoursDialog(WorkEntry existingEntry) {
        // Use the same dialog as director but simplified for workers
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
        Date dateToShow = existingEntry != null ? existingEntry.getWorkDate() : new Date();
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
            }
            
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                Calendar selectedCalendar = Calendar.getInstance();
                selectedCalendar.set(year, month, dayOfMonth);
                editWorkDate.setText(sdf.format(selectedCalendar.getTime()));
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });
        
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(existingEntry == null ? "Report Work Hours" : "Edit Work Hours")
                .setView(dialogView)
                .setPositiveButton(existingEntry == null ? "Submit" : "Update", null)
                .setNegativeButton("Cancel", null)
                .create();
        
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String dateStr = editWorkDate.getText().toString().trim();
                String projectName = editProject.getText().toString().trim();
                String hoursStr = editHours.getText().toString().trim();
                String description = editDescription.getText().toString().trim();
                
                if (validateInput(dateStr, projectName, hoursStr)) {
                    try {
                        Date workDate = sdf.parse(dateStr);
                        double hours = Double.parseDouble(hoursStr);
                        
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
    
    private boolean validateInput(String date, String project, String hours) {
        if (date.isEmpty() || project.isEmpty() || hours.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
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
        Log.d("HoursFragmentWorker", "Adding work entry - Project: " + projectName + ", Hours: " + hours + ", User: " + currentUserId);
        
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
        
        Log.d("HoursFragmentWorker", "Work entry data prepared: " + workEntry.toString());
        
        firestore.collection("workEntries")
                .add(workEntry)
                .addOnSuccessListener(documentReference -> {
                    Log.d("HoursFragmentWorker", "Work entry saved successfully with ID: " + documentReference.getId());
                    Toast.makeText(this, "Work hours submitted successfully", Toast.LENGTH_SHORT).show();
                    loadWorkEntries();
                })
                .addOnFailureListener(e -> {
                    Log.e("HoursFragmentWorker", "Failed to save work entry", e);
                    Toast.makeText(this, "Failed to submit work hours: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
                    Toast.makeText(this, "Work hours updated", Toast.LENGTH_SHORT).show();
                    loadWorkEntries();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update work hours", Toast.LENGTH_SHORT).show();
                });
    }
    
    private void showWorkEntryDetails(WorkEntry workEntry) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        
        String details = "Date: " + sdf.format(workEntry.getWorkDate()) + "\n" +
                        "Project: " + workEntry.getProjectName() + "\n" +
                        "Hours: " + workEntry.getHoursWorked() + "\n" +
                        "Submitted: " + sdf.format(workEntry.getCreatedAt());
        
        if (workEntry.getDescription() != null && !workEntry.getDescription().trim().isEmpty()) {
            details += "\n\nDescription:\n" + workEntry.getDescription();
        }
        
        new AlertDialog.Builder(this)
                .setTitle("Work Entry")
                .setMessage(details)
                .setPositiveButton("Edit", (dialog, which) -> showAddWorkHoursDialog(workEntry))
                .setNegativeButton("Close", null)
                .show();
    }

    private void setupBottomNavigation() {
        bottomNavigation = findViewById(R.id.bottom_navigation);
        
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, Profile.class));
                return true;
            } else if (itemId == R.id.nav_work) {
                startActivity(new Intent(this, WorkerActivity.class));
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