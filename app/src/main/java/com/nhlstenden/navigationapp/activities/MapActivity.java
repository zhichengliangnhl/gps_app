package com.nhlstenden.navigationapp.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;
import android.widget.EditText;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nhlstenden.navigationapp.R;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng selectedLocation;
    private Button btnSaveWaypoint;

    private final ActivityResultLauncher<Intent> createWaypointLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    // Pass the result back to WaypointActivity
                    setResult(RESULT_OK, result.getData());
                    finish();
                } else if (result.getResultCode() == RESULT_CANCELED) {
                    Toast.makeText(this, "Waypoint creation cancelled", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Initialize the map
        SupportMapFragment mapFragment = new SupportMapFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.map_container, mapFragment)
                .commit();
        mapFragment.getMapAsync(this);

        // Initialize location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize search bar and set up listener for Enter
        EditText searchEditText = findViewById(R.id.searchEditText);
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                String locationName = searchEditText.getText().toString().trim();
                if (!locationName.isEmpty()) {
                    searchLocation(locationName);
                }
                return true;
            }
            return false;
        });

        // Initialize save button
        btnSaveWaypoint = findViewById(R.id.btnSaveWaypoint);
        btnSaveWaypoint.setEnabled(false);
        btnSaveWaypoint.setOnClickListener(v -> saveWaypoint());

        // Check if we received coordinates to center the map
        Intent intent = getIntent();
        if (intent.hasExtra("lat") && intent.hasExtra("lng")) {
            double lat = intent.getDoubleExtra("lat", 0.0);
            double lng = intent.getDoubleExtra("lng", 0.0);
            if (lat != 0.0 && lng != 0.0) {
                selectedLocation = new LatLng(lat, lng);
                btnSaveWaypoint.setEnabled(true);
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Check for location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }

        // Set up map click listener for waypoint creation
        mMap.setOnMapClickListener(latLng -> {
            selectedLocation = latLng;
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(latLng).title("Selected Location"));
            btnSaveWaypoint.setEnabled(true);
        });

        // If we have a selected location from onCreate, show it on the map
        if (selectedLocation != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLocation, 15));
            mMap.addMarker(new MarkerOptions().position(selectedLocation).title("Selected Location"));
        }
    }

    private void enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);

            // Get current location and move camera
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                        }
                    });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            } else {
                Toast.makeText(this, "Location permission is required for this feature",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void saveWaypoint() {
        if (selectedLocation != null) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("lat", selectedLocation.latitude);
            resultIntent.putExtra("lng", selectedLocation.longitude);
            setResult(RESULT_OK, resultIntent);
            finish();
        }
    }

    private void searchLocation(String locationName) {
        Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> addressList = geocoder.getFromLocationName(locationName, 1);
            if (addressList != null && !addressList.isEmpty()) {
                Address address = addressList.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                selectedLocation = latLng;
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(latLng).title(locationName));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                btnSaveWaypoint.setEnabled(true);
            } else {
                Toast.makeText(this, "Address not found", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Log.e("MapActivity", "Geocoder failed", e);
            Toast.makeText(this, "Geocoding failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

}