package com.example.elite;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DirectorActivity extends AppCompatActivity implements BuildingAdapter.OnBuildingActionListener {
    
    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseFirestore db;
    private BottomNavigationView bottomNavigationView;
    private RecyclerView recyclerViewBuildings;
    private BuildingAdapter buildingAdapter;
    private FloatingActionButton fabAddBuilding;
    private CircularProgressIndicator progressIndicator;
    private android.widget.TextView textEmptyState;
    private android.widget.Button buttonLogout;
    
    private List<Building> buildingsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_director);

        initializeFirebase();
        initializeViews();
        setupRecyclerView();
        setupClickListeners();
        setupBottomNavigation();
        
        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
            return;
        }
        
        loadBuildings();
    }

    private void initializeFirebase() {
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
    }

    private void initializeViews() {
        recyclerViewBuildings = findViewById(R.id.recycler_view_buildings);
        fabAddBuilding = findViewById(R.id.fab_add_building);
        progressIndicator = findViewById(R.id.progress_indicator);
        textEmptyState = findViewById(R.id.text_empty_state);
        buttonLogout = findViewById(R.id.button_logout_director);
        bottomNavigationView = findViewById(R.id.bottom_navigation_director);
        
        buildingsList = new ArrayList<>();
    }

    private void setupRecyclerView() {
        buildingAdapter = new BuildingAdapter(this, buildingsList, this);
        recyclerViewBuildings.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewBuildings.setAdapter(buildingAdapter);
    }

    private void setupClickListeners() {
        fabAddBuilding.setOnClickListener(v -> showAddBuildingDialog(null));
        
        buttonLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        });
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, Profile.class));
                return true;
            } else if (itemId == R.id.nav_work) {
                // Already on work page
                return true;
            } else if (itemId == R.id.nav_work_hour) {
                startActivity(new Intent(this, HoursFragmentDirector.class));
                return true;
            }
            return false;
        });
        
        bottomNavigationView.setSelectedItemId(R.id.nav_work);
    }

    private void loadBuildings() {
        showLoading(true);
        
        db.collection("buildings")
                .get()
                .addOnCompleteListener(task -> {
                    showLoading(false);
                    
                    if (task.isSuccessful()) {
                        buildingsList.clear();
                        
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Building building = new Building(
                                    document.getId(),
                                    document.getString("name"),
                                    document.getString("city"),
                                    document.getString("street"),
                                    document.getString("code"),
                                    document.getString("googleMapsUrl")
                            );
                            buildingsList.add(building);
                        }
                        
                        buildingAdapter.updateBuildings(buildingsList);
                        updateEmptyState();
                        
                    } else {
                        Toast.makeText(this, "Error loading buildings: " + task.getException().getMessage(), 
                                Toast.LENGTH_SHORT).show();
                        updateEmptyState();
                    }
                });
    }

    private void showAddBuildingDialog(Building existingBuilding) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_building, null);
        
        EditText editName = dialogView.findViewById(R.id.edit_building_name);
        EditText editStreet = dialogView.findViewById(R.id.edit_building_street);
        EditText editCity = dialogView.findViewById(R.id.edit_building_city);
        EditText editPostalCode = dialogView.findViewById(R.id.edit_building_postal_code);
        EditText editMapsUrl = dialogView.findViewById(R.id.edit_building_maps_url);
        MaterialButton buttonOpenMaps = dialogView.findViewById(R.id.button_open_maps);
        
        // Pre-fill fields if editing existing building
        if (existingBuilding != null) {
            editName.setText(existingBuilding.getName());
            editStreet.setText(existingBuilding.getStreet());
            editCity.setText(existingBuilding.getCity());
            editPostalCode.setText(existingBuilding.getCode());
            editMapsUrl.setText(existingBuilding.getGoogleMapsUrl());
        }
        
        buttonOpenMaps.setOnClickListener(v -> {
            String url = editMapsUrl.getText().toString().trim();
            if (!url.isEmpty()) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(this, "Could not open URL", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Please enter a Google Maps URL first", Toast.LENGTH_SHORT).show();
            }
        });
        
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(existingBuilding == null ? "Add Project" : "Edit Project")
                .setView(dialogView)
                .setPositiveButton(existingBuilding == null ? "Add" : "Update", null)
                .setNegativeButton("Cancel", null)
                .create();
        
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String name = editName.getText().toString().trim();
                String street = editStreet.getText().toString().trim();
                String city = editCity.getText().toString().trim();
                String postalCode = editPostalCode.getText().toString().trim();
                String mapsUrl = editMapsUrl.getText().toString().trim();
                
                if (validateInput(name, street)) {
                    if (existingBuilding == null) {
                        addBuilding(name, street, city, postalCode, mapsUrl);
                    } else {
                        updateBuilding(existingBuilding.getId(), name, street, city, postalCode, mapsUrl);
                    }
                    dialog.dismiss();
                }
            });
        });
        
        dialog.show();
    }

    private boolean validateInput(String name, String street) {
        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter project name", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        if (street.isEmpty()) {
            Toast.makeText(this, "Please enter street address", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        return true;
    }

    private void addBuilding(String name, String street, String city, String postalCode, String mapsUrl) {
        Map<String, Object> building = new HashMap<>();
        building.put("name", name);
        building.put("street", street);
        building.put("city", city);
        building.put("code", postalCode);
        building.put("googleMapsUrl", mapsUrl);
        
        db.collection("buildings")
                .add(building)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Project added successfully", Toast.LENGTH_SHORT).show();
                    loadBuildings();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error adding project: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void updateBuilding(String buildingId, String name, String street, String city, String postalCode, String mapsUrl) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("street", street);
        updates.put("city", city);
        updates.put("code", postalCode);
        updates.put("googleMapsUrl", mapsUrl);
        
        db.collection("buildings").document(buildingId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Project updated successfully", Toast.LENGTH_SHORT).show();
                    loadBuildings();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error updating project: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onDeleteBuilding(Building building) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Project")
                .setMessage("Are you sure you want to delete \"" + building.getName() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.collection("buildings").document(building.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Project deleted successfully", Toast.LENGTH_SHORT).show();
                                loadBuildings();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Error deleting project: " + e.getMessage(), 
                                        Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onEditBuilding(Building building) {
        showAddBuildingDialog(building);
    }

    @Override
    public void onCopyAddress(Building building) {
        String addressToCopy = building.getFullAddress();
        
        if (addressToCopy == null || addressToCopy.trim().isEmpty()) {
            // If no full address, create one from available parts
            StringBuilder address = new StringBuilder();
            if (building.getStreet() != null && !building.getStreet().trim().isEmpty()) {
                address.append(building.getStreet());
            }
            if (building.getCity() != null && !building.getCity().trim().isEmpty()) {
                if (address.length() > 0) address.append(", ");
                address.append(building.getCity());
            }
            if (building.getCode() != null && !building.getCode().trim().isEmpty()) {
                if (address.length() > 0) address.append(" ");
                address.append(building.getCode());
            }
            addressToCopy = address.toString();
        }
        
        if (!addressToCopy.trim().isEmpty()) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Building Address", addressToCopy);
            clipboard.setPrimaryClip(clip);
            
            Toast.makeText(this, "Address copied to clipboard!\nYou can paste it in Google Maps", 
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "No address available to copy", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onOpenMaps(Building building) {
        String mapsUrl = building.getGoogleMapsUrl();
        if (mapsUrl != null && !mapsUrl.trim().isEmpty()) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mapsUrl));
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Could not open Google Maps", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No Google Maps URL set for this project.\nEdit the project to add Maps URL.", 
                    Toast.LENGTH_LONG).show();
        }
    }

    private void showLoading(boolean show) {
        progressIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerViewBuildings.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void updateEmptyState() {
        if (buildingsList.isEmpty()) {
            textEmptyState.setVisibility(View.VISIBLE);
            recyclerViewBuildings.setVisibility(View.GONE);
        } else {
            textEmptyState.setVisibility(View.GONE);
            recyclerViewBuildings.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBuildingClick(Building building) {
        // Открываем детальную информацию о здании
        Intent intent = new Intent(this, BuildingDetailActivity.class);
        intent.putExtra(BuildingDetailActivity.EXTRA_BUILDING, building);
        startActivityForResult(intent, 100); // Код запроса для получения результата
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            String action = data.getStringExtra("action");
            if ("edit".equals(action)) {
                Building buildingToEdit = (Building) data.getSerializableExtra("building");
                if (buildingToEdit != null) {
                    showAddBuildingDialog(buildingToEdit);
                }
            }
        }
    }
}


