package com.nhlstenden.navigationapp.activities;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nhlstenden.navigationapp.BaseActivity;
import com.nhlstenden.navigationapp.R;
import com.nhlstenden.navigationapp.dialogs.IconSelectionDialog;
import com.nhlstenden.navigationapp.helpers.AchievementManager;
import com.nhlstenden.navigationapp.models.Waypoint;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CreateWaypointActivity extends BaseActivity {

    private MapView mapPreview;
    private GoogleMap previewMap;

    private static final int MAX_DESCRIPTION_LENGTH = 500;
    private double lat = 0.0;
    private double lng = 0.0;

    private EditText etName, etDescription;
    private Button btnSaveWaypoint, btnCancel;
    private ImageView imagePreview;
    private View imageClickOverlay;
    private String mode, id;
    private int selectedIconResId = R.drawable.icon1;
    private int selectedIconColor = Color.BLACK;
    private String originalDate;

    // Launcher for picking location on map
    private final ActivityResultLauncher<Intent> mapLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    if (data.hasExtra("lat") && data.hasExtra("lng")) {
                        lat = data.getDoubleExtra("lat", 0.0);
                        lng = data.getDoubleExtra("lng", 0.0);

                        Toast.makeText(this,
                                String.format("Location selected: %.6f, %.6f", lat, lng),
                                Toast.LENGTH_SHORT).show();

                        updateMapPreview(lat, lng);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_waypoint);

        // Initialize views
        TextView headerTitle = findViewById(R.id.headerTitle);
        etName = findViewById(R.id.etName);
        etDescription = findViewById(R.id.etDescription);
        btnSaveWaypoint = findViewById(R.id.btnSaveWaypoint);
        btnCancel = findViewById(R.id.btnCancel);
        mapPreview = findViewById(R.id.mapPreview);
        imagePreview = findViewById(R.id.imagePreview);
        imageClickOverlay = findViewById(R.id.imageClickOverlay);

        // Set initial icon
        imagePreview.setImageResource(selectedIconResId);
        imagePreview.setColorFilter(selectedIconColor);

        // Handle intent extras
        Intent intent = getIntent();
        mode = intent.getStringExtra("mode");
        id = intent.getStringExtra("id");

        // Set appropriate title
        if (headerTitle != null) {
            headerTitle.setText("edit".equals(mode) ? "Edit Treasure" : "Create Treasure");
        }

        // Restore state or initialize from intent
        if (savedInstanceState != null) {
            lat = savedInstanceState.getDouble("lat", 0.0);
            lng = savedInstanceState.getDouble("lng", 0.0);
            mode = savedInstanceState.getString("mode");
            id = savedInstanceState.getString("id");
            selectedIconResId = savedInstanceState.getInt("iconResId", R.drawable.icon1);
            selectedIconColor = savedInstanceState.getInt("iconColor", Color.BLACK);
            imagePreview.setImageResource(selectedIconResId);
            imagePreview.setColorFilter(selectedIconColor);
        } else if ("edit".equals(mode)) {
            Waypoint waypoint = intent.getParcelableExtra("WAYPOINT");
            if (waypoint != null) {
                etName.setText(waypoint.getName());
                etDescription.setText(waypoint.getDescription());
                lat = waypoint.getLat();
                lng = waypoint.getLng();
                originalDate = waypoint.getDate();
                selectedIconResId = waypoint.getIconResId();
                selectedIconColor = waypoint.getIconColor();
                imagePreview.setImageResource(selectedIconResId);
                imagePreview.setColorFilter(selectedIconColor);
            }
        } else {
            if (intent.hasExtra("lat") && intent.hasExtra("lng")) {
                lat = intent.getDoubleExtra("lat", 0.0);
                lng = intent.getDoubleExtra("lng", 0.0);
            }
        }

        // MapView setup
        mapPreview.onCreate(savedInstanceState);
        mapPreview.getMapAsync(googleMap -> {
            previewMap = googleMap;
            previewMap.getUiSettings().setAllGesturesEnabled(false);
            updateMapPreview(lat, lng);
        });

        // Set up click listeners
        findViewById(R.id.mapClickOverlay).setOnClickListener(v -> {
            Intent mapIntent = new Intent(CreateWaypointActivity.this, MapActivity.class);
            if (lat != 0.0 && lng != 0.0) {
                mapIntent.putExtra("lat", lat);
                mapIntent.putExtra("lng", lng);
            }
            mapLauncher.launch(mapIntent);
        });

        imageClickOverlay.setOnClickListener(v -> {
            IconSelectionDialog dialog = new IconSelectionDialog(this, (iconResId, color) -> {
                selectedIconResId = iconResId;
                selectedIconColor = color;
                imagePreview.setImageResource(selectedIconResId);
                imagePreview.setColorFilter(selectedIconColor);
            });
            dialog.show();
        });

        btnSaveWaypoint.setOnClickListener(v -> {
            if (!validateInput()) {
                return;
            }

            String name = etName.getText().toString().trim();
            String description = etDescription.getText().toString().trim();
            if (description.length() > MAX_DESCRIPTION_LENGTH) {
                description = description.substring(0, MAX_DESCRIPTION_LENGTH);
            }

            Waypoint resultWaypoint = new Waypoint(
                    id,
                    name,
                    description,
                    selectedIconResId,
                    selectedIconColor,
                    lat,
                    lng
            );

            if ("edit".equals(mode)) {
                resultWaypoint.setDate(originalDate);
            } else {
                // Update First Steps achievement when creating a new waypoint
                AchievementManager.updateFirstStepsProgress(this);
            }

            Intent resultIntent = new Intent();
            resultIntent.putExtra("WAYPOINT", resultWaypoint);
            resultIntent.putExtra("mode", mode);
            setResult(RESULT_OK, resultIntent);
            finish();
        });

        btnCancel.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });
    }

    private boolean validateInput() {
        if (TextUtils.isEmpty(etName.getText().toString().trim())) {
            Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (lat == 0.0 && lng == 0.0) {
            Toast.makeText(this, "Please select a location on the map", Toast.LENGTH_SHORT).show();
            return false;
        }

        String description = etDescription.getText().toString().trim();
        if (description.length() > MAX_DESCRIPTION_LENGTH) {
            Toast.makeText(this, "Description too long (max 500 characters)", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void updateMapPreview(double lat, double lng) {
        if (previewMap == null) return;

        previewMap.clear();
        if (lat != 0.0 && lng != 0.0) {
            LatLng location = new LatLng(lat, lng);
            previewMap.addMarker(new MarkerOptions().position(location).title("Selected Location"));
            previewMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
        }
    }

    // MapView lifecycle methods
    @Override
    protected void onResume() {
        super.onResume();
        mapPreview.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapPreview.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapPreview.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapPreview.onLowMemory();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapPreview.onSaveInstanceState(outState);
        outState.putDouble("lat", lat);
        outState.putDouble("lng", lng);
        outState.putString("mode", mode);
        outState.putString("id", id);
        outState.putInt("iconResId", selectedIconResId);
        outState.putInt("iconColor", selectedIconColor);
    }
}