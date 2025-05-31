package com.nhlstenden.navigationapp.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nhlstenden.navigationapp.R;

public class CreateWaypointActivity extends AppCompatActivity {

    private MapView mapPreview;
    private GoogleMap previewMap;

    private double lat = 0.0;
    private double lng = 0.0;

    private EditText etName, etDescription;
    private Button btnSaveWaypoint, btnCancel;
    private ImageView imagePreview;
    private View imageClickOverlay;

    private Uri imageUri = Uri.EMPTY;

    // Launcher for picking an image from gallery
    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    imageUri = uri;
                    imagePreview.setImageURI(imageUri);
                }
            });

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

        TextView headerTitle = findViewById(R.id.headerTitle);
        if (headerTitle != null) {
            headerTitle.setText("Create Treasure");
        }

        etName = findViewById(R.id.etName);
        etDescription = findViewById(R.id.etDescription);
        btnSaveWaypoint = findViewById(R.id.btnSaveWaypoint);
        btnCancel = findViewById(R.id.btnCancel);
        mapPreview = findViewById(R.id.mapPreview);
        imagePreview = findViewById(R.id.imagePreview);
        imageClickOverlay = findViewById(R.id.imageClickOverlay);

        // MapView setup
        mapPreview.onCreate(savedInstanceState);
        mapPreview.getMapAsync(googleMap -> {
            previewMap = googleMap;
            previewMap.getUiSettings().setAllGesturesEnabled(false); // Disable interaction
            updateMapPreview(lat, lng);
        });

        // Restore lst/lng
        if (savedInstanceState != null) {
            lat = savedInstanceState.getDouble("lat", 0.0);
            lng = savedInstanceState.getDouble("lng", 0.0);
        } else {
            Intent intent = getIntent();
            if (intent != null && intent.hasExtra("lat") && intent.hasExtra("lng")) {
                lat = intent.getDoubleExtra("lat", 0.0);
                lng = intent.getDoubleExtra("lng", 0.0);
            }
        }

        // Launch MapActivity when overlay is clicked
        findViewById(R.id.mapClickOverlay).setOnClickListener(v -> {
            Intent intent = new Intent(CreateWaypointActivity.this, MapActivity.class);
            if (lat != 0.0 && lng != 0.0) {
                intent.putExtra("lat", lat);
                intent.putExtra("lng", lng);
            }
            mapLauncher.launch(intent);
        });

        imageClickOverlay.setOnClickListener(v -> {
            imagePickerLauncher.launch("image/*");
        });

        btnSaveWaypoint.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter a waypoint name", Toast.LENGTH_SHORT).show();
                return;
            }
            if (lat == 0.0 && lng == 0.0) {
                Toast.makeText(this, "Please select a location on the map", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent resultIntent = new Intent();
            resultIntent.putExtra("name", name);
            resultIntent.putExtra("description", etDescription.getText().toString().trim());
            resultIntent.putExtra("lat", lat);
            resultIntent.putExtra("lng", lng);

            if (imageUri != Uri.EMPTY) {
                resultIntent.putExtra("imageUri", imageUri.toString());
            }

            setResult(RESULT_OK, resultIntent);
            finish();
        });

        // Cancel button logic
        btnCancel.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });
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
    }
}
