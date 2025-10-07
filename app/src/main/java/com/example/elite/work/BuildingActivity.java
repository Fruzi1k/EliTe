package com.example.elite.work;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.elite.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class BuildingActivity extends AppCompatActivity {
    TextView nameView, addressView;
    ImageButton mapBtn;
    RecyclerView recyclerFolders;
    FloatingActionButton fab;

    String buildingName;
    double lat, lng;
    String address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_building);

        nameView = findViewById(R.id.buildingNameDetail);
        addressView = findViewById(R.id.buildingAddressDetail);
        mapBtn = findViewById(R.id.mapButton);
        recyclerFolders = findViewById(R.id.recyclerFolders);
        fab = findViewById(R.id.fabAdd);

        Intent intent = getIntent();
        buildingName = intent.getStringExtra("name");
        address = intent.getStringExtra("address");
        lat = intent.getDoubleExtra("lat", 0);
        lng = intent.getDoubleExtra("lng", 0);

        nameView.setText(buildingName);
        addressView.setText(address);

        mapBtn.setOnClickListener(v -> {
            Uri gmmIntentUri = Uri.parse("geo:" + lat + "," + lng + "?q=" + Uri.encode(address));
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        });

        // TODO: здесь выводим "Фото" и "Видео" в RecyclerView
        // TODO: по нажатию на "Фото" или "Видео" открываем список файлов из Firebase Storage

        fab.setOnClickListener(v -> {
            // TODO: диалог выбора: загрузить фото или видео
            Toast.makeText(this, "Добавить фото/видео", Toast.LENGTH_SHORT).show();
        });
    }
}
