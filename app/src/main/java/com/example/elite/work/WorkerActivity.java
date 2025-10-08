package com.example.elite.work;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.elite.R;
import com.example.elite.apps.AppsActivity;
import com.example.elite.auth.Login;
import com.example.elite.profile.Profile;
import com.example.elite.workhours.HoursFragmentWorker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

public class WorkerActivity extends AppCompatActivity {
    StorageReference storageReference;
    LinearProgressIndicator progress;
    Uri image , video;
    MaterialButton selectImage, uploadImage, selectVideo , uploadVideo;
    ImageView imageView;
    FirebaseAuth auth;
    FirebaseUser user;
    Button button_logOut;
    TextView textView;
    BottomNavigationView bottomNavigationView;

    private final ActivityResultLauncher<Intent> activityResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult result) {
                            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                                image = result.getData().getData();
                                video = result.getData().getData();
                                uploadImage.setEnabled(true);
                                Glide.with(getApplicationContext()).load(image).into(imageView);
                                Glide.with(getApplicationContext()).load(video).into(imageView);
                            } else {
                                Toast.makeText(WorkerActivity.this, "Please select an image", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker);

        storageReference = FirebaseStorage.getInstance().getReference();
        auth = FirebaseAuth.getInstance();
        button_logOut = findViewById(R.id.button_logout_worker);
        textView = findViewById(R.id.textview_user_email);
        user = auth.getCurrentUser();

        if (user == null){
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        }else{
            textView.setText(user.getEmail());
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

        Toolbar toolbar = findViewById(R.id.toolbar_worker);
        setSupportActionBar(toolbar);

        progress = findViewById(R.id.progress_worker);
        imageView = findViewById(R.id.imageview_worker);
        selectImage = findViewById(R.id.button_select_image_worker);
        uploadImage = findViewById(R.id.button_upload_image_worker);
        selectVideo = findViewById(R.id.button_select_video);
        uploadVideo = findViewById(R.id.button_upload_video);

        // Initialize Bottom Navigation
        bottomNavigationView = findViewById(R.id.bottom_navigation_worker);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, Profile.class));
                return true;
            } else if (itemId == R.id.nav_work) {
                // Already on work page
                return true;
            } else if (itemId == R.id.nav_work_hour) {
                startActivity(new Intent(this, HoursFragmentWorker.class));
                return true;
            } else if (itemId == R.id.nav_apps) {
                startActivity(new Intent(this, AppsActivity.class));
                return true;
            }
            return false;
        });

        // Set default selection
        bottomNavigationView.setSelectedItemId(R.id.nav_work);

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

        selectVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("video/*");
                activityResultLauncher.launch(intent);
            }
        });

        uploadVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadVideo(video);
            }
        });




    }

    private void uploadImage(Uri image) {
        StorageReference reference = storageReference.child("images/" + UUID.randomUUID().toString());
        reference.putFile(image).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(WorkerActivity.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(WorkerActivity.this, "There was an error while uploading", Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                progress.setMax(Math.toIntExact(snapshot.getTotalByteCount()));
                progress.setProgress(Math.toIntExact(snapshot.getTotalByteCount()));


            }
        });
    }

    private void uploadVideo(Uri video) {
        StorageReference reference = storageReference.child("videos/" + UUID.randomUUID().toString());
        reference.putFile(video).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(WorkerActivity.this, "Video uploaded successfully", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(WorkerActivity.this, "There was an error while uploading", Toast.LENGTH_SHORT).show();
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
