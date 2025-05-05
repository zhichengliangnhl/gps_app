package com.nhlstenden.navigationapp.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.nhlstenden.navigationapp.R;
import com.nhlstenden.navigationapp.models.Waypoint;

public class CompassActivity extends AppCompatActivity implements SensorEventListener {

    private static final int LOCATION_PERMISSION_REQUEST = 100;

    private SensorManager sensorManager;
    private Sensor rotationSensor;
    private ImageView compassNeedle;
    private TextView distanceText, nameText;

    private FusedLocationProviderClient locationClient;
    private Waypoint targetWaypoint;
    private Location currentLocation;

    private float currentAzimuth = 0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);

        compassNeedle = findViewById(R.id.compassNeedleImageView);
        distanceText = findViewById(R.id.distanceTextView);
        nameText = findViewById(R.id.nameTextView);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        locationClient = LocationServices.getFusedLocationProviderClient(this);

        targetWaypoint = (Waypoint) getIntent().getSerializableExtra("WAYPOINT");
        if (targetWaypoint != null) {
            nameText.setText(targetWaypoint.getName());
        } else {
            nameText.setText("No waypoint selected");
            distanceText.setText("Distance: --");
        }

        findViewById(R.id.btnWaypointList).setOnClickListener(v -> {
            Intent i = new Intent(this, WaypointActivity.class);
            startActivity(i);
        });

        findViewById(R.id.btnCreateWaypoint).setOnClickListener(v -> {
            Intent i = new Intent(this, CreateWaypointActivity.class);
            startActivity(i);
        });

        requestLocationAccess();
    }

    private void requestLocationAccess() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
        } else {
            updateLocation();
        }
    }

    private void updateLocation() {
        try {
            locationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        currentLocation = location;
                        updateDistanceDisplay();
                    }
                }
            });
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void updateDistanceDisplay() {
        if (currentLocation != null && targetWaypoint != null) {
            Location target = new Location("target");
            target.setLatitude(targetWaypoint.getLat());
            target.setLongitude(targetWaypoint.getLng());

            float distance = currentLocation.distanceTo(target);
            distanceText.setText(String.format("Distance: %.1f meters", distance));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (rotationSensor != null)
            sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_UI);
        updateLocation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            float[] rotationMatrix = new float[9];
            float[] orientation = new float[3];

            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
            SensorManager.getOrientation(rotationMatrix, orientation);

            float azimuthRad = orientation[0];
            float azimuthDeg = (float) Math.toDegrees(azimuthRad);
            if (azimuthDeg < 0) azimuthDeg += 360;

            currentAzimuth = azimuthDeg;
            updateNeedleRotation();
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
    public void onAccuracyChanged(Sensor sensor, int i) {
        // no-op
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            updateLocation();
        } else {
            Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
