package com.example.elite;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class BuildingDetailActivity extends AppCompatActivity {
    
    public static final String EXTRA_BUILDING = "extra_building";
    
    private Building building;
    private TextView textProjectName, textStreet, textCity, textPostalCode, textGoogleMapsUrl;
    private MaterialButton buttonCopyAddress, buttonOpenMaps, buttonEdit;
    private MaterialToolbar toolbar;
    private MaterialCardView mapsCard;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_building_detail);
        
        initViews();
        getBuildingFromIntent();
        setupToolbar();
        setupClickListeners();
        displayBuildingInfo();
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        textProjectName = findViewById(R.id.text_project_name);
        textStreet = findViewById(R.id.text_street);
        textCity = findViewById(R.id.text_city);
        textPostalCode = findViewById(R.id.text_postal_code);
        textGoogleMapsUrl = findViewById(R.id.text_google_maps_url);
        buttonCopyAddress = findViewById(R.id.button_copy_address);
        buttonOpenMaps = findViewById(R.id.button_open_maps);
        buttonEdit = findViewById(R.id.button_edit);
        mapsCard = findViewById(R.id.maps_card);
    }
    
    private void getBuildingFromIntent() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_BUILDING)) {
            building = (Building) intent.getSerializableExtra(EXTRA_BUILDING);
        }
        
        if (building == null) {
            Toast.makeText(this, "Error loading building details", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Project Details");
        }
        
        toolbar.setNavigationOnClickListener(v -> finish());
    }
    
    private void setupClickListeners() {
        buttonCopyAddress.setOnClickListener(v -> copyAddressToClipboard());
        buttonOpenMaps.setOnClickListener(v -> openGoogleMaps());
        buttonEdit.setOnClickListener(v -> editBuilding());
    }
    
    private void displayBuildingInfo() {
        if (building == null) return;
        
        textProjectName.setText(building.getName() != null ? building.getName() : "N/A");
        textStreet.setText(building.getStreet() != null ? building.getStreet() : "Not specified");
        textCity.setText(building.getCity() != null ? building.getCity() : "Not specified");
        textPostalCode.setText(building.getCode() != null ? building.getCode() : "Not specified");
        
        // Обработка Google Maps URL
        if (building.hasValidGoogleMapsUrl()) {
            textGoogleMapsUrl.setText(building.getGoogleMapsUrl());
            mapsCard.setVisibility(View.VISIBLE);
            buttonOpenMaps.setVisibility(View.VISIBLE);
        } else {
            textGoogleMapsUrl.setText("No Google Maps URL set");
            mapsCard.setVisibility(View.VISIBLE);
            buttonOpenMaps.setVisibility(View.GONE);
        }
    }
    
    private void copyAddressToClipboard() {
        String fullAddress = getFullAddress();
        
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Building Address", fullAddress);
        clipboard.setPrimaryClip(clip);
        
        Toast.makeText(this, "Address copied to clipboard!", Toast.LENGTH_SHORT).show();
    }
    
    private String getFullAddress() {
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
        
        return address.length() > 0 ? address.toString() : "Address not available";
    }
    
    private void openGoogleMaps() {
        if (building != null && building.hasValidGoogleMapsUrl()) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(building.getGoogleMapsUrl()));
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Cannot open Google Maps", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No Google Maps URL available", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void editBuilding() {
        // Возвращаемся к DirectorActivity с результатом для редактирования
        Intent resultIntent = new Intent();
        resultIntent.putExtra("action", "edit");
        resultIntent.putExtra("building", building);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}