package com.example.elite;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.UUID;

public class DirectorActivity extends AppCompatActivity {
    //TODO имплементировать класс
    StorageReference storageReference;
    LinearProgressIndicator progress;
    Uri image;
    MaterialButton selectImage, uploadImage;
    ImageView imageView;
    FirebaseAuth auth;
    FirebaseUser user;
    Button button_logOut;

    ListView userList;
    ArrayAdapter<String> adapter;
    ArrayList<String> users;

    FirebaseFirestore db;

    private final ActivityResultLauncher<Intent> activityResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult result) {
                            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                                image = result.getData().getData();
                                uploadImage.setEnabled(true);
                                Glide.with(getApplicationContext()).load(image).into(imageView);
                            } else {
                                Toast.makeText(DirectorActivity.this, "Please select an image", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_director);

        storageReference = FirebaseStorage.getInstance().getReference();
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = auth.getCurrentUser();

        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        }

        button_logOut = findViewById(R.id.button_logout_director);
        button_logOut.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        });

        progress = findViewById(R.id.progress_d);
        imageView = findViewById(R.id.imageview_director);
        selectImage = findViewById(R.id.button_select_image_director);
        uploadImage = findViewById(R.id.button_upload_image_director);
        userList = findViewById(R.id.recyclerview_users);

        users = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, users);
        userList.setAdapter(adapter);

        // загрузка списка пользователей
        loadUsers();

        selectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            activityResultLauncher.launch(intent);
        });

        uploadImage.setOnClickListener(v -> {
            if (image != null) {
                uploadImage(image);
            }
        });
    }

    private void loadUsers() {
        db.collection("users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                users.clear();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    String email = doc.getString("email");
                    String role = doc.getString("role");
                    users.add(email + " (" + role + ")");
                }
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "Error loading users", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadImage(Uri image) {
        StorageReference reference = storageReference.child("images/" + UUID.randomUUID().toString());
        reference.putFile(image)
                .addOnSuccessListener(taskSnapshot -> Toast.makeText(DirectorActivity.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(DirectorActivity.this, "Error uploading: " + e.getMessage(), Toast.LENGTH_SHORT).show())
                .addOnProgressListener((OnProgressListener<UploadTask.TaskSnapshot>) snapshot -> {
                    long progressPercent = (100 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    progress.setProgress((int) progressPercent);
                });
    }
}


