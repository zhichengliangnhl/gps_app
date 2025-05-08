package com.nhlstenden.navigationapp.activities;

import android.Manifest;
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

import com.nhlstenden.navigationapp.R;
import com.nhlstenden.navigationapp.models.Waypoint;

public class CompassActivity extends AppCompatActivity implements SensorEventListener {

    private static final int LOCATION_PERMISSION_REQUEST = 100;

    private SensorManager sensorManager;
    private Sensor rotationSensor;

    private ImageView compassNeedle;
    private TextView distanceText, nameText;

    private float currentDegree = 0f;
    private Waypoint targetWaypoint;
    private Location currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);

        compassNeedle = findViewById(R.id.compassNeedleImageView);
        distanceText = findViewById(R.id.distanceTextView);
        nameText = findViewById(R.id.nameTextView);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

        targetWaypoint = (Waypoint) getIntent().getSerializableExtra("WAYPOINT");
        if (targetWaypoint == null) {
            Toast.makeText(this, "No waypoint provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        nameText.setText(targetWaypoint.getName());

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
        Location location = new Location("dummy");
        location.setLatitude(52.0); // TODO: replace with real location
        location.setLongitude(6.0);
        this.currentLocation = location;

        updateDistanceAndBearing();
    }

    private void updateDistanceAndBearing() {
        Location target = new Location("target");
        target.setLatitude(targetWaypoint.getLat());
        target.setLongitude(targetWaypoint.getLng());

        float distance = currentLocation.distanceTo(target);
        distanceText.setText(String.format("Distance: %.1f meters", distance));
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float degree = Math.round(event.values[0]);
        compassNeedle.setRotation(-degree);
        currentDegree = degree;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            updateLocation();
        } else {
            Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
