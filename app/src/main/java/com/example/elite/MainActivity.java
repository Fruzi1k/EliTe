package com.example.elite;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.elite.auth.Login;
import com.example.elite.models.User;
import com.example.elite.profile.Profile;
import com.example.elite.work.DirectorActivity;
import com.example.elite.work.WorkerActivity;
import com.example.elite.workhours.WorkHourActivity;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    StorageReference storageReference;
    LinearProgressIndicator progress;
    Uri image;
    MaterialButton selectImage, uploadImage;
    ImageView imageView;
    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseFirestore db;
    Button button_logOut;
    TextView textView;
    BottomNavigationView bottomNavigationView;


    private  final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if(result.getResultCode() == RESULT_OK){

                if (result.getData() != null) {
                    image = result.getData().getData();
                    uploadImage.setEnabled(true);
                    Glide.with(getApplicationContext()).load(image).into(imageView);
                }
            }else{
                Toast.makeText(MainActivity.this,"Please select an image", Toast.LENGTH_SHORT).show();
            }
        }
    });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });

        FirebaseApp.initializeApp( MainActivity.this);
        storageReference = FirebaseStorage.getInstance().getReference();
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        button_logOut = findViewById(R.id.button_LogOut);
        textView = findViewById(R.id.user_details);
        user = auth.getCurrentUser();
        if (user == null){
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        }else{
            textView.setText(user.getEmail());
            // Проверяем роль пользователя и перенаправляем соответственно
            checkUserRoleAndRedirect();
        }
        button_logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progress = findViewById(R.id.progress);
        imageView = findViewById(R.id.imageView);
        selectImage = findViewById(R.id.button_Login);
        uploadImage = findViewById(R.id.button_uploadImage);

        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                activityResultLauncher.launch(intent);
            }
        });
        
        uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage(image);
            }
        });

        // Initialize Bottom Navigation
        bottomNavigationView = findViewById(R.id.bottom_navigation_main);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_profile) {
                redirectToProfile();
                return true;
            } else if (itemId == R.id.nav_work) {
                // Navigate to work section based on user role
                navigateToWorkSection();
                return true;
            } else if (itemId == R.id.nav_work_hour) {
                startActivity(new Intent(this, WorkHourActivity.class));
                finish();
                return true;
            }
            return false;
        });

        // Set default selection
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);

    }

    private void redirectToProfile() {
        Intent intent = new Intent(this, Profile.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void checkUserRoleAndRedirect() {
        if (user == null) return;
        
        Log.d("MainActivity", "Checking user role for UID: " + user.getUid());
        
        // Load user role from Firestore and navigate accordingly
        db.collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Log.d("MainActivity", "User document exists");
                        Log.d("MainActivity", "Document data: " + documentSnapshot.getData());
                        
                        User currentUser = documentSnapshot.toObject(User.class);
                        if (currentUser != null) {
                            Log.d("MainActivity", "User position: " + currentUser.getPosition());
                            Log.d("MainActivity", "Is director: " + currentUser.isDirector());
                            
                            Intent intent;
                            if (currentUser.isDirector()) {
                                Log.d("MainActivity", "Redirecting to DirectorActivity");
                                intent = new Intent(this, DirectorActivity.class);
                            } else {
                                Log.d("MainActivity", "Redirecting to WorkerActivity");
                                intent = new Intent(this, WorkerActivity.class);
                            }
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            Log.e("MainActivity", "User object is null");
                            // Fallback to Profile if user object is null
                            redirectToProfile();
                        }
                    } else {
                        Log.e("MainActivity", "User document does not exist");
                        // Fallback to Profile if no user document found
                        redirectToProfile();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("MainActivity", "Error loading user role", e);
                    Toast.makeText(this, "Error loading user role", Toast.LENGTH_SHORT).show();
                    // Fallback to Profile on error
                    redirectToProfile();
                });
    }

    private void navigateToWorkSection() {
        if (user == null) return;
        
        // Load user role from Firestore and navigate accordingly
        db.collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User currentUser = documentSnapshot.toObject(User.class);
                        if (currentUser != null) {
                            Intent intent;
                            if (currentUser.isDirector()) {
                                intent = new Intent(this, DirectorActivity.class);
                            } else {
                                intent = new Intent(this, WorkerActivity.class);
                            }
                            startActivity(intent);
                        }
                    } else {
                        // Default to WorkerActivity if no role found
                        startActivity(new Intent(this, WorkerActivity.class));
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading user role", Toast.LENGTH_SHORT).show();
                    // Default to WorkerActivity on error
                    startActivity(new Intent(this, WorkerActivity.class));
                });
    }

    private void uploadImage(Uri image) {
        StorageReference reference = storageReference.child("images/" + UUID.randomUUID().toString());
        reference.putFile(image).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(MainActivity.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "There was an errorwhile uploading", Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                progress.setMax(Math.toIntExact(snapshot.getTotalByteCount()));
                progress.setProgress(Math.toIntExact(snapshot.getTotalByteCount()));


            }
        });
    }
}