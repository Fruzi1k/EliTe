package com.example.elite;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BuildingDetailActivity extends AppCompatActivity {
    
    public static final String EXTRA_BUILDING = "extra_building";
    
    private Building building;
    private TextView textProjectName, textStreet, textCity, textPostalCode, textGoogleMapsUrl;
    private MaterialButton buttonCopyAddress, buttonOpenMaps, buttonEdit;
    private MaterialToolbar toolbar;
    private MaterialCardView mapsCard;
    private FloatingActionButton fabAddMedia;
    
    // Firebase Storage
    private StorageReference storageReference;
    
    // Firebase Auth and Firestore for user information
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private LinearProgressIndicator progressIndicator;
    private Uri selectedFileUri;
    
    // Media components
    private RecyclerView recyclerMedia;
    private MediaAdapter mediaAdapter;
    private TextView textNoMedia, textMediaCount;
    private List<MediaItem> mediaList;
    
    // Activity Result Launchers
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<Intent> videoPickerLauncher;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_building_detail);
        
        // Initialize Firebase Storage
        storageReference = FirebaseStorage.getInstance().getReference();
        
        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        
        initViews();
        setupActivityResultLaunchers();
        getBuildingFromIntent();
        setupToolbar();
        setupClickListeners();
        setupMediaRecyclerView();
        displayBuildingInfo();
        loadMediaFiles();
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
        fabAddMedia = findViewById(R.id.fab_add_media);
        progressIndicator = findViewById(R.id.progress_indicator);
        
        // Media views
        recyclerMedia = findViewById(R.id.recycler_media);
        textNoMedia = findViewById(R.id.text_no_media);
        textMediaCount = findViewById(R.id.text_media_count);
        
        Log.d("BuildingDetail", "Media views initialized: " + 
            (recyclerMedia != null) + ", " + 
            (textNoMedia != null) + ", " + 
            (textMediaCount != null));
        
        mediaList = new ArrayList<>();
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
    
    private void setupActivityResultLaunchers() {
        // Image picker launcher
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            selectedFileUri = result.getData().getData();
                            if (selectedFileUri != null) {
                                uploadMedia(selectedFileUri, "images");
                            }
                        }
                    }
                }
        );
        
        // Video picker launcher
        videoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            selectedFileUri = result.getData().getData();
                            if (selectedFileUri != null) {
                                uploadMedia(selectedFileUri, "videos");
                            }
                        }
                    }
                }
        );
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
        fabAddMedia.setOnClickListener(v -> showMediaChoiceDialog());
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
    
    private void showMediaChoiceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Media to Project")
                .setMessage("Choose the type of media you want to upload:")
                .setPositiveButton("Image", (dialog, which) -> selectImage())
                .setNegativeButton("Video", (dialog, which) -> selectVideo())
                .setNeutralButton("Cancel", null)
                .show();
    }
    
    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }
    
    private void selectVideo() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*");
        videoPickerLauncher.launch(intent);
    }
    
    private void uploadMedia(Uri fileUri, String mediaType) {
        if (fileUri == null || building == null) {
            Toast.makeText(this, "Error: No file selected", Toast.LENGTH_SHORT).show();
            return;
        }
        
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Error: User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show progress
        if (progressIndicator != null) {
            progressIndicator.setVisibility(View.VISIBLE);
        }
        
        // Get user information from Firestore
        firestore.collection("users").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String firstName = documentSnapshot.getString("firstName");
                    String lastName = documentSnapshot.getString("lastName");
                    
                    // Generate filename with user info and timestamp
                    String fileName = generateFileName(lastName, firstName);
                    
                    // Create folder structure: projectName/images or videos/fileName
                    String folderType = "images".equals(mediaType) ? "images" : "videos";
                    String cleanProjectName = cleanFileName(building.getName());
                    String path = cleanProjectName + "/" + folderType + "/" + fileName;
                    StorageReference reference = storageReference.child(path);
                    
                    // Upload file
                    reference.putFile(fileUri)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    if (progressIndicator != null) {
                                        progressIndicator.setVisibility(View.GONE);
                                    }
                                    String mediaTypeText = "images".equals(mediaType) ? "Image" : "Video";
                                    Toast.makeText(BuildingDetailActivity.this, 
                                            mediaTypeText + " uploaded successfully!", Toast.LENGTH_SHORT).show();
                                    
                                    // Refresh media list
                                    loadMediaFiles();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    if (progressIndicator != null) {
                                        progressIndicator.setVisibility(View.GONE);
                                    }
                                    Toast.makeText(BuildingDetailActivity.this, 
                                            "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                                    if (progressIndicator != null) {
                                        double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                                        progressIndicator.setProgress((int) progress);
                                    }
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    if (progressIndicator != null) {
                        progressIndicator.setVisibility(View.GONE);
                    }
                    Toast.makeText(this, "Error getting user information: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    
    private String generateFileName(String lastName, String firstName) {
        // Generate timestamp in format: date (DDMMYYYY) + time (HHMMSS)
        SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HHmmss", Locale.getDefault());
        
        Date now = new Date();
        String date = dateFormat.format(now);
        String time = timeFormat.format(now);
        
        // Handle null values and clean names
        String safeLastName = cleanFileName(lastName != null && !lastName.trim().isEmpty() ? lastName.trim() : "Unknown");
        String safeFirstName = cleanFileName(firstName != null && !firstName.trim().isEmpty() ? firstName.trim() : "User");
        
        // Format: lastName_firstName_date_time
        return safeLastName + "_" + safeFirstName + "_" + date + "_" + time;
    }
    
    private String cleanFileName(String name) {
        // Remove invalid characters for file names and Firebase Storage
        // Replace spaces and invalid characters with underscores
        return name.replaceAll("[<>\"?*/\\\\:|\\s]+", "_");
    }
    
    private void setupMediaRecyclerView() {
        mediaAdapter = new MediaAdapter(mediaList, this, new MediaAdapter.OnMediaActionListener() {
            @Override
            public void onMediaClick(MediaItem mediaItem) {
                // Open media file
                openMediaFile(mediaItem);
            }

            @Override
            public void onMediaDelete(MediaItem mediaItem) {
                // Delete media file
                deleteMediaFile(mediaItem);
            }
        });
        
        recyclerMedia.setLayoutManager(new LinearLayoutManager(this));
        recyclerMedia.setAdapter(mediaAdapter);
    }

    private void loadMediaFiles() {
        if (building == null) return;
        
        mediaList.clear();
        String projectName = cleanFileName(building.getName());
        
        Log.d("BuildingDetail", "Loading media for project: " + projectName);
        Log.d("BuildingDetail", "Original project name: " + building.getName());
        
        // Load images
        loadMediaFromFolder(projectName + "/images", "image");
        
        // Load videos
        loadMediaFromFolder(projectName + "/videos", "video");
        
        // Show media section immediately
        updateMediaDisplay();
    }

    private void loadMediaFromFolder(String folderPath, String mediaType) {
        StorageReference folderRef = storageReference.child(folderPath);
        
        Log.d("BuildingDetail", "Loading from folder: " + folderPath);
        
        folderRef.listAll()
            .addOnSuccessListener(listResult -> {
                Log.d("BuildingDetail", "Found " + listResult.getItems().size() + " items in " + folderPath);
                for (StorageReference item : listResult.getItems()) {
                    // Get download URL and metadata
                    item.getDownloadUrl().addOnSuccessListener(uri -> {
                        item.getMetadata().addOnSuccessListener(metadata -> {
                            String fileName = item.getName();
                            String uploadedBy = extractUploadedBy(fileName);
                            long uploadTime = metadata.getCreationTimeMillis();
                            long fileSize = metadata.getSizeBytes();
                            
                            MediaItem mediaItem = new MediaItem(
                                fileName,
                                uri.toString(),
                                mediaType,
                                uploadedBy,
                                uploadTime,
                                fileSize
                            );
                            
                            mediaList.add(mediaItem);
                            updateMediaDisplay();
                        });
                    });
                }
            })
            .addOnFailureListener(e -> {
                Log.e("BuildingDetail", "Error loading media files from " + folderPath + ": " + e.getMessage(), e);
            });
    }

    private String extractUploadedBy(String fileName) {
        // Extract user name from filename (lastName_firstName_date_time)
        try {
            String[] parts = fileName.split("_");
            if (parts.length >= 2) {
                return parts[1] + " " + parts[0]; // firstName lastName
            }
        } catch (Exception e) {
            Log.e("BuildingDetail", "Error extracting user name from filename", e);
        }
        return "Unknown";
    }

    private void updateMediaDisplay() {
        runOnUiThread(() -> {
            Log.d("BuildingDetail", "Updating media display. Media count: " + mediaList.size());
            if (mediaList.isEmpty()) {
                textNoMedia.setVisibility(View.VISIBLE);
                recyclerMedia.setVisibility(View.GONE);
                textMediaCount.setText("0 files");
                Log.d("BuildingDetail", "No media files found, showing empty state");
            } else {
                textNoMedia.setVisibility(View.GONE);
                recyclerMedia.setVisibility(View.VISIBLE);
                textMediaCount.setText(mediaList.size() + " files");
                mediaAdapter.updateMediaList(mediaList);
                Log.d("BuildingDetail", "Showing " + mediaList.size() + " media files");
            }
        });
    }

    private void openMediaFile(MediaItem mediaItem) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(mediaItem.getDownloadUrl()));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Cannot open media file", Toast.LENGTH_SHORT).show();
            Log.e("BuildingDetail", "Error opening media", e);
        }
    }

    private void deleteMediaFile(MediaItem mediaItem) {
        new AlertDialog.Builder(this)
            .setTitle("Delete Media")
            .setMessage("Are you sure you want to delete this file?")
            .setPositiveButton("Delete", (dialog, which) -> {
                // Delete file from Storage
                String projectName = cleanFileName(building.getName());
                String filePath = projectName + "/" + (mediaItem.isImage() ? "images" : "videos") + "/" + mediaItem.getFileName();
                
                storageReference.child(filePath).delete()
                    .addOnSuccessListener(aVoid -> {
                        mediaList.remove(mediaItem);
                        updateMediaDisplay();
                        Toast.makeText(this, "File deleted successfully", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to delete file", Toast.LENGTH_SHORT).show();
                        Log.e("BuildingDetail", "Error deleting file", e);
                    });
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}