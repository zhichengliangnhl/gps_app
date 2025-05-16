package com.nhlstenden.navigationapp.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nhlstenden.navigationapp.R;

import java.util.Locale;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final LatLng NHL_STENDEN_EMMEN = new LatLng(52.77813, 6.91259);
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private GoogleMap googleMap;
    private Marker currentMarker;
    private LatLng selectedLocation;
    private FusedLocationProviderClient fusedLocationClient;
    private boolean usingFallbackLocation = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Button saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(v -> saveLocation());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;

        googleMap.setOnMapClickListener(latLng -> {
            placeMarker(latLng);
            selectedLocation = latLng;
            usingFallbackLocation = false; // User selected a location manually
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            enableLocationFeatures();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE
            );
        }
    }

    private void enableLocationFeatures() {
        try {
            googleMap.setMyLocationEnabled(true);
            googleMap.getUiSettings().setMyLocationButtonEnabled(true);

            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            LatLng currentLatLng = new LatLng(
                                    location.getLatitude(),
                                    location.getLongitude()
                            );
                            placeMarker(currentLatLng);
                            selectedLocation = currentLatLng;
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f));
                        } else {
                            useFallbackLocation();
                        }
                    });
        } catch (SecurityException e) {
            useFallbackLocation();
        }
    }

    private void useFallbackLocation() {
        usingFallbackLocation = true;
        placeMarker(NHL_STENDEN_EMMEN);
        selectedLocation = NHL_STENDEN_EMMEN;
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(NHL_STENDEN_EMMEN, 15f));
        Toast.makeText(this, "Using NHL Stenden as fallback location", Toast.LENGTH_SHORT).show();
    }

    private void placeMarker(LatLng position) {
        if (currentMarker != null) {
            currentMarker.remove();
        }
        currentMarker = googleMap.addMarker(new MarkerOptions()
                .position(position)
                .title(usingFallbackLocation ? "Default Location" : "Selected Location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
    }

    private void saveLocation() {
        if (selectedLocation != null) {
            String locationType = usingFallbackLocation ? "Default" : "Selected";
            String message = String.format(Locale.getDefault(),
                    "Lat: %.6f\nLng: %.6f",
                    locationType,
                    selectedLocation.latitude,
                    selectedLocation.longitude);

            Toast.makeText(this, message, Toast.LENGTH_LONG).show();

            getSharedPreferences("Locations", MODE_PRIVATE)
                    .edit()
                    .putFloat("last_lat", (float) selectedLocation.latitude)
                    .putFloat("last_lng", (float) selectedLocation.longitude)
                    .putBoolean("was_fallback", usingFallbackLocation)
                    .apply();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableLocationFeatures();
            } else {
                useFallbackLocation();
            }
        }
    }
}