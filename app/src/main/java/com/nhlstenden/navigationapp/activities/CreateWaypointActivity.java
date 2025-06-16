package com.nhlstenden.navigationapp.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.graphics.TypefaceCompatApi28Impl;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nhlstenden.navigationapp.BaseActivity;
import com.nhlstenden.navigationapp.R;
import com.nhlstenden.navigationapp.dialogs.IconSelectionDialog;
import com.nhlstenden.navigationapp.helpers.AchievementManager;
import com.nhlstenden.navigationapp.helpers.ToastUtils;
import com.nhlstenden.navigationapp.models.Waypoint;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

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
    private String selectedIconName = "icon1";
    private int selectedIconColor = Color.BLACK;
    private String originalDate;
    private boolean isEditMode = false;
    private Waypoint existingWaypoint = null;
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
        imagePreview.setImageResource(getResources().getIdentifier(selectedIconName, "drawable", getPackageName()));
        imagePreview.setColorFilter(selectedIconColor);

        // Handle intent extras
        mode = getIntent().getStringExtra("mode");
        isEditMode = "edit".equals(mode);
        id = getIntent().getStringExtra("id");

        boolean isImported;

        // Set appropriate title
        if (headerTitle != null) {
            headerTitle.setText("edit".equals(mode) ? "Edit Treasure" : "Create Treasure");
        }
        setupSettingsPanel();

        // Restore state or initialize from intent
        if (savedInstanceState != null) {
            isImported = false;
            lat = savedInstanceState.getDouble("lat", 0.0);
            lng = savedInstanceState.getDouble("lng", 0.0);
            mode = savedInstanceState.getString("mode");
            id = savedInstanceState.getString("id");
            selectedIconName = savedInstanceState.getString("iconName");
            selectedIconColor = savedInstanceState.getInt("iconColor", Color.BLACK);
            imagePreview.setImageResource(getResources().getIdentifier(selectedIconName, "drawable", getPackageName()));
            imagePreview.setColorFilter(selectedIconColor);
        } else if ("edit".equals(mode)) {
            existingWaypoint = getIntent().getParcelableExtra("WAYPOINT");
            if (existingWaypoint != null) {
                etName.setText(existingWaypoint.getName());
                etDescription.setText(existingWaypoint.getDescription());
                lat = existingWaypoint.getLat();
                lng = existingWaypoint.getLng();
                originalDate = existingWaypoint.getDate();
                isImported = existingWaypoint.isImported();
                selectedIconName = existingWaypoint.getIconName();
                selectedIconColor = existingWaypoint.getIconColor();
                imagePreview.setImageResource(getResources().getIdentifier(selectedIconName, "drawable", getPackageName()));
                imagePreview.setColorFilter(selectedIconColor);
            } else {
                isImported = false;
            }
        } else {
            isImported = false;
            if (getIntent().hasExtra("lat") && getIntent().hasExtra("lng")) {
                lat = getIntent().getDoubleExtra("lat", 0.0);
                lng = getIntent().getDoubleExtra("lng", 0.0);
            }
        }

        // MapView setup
        mapPreview.onCreate(savedInstanceState);
        mapPreview.getMapAsync(googleMap -> {
            previewMap = googleMap;
            previewMap.getUiSettings().setAllGesturesEnabled(false);
            updateMapPreview(lat, lng);
        });

        View ivCompass = findViewById(R.id.ivCompass);

        if (isImported) {
            mapPreview.setAlpha(0.6f);
            mapPreview.setVisibility(View.GONE);
            findViewById(R.id.mapClickOverlay).setVisibility(View.GONE);
            ivCompass.setVisibility(View.VISIBLE);
        } else {
            mapPreview.setVisibility(View.VISIBLE);
            findViewById(R.id.mapClickOverlay).setVisibility(View.VISIBLE);
            ivCompass.setVisibility(View.GONE);
            findViewById(R.id.mapClickOverlay).setOnClickListener(v -> {
                Intent mapIntent = new Intent(CreateWaypointActivity.this, MapActivity.class);
                if (lat != 0.0 && lng != 0.0) {
                    mapIntent.putExtra("lat", lat);
                    mapIntent.putExtra("lng", lng);
                }
                mapLauncher.launch(mapIntent);
            });
        }

        imageClickOverlay.setOnClickListener(v -> {
            IconSelectionDialog dialog = new IconSelectionDialog(this, selectedIconName, selectedIconColor, (iconName, color) -> {
                selectedIconName = iconName;
                selectedIconColor = color;
                imagePreview.setImageResource(getResources().getIdentifier(selectedIconName, "drawable", getPackageName()));
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

            // Always use the existing waypoint's ID when in edit mode
            String waypointId;
            String waypointDate;
            if (isEditMode && existingWaypoint != null) {
                waypointId = existingWaypoint.getId();
                waypointDate = existingWaypoint.getDate();
            } else {
                waypointId = UUID.randomUUID().toString();
                waypointDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                // Update First Steps achievement only for new waypoints
                AchievementManager.updateFirstStepsProgress(this);
            }

            Waypoint resultWaypoint = new Waypoint(
                    waypointId,
                    name,
                    description,
                    selectedIconName,
                    selectedIconColor,
                    lat,
                    lng
            );
            resultWaypoint.setDate(waypointDate);
            resultWaypoint.setImported(isImported);

            Intent resultIntent = new Intent();

            Log.d("WAYPOINT_RESULT", "Waypoint: " +  resultWaypoint);

            resultIntent.putExtra("WAYPOINT", resultWaypoint);
            resultIntent.putExtra("mode", mode);
            setResult(RESULT_OK, resultIntent);
            finish();
        });

        btnCancel.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        View topBar = findViewById(R.id.top_bar);
        View bottomNav = findViewById(R.id.bottom_nav_container);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            if (bottomNav != null) {
                bottomNav.setPadding(
                    bottomNav.getPaddingLeft(),
                    bottomNav.getPaddingTop(),
                    bottomNav.getPaddingRight(),
                    systemInsets.bottom
                );
            }
            return insets;
        });
    }

    private boolean validateInput() {
        if (TextUtils.isEmpty(etName.getText().toString().trim())) {
            ToastUtils.show(this, "Please enter a name", Toast.LENGTH_SHORT);
            return false;
        }

        if (lat == 0.0 && lng == 0.0) {
            ToastUtils.show(this, "Please select a location on the map", Toast.LENGTH_SHORT);
            return false;
        }

        String description = etDescription.getText().toString().trim();
        if (description.length() > MAX_DESCRIPTION_LENGTH) {
            ToastUtils.show(this, "Description too long (max 500 characters)", Toast.LENGTH_SHORT);
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
        outState.putString("iconName", selectedIconName);
        outState.putInt("iconColor", selectedIconColor);
    }
}