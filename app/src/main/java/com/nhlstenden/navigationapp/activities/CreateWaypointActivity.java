package com.nhlstenden.navigationapp.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.nhlstenden.navigationapp.BaseActivity;
import com.nhlstenden.navigationapp.R;
import com.nhlstenden.navigationapp.models.Waypoint;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CreateWaypointActivity extends BaseActivity {

    private static final int MAX_DESCRIPTION_LENGTH = 500;

    private EditText etName, etDescription;
    private ImageView imagePreview;
    private String mode, id;
    private double lat, lng;
    private Uri imageUri = Uri.EMPTY;
    private String originalDate;

    private ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_waypoint);

        // ✅ Set title
        TextView headerTitle = findViewById(R.id.headerTitle);
        if (headerTitle != null) {
            headerTitle.setText("Treasure Creation");
        }

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        imageUri = uri;

                        String internalPath = copyImageToInternalStorage(uri);
                        if (internalPath != null) {
                            imageUri = Uri.fromFile(new File(internalPath));
                            imagePreview.setImageURI(imageUri);
                        } else {
                            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

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
            if (headerTitle != null) {
                headerTitle.setText("Edit Waypoint");
            }

            Waypoint w = intent.getParcelableExtra("WAYPOINT");
            if (w != null) {
                etName.setText(w.getName());
                etDescription.setText(w.getDescription());
                lat = w.getLat();
                lng = w.getLng();
                imageUri = w.getImageUri() != null ? Uri.parse(w.getImageUri()) : Uri.EMPTY;
                originalDate = w.getDate();
                if (imageUri != Uri.EMPTY) {
                    Glide.with(this).load(imageUri).into(imagePreview);
                }
                btnMap.setEnabled(false);
                btnMap.setAlpha(0.5f);
            }
        }

        btnChooseImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        btnMap.setOnClickListener(v -> {
            Intent mapIntent = new Intent(this, MapActivity.class);
            if (lat != 0.0 && lng != 0.0) {
                mapIntent.putExtra("lat", lat);
                mapIntent.putExtra("lng", lng);
            }
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

                Waypoint resultWaypoint = new Waypoint(id, name, description, imageUri.toString(), lat, lng);
                if ("edit".equals(mode) && originalDate != null) {
                    resultWaypoint.setDate(originalDate);
                }

                Intent resultIntent = new Intent();
                resultIntent.putExtra("WAYPOINT", resultWaypoint);
                resultIntent.putExtra("mode", mode);
                resultIntent.putExtra("imageUri", imageUri != null ? imageUri.toString() : null);
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

        if (lat == 0.0 && lng == 0.0) {
            Toast.makeText(this, "Please select a location on the map first", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private String copyImageToInternalStorage(Uri sourceUri) {
        try (InputStream in = getContentResolver().openInputStream(sourceUri)) {
            File file = new File(getFilesDir(), "img_" + System.currentTimeMillis() + ".jpg");
            try (OutputStream out = new FileOutputStream(file)) {
                byte[] buffer = new byte[4096];
                int len;
                while ((len = in.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                }
            }
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private final ActivityResultLauncher<Intent> mapLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            Intent data = result.getData();
                            if (data.hasExtra("lat") && data.hasExtra("lng")) {
                                lat = data.getDoubleExtra("lat", 0.0);
                                lng = data.getDoubleExtra("lng", 0.0);
                                Toast.makeText(this,
                                        String.format("Location updated: %.6f, %.6f", lat, lng),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
}
