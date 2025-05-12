package com.nhlstenden.navigationapp.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.nhlstenden.navigationapp.R;
import com.nhlstenden.navigationapp.models.Waypoint;

public class CreateWaypointActivity extends AppCompatActivity {

    private static final int MAX_NAME_LENGTH = 50;
    private static final int MAX_DESCRIPTION_LENGTH = 500;

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
            Waypoint wp = intent.getParcelableExtra("WAYPOINT");
            if (wp != null) {
                etName.setText(wp.getName());
                etDescription.setText(wp.getDescription());
                imageUri = wp.getImageUri() != null ? Uri.parse(wp.getImageUri()) : null;
                if (imageUri != null) {
                    imagePreview.setImageURI(imageUri);
                }
                id = wp.getId();
                lat = wp.getLat();
                lng = wp.getLng();
            }
        }

        btnChooseImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        btnSave.setOnClickListener(v -> {
            if (validateInput()) {
                String name = etName.getText().toString().trim();
                String description = etDescription.getText().toString().trim();

                // Truncate description if it exceeds maximum length
                if (description.length() > MAX_DESCRIPTION_LENGTH) {
                    description = description.substring(0, MAX_DESCRIPTION_LENGTH);
                    Toast.makeText(this, "Description truncated to 500 characters", Toast.LENGTH_SHORT).show();
                }

                Intent resultIntent = new Intent();

                Waypoint resultWaypoint = new Waypoint(
                        id,
                        name,
                        description,
                        imageUri != null ? imageUri.toString() : null,
                        lat,
                        lng
                );

                resultIntent.putExtra("WAYPOINT", resultWaypoint);
                setResult(RESULT_OK, resultIntent);

                Toast.makeText(this, "Waypoint saved!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        btnCancel.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });
    }

    private boolean validateInput() {
        String name = etName.getText().toString().trim();

        // Check if name is empty
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Error: Waypoint name cannot be empty", Toast.LENGTH_SHORT).show();
            etName.requestFocus();
            return false;
        }

        // Check if name exceeds maximum length
        if (name.length() >= MAX_NAME_LENGTH) {
            Toast.makeText(this, "Error: Name must be less than 50 characters", Toast.LENGTH_SHORT).show();
            etName.requestFocus();
            return false;
        }

        return true;
    }
}