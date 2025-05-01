package com.nhlstenden.navigationapp.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.nhlstenden.navigationapp.R;

public class CreateWaypointActivity extends AppCompatActivity {

    private EditText etName, etDescription;
    private ImageView imagePreview;
    private Uri imageUri;

    private String mode;
    private String id;
    private double lat;
    private double lng;

    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    imageUri = uri;
                    imagePreview.setImageURI(uri);
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_waypoint);

        etName = findViewById(R.id.etName);
        etDescription = findViewById(R.id.etDescription);
        imagePreview = findViewById(R.id.imagePreview);
        Button btnChooseImage = findViewById(R.id.btnChooseImage);
        Button btnSave = findViewById(R.id.btnSaveWaypoint);
        Button btnCancel = findViewById(R.id.btnCancel);

        Intent intent = getIntent();
        mode = intent.getStringExtra("mode");
        id = intent.getStringExtra("id");
        lat = intent.getDoubleExtra("lat", 0.0);
        lng = intent.getDoubleExtra("lng", 0.0);

        if ("edit".equals(mode)) {
            etName.setText(intent.getStringExtra("name"));
            etDescription.setText(intent.getStringExtra("description"));
            String imageUriString = intent.getStringExtra("imageUri");
            if (imageUriString != null) {
                imageUri = Uri.parse(imageUriString);
                imagePreview.setImageURI(imageUri);
            }
        }

        btnChooseImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        btnSave.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("id", id);
            resultIntent.putExtra("name", etName.getText().toString());
            resultIntent.putExtra("description", etDescription.getText().toString());
            resultIntent.putExtra("imageUri", imageUri != null ? imageUri.toString() : null);
            resultIntent.putExtra("lat", lat);
            resultIntent.putExtra("lng", lng);
            setResult(RESULT_OK, resultIntent);
            finish();
        });

        btnCancel.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });
    }
}
