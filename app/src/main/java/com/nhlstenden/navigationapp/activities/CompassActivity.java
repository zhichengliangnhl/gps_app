package com.nhlstenden.navigationapp.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.nhlstenden.navigationapp.BaseActivity;
import com.nhlstenden.navigationapp.R;
import com.nhlstenden.navigationapp.enums.ThemeMode;
import com.nhlstenden.navigationapp.helpers.CoinManager;
import com.nhlstenden.navigationapp.helpers.ThemeHelper;
import com.nhlstenden.navigationapp.interfaces.CompassListener;
import com.nhlstenden.navigationapp.adapters.CompassSensorManager;
import com.nhlstenden.navigationapp.models.Waypoint;
import com.nhlstenden.navigationapp.helpers.AchievementManager;

public class CompassActivity extends BaseActivity implements CompassListener {

    private static final int LOCATION_PERMISSION_REQUEST = 100;

    private CompassSensorManager compassSensorManager;
    private ImageView compassNeedle;
    private TextView distanceText, nameText;
    private FusedLocationProviderClient locationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private Waypoint targetWaypoint;
    private Location currentLocation;

    private float currentAzimuth = 0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);

        TextView headerTitle = findViewById(R.id.headerTitle);
        if (headerTitle != null) {
            headerTitle.setText("Treasure Finder");
        }

        // Arrow needle and waypoint info
        compassNeedle = findViewById(R.id.arrowImage);
        distanceText = findViewById(R.id.distanceText);
        nameText = findViewById(R.id.waypointStatus);

        // Buttons
        Button waypointButton = findViewById(R.id.waypointsButton);


        findViewById(R.id.btnChangeTheme).setOnClickListener(v -> {
            String[] options = {"Classic", "Splash", "Retro"};
            new AlertDialog.Builder(this)
                    .setTitle("Choose Theme")
                    .setItems(options, (dialog, which) -> {
                        ThemeMode selected = ThemeMode.values()[which];
                        ThemeHelper.setTheme(this, selected);
                        recreate(); // Restart activity to apply theme
                    })
                    .show();
        });

        TextView coinText = findViewById(R.id.coinText);
        Button btnEarn = findViewById(R.id.btnEarnCoins);

        // Display current coin count
        coinText.setText("Coins: " + CoinManager.getCoins(this));

        // Add coin when button clicked
        btnEarn.setOnClickListener(v -> {
            CoinManager.addCoins(this, 1);
            coinText.setText("Coins: " + CoinManager.getCoins(this));
        });

        waypointButton.setOnClickListener(v -> startActivity(new Intent(this, FolderActivity.class)));

        // Compass + location setup
        compassSensorManager = new CompassSensorManager(this);
        compassSensorManager.setCompassListener(this);

        locationClient = LocationServices.getFusedLocationProviderClient(this);

        targetWaypoint = (Waypoint) getIntent().getSerializableExtra("WAYPOINT");
        if (targetWaypoint != null) {
            nameText.setText(targetWaypoint.getName());
        } else {
            nameText.setText("No waypoint selected");
            distanceText.setText("Distance: -");
        }

        requestLocationAccess();
    }

    private void requestLocationAccess() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
        } else {
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(3000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;
                currentLocation = locationResult.getLastLocation();
                updateDistanceDisplay();
                updateNeedleRotation();
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationClient.requestLocationUpdates(locationRequest, locationCallback, getMainLooper());
        }
    }

    private void stopLocationUpdates() {
        locationClient.removeLocationUpdates(locationCallback);
    }

    private void updateDistanceDisplay() {
        if (currentLocation != null && targetWaypoint != null) {
            Location target = new Location("target");
            target.setLatitude(targetWaypoint.getLat());
            target.setLongitude(targetWaypoint.getLng());
            float distance = currentLocation.distanceTo(target);
            distanceText.setText(String.format("Distance: %.1f meters", distance));
            
            // Check if waypoint is reached and update achievements
            AchievementManager.checkWaypointCompletion(this, distance);
        }
    }

    private void updateNeedleRotation() {
        if (currentLocation == null || targetWaypoint == null) return;

        Location target = new Location("target");
        target.setLatitude(targetWaypoint.getLat());
        target.setLongitude(targetWaypoint.getLng());

        float bearingTo = currentLocation.bearingTo(target);
        float angle = (bearingTo - currentAzimuth + 360) % 360;

        compassNeedle.setRotation(angle);
    }

    @Override
    protected void onResume() {
        super.onResume();
        compassSensorManager.start();
        startLocationUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        compassSensorManager.stop();
        stopLocationUpdates();
    }

    @Override
    public void onAzimuthChanged(float azimuth) {
        this.currentAzimuth = azimuth;
        updateNeedleRotation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        } else {
            Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
