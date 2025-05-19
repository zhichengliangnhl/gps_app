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

import com.bumptech.glide.Glide;
import com.nhlstenden.navigationapp.R;
import com.nhlstenden.navigationapp.activities.MapActivity;
import com.nhlstenden.navigationapp.models.Waypoint;

public class CreateWaypointActivity extends AppCompatActivity {
    private static final int MAX_DESCRIPTION_LENGTH = 500;

    private EditText etName, etDescription;
    private ImageView imagePreview;
    private String mode, id;
    private double lat, lng;
    private Uri imageUri = Uri.EMPTY;

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
        Button btnMap = findViewById(R.id.btnMap);

        Intent intent = getIntent();
        mode = intent.getStringExtra("mode");
        id = intent.getStringExtra("id");
        lat = intent.getDoubleExtra("lat", 0.0);
        lng = intent.getDoubleExtra("lng", 0.0);

        if ("edit".equals(mode)) {
            Waypoint w = intent.getParcelableExtra("WAYPOINT");
            if (w != null) {
                etName.setText(w.getName());
                etDescription.setText(w.getDescription());
                lat = w.getLat();
                lng = w.getLng();
                imageUri = w.getImageUri() != null ? Uri.parse(w.getImageUri()) : Uri.EMPTY;
                if (imageUri != Uri.EMPTY) {
                    Glide.with(this).load(imageUri).into(imagePreview);
                }
            }
        }

        btnChooseImage.setOnClickListener(v -> {
            selectImageLauncher.launch("image/*");
        });

        btnMap.setOnClickListener(v -> {
            Intent mapIntent = new Intent(this, MapActivity.class);
            mapLauncher.launch(mapIntent);
        });

        btnSave.setOnClickListener(v -> {
            if (validateInput()) {
                String name = etName.getText().toString().trim();
                String description = etDescription.getText().toString().trim();
                if (description.length() > MAX_DESCRIPTION_LENGTH) {
                    description = description.substring(0, MAX_DESCRIPTION_LENGTH);
                    Toast.makeText(this, "Description truncated to 500 characters", Toast.LENGTH_SHORT).show();
                }
                Intent resultIntent = new Intent();
                Waypoint resultWaypoint = new Waypoint(id, name, description, imageUri.toString(), lat, lng);
                resultIntent.putExtra("WAYPOINT", resultWaypoint);
                resultIntent.putExtra("mode", mode);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });

        btnCancel.setOnClickListener(v -> finish());
    }

    private boolean validateInput() {
        if (TextUtils.isEmpty(etName.getText().toString().trim())) {
            Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(etDescription.getText().toString().trim())) {
            Toast.makeText(this, "Please enter a description", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private final ActivityResultLauncher<String> selectImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null) {
                            imageUri = uri;
                            Glide.with(this).load(imageUri).into(imagePreview);
                        }
                    });

    private final ActivityResultLauncher<Intent> mapLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {
                            lat = result.getData().getDoubleExtra("lat", 0.0);
                            lng = result.getData().getDoubleExtra("lng", 0.0);
                        }
                    });
}
