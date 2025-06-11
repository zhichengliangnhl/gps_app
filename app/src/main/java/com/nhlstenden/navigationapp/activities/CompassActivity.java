package com.nhlstenden.navigationapp.activities;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import com.nhlstenden.navigationapp.BaseActivity;
import com.nhlstenden.navigationapp.R;
import com.nhlstenden.navigationapp.helpers.CoinManager;
import com.nhlstenden.navigationapp.interfaces.CompassListener;
import com.nhlstenden.navigationapp.adapters.CompassSensorManager;
import com.nhlstenden.navigationapp.models.Waypoint;

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
    private float lastAnimatedAzimuth = 0f;
    private float currentAzimuth = 0f;
    private long lastUpdateTime = 0;
    private static final long MIN_UPDATE_INTERVAL_MS = 100; // e.g. 100ms, adjust as needed
    private static final int AZIMUTH_AVG_WINDOW = 5;
    private final float[] azimuthBuffer = new float[AZIMUTH_AVG_WINDOW];
    private int azimuthBufferIdx = 0;
    private final ActivityResultLauncher<ScanOptions> qrScannerLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if (result.getContents() != null) {
                    Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_SHORT).show();
                }
            });

    // --- Add for waypoint reached dialog ---
    private boolean waypointReachedShown = false;
    private long navigationStartTime = 0L;
    private static final float COMPLETION_DISTANCE = 10.0f; // meters

    // --- Enhanced stats tracking ---
    private float totalDistanceTraveled = 0f;
    private Location lastLocation = null;
    private int compassCorrections = 0;
    private float lastCompassAzimuth = 0f;
    private static final float COMPASS_CORRECTION_THRESHOLD = 30f; // degrees

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);

        // Header title
        TextView headerTitle = findViewById(R.id.headerTitle);
        if (headerTitle != null) {
            headerTitle.setText("Treasure Finder");
        }

        // Needle and waypoint info
        compassNeedle = findViewById(R.id.arrowImage);
        distanceText = findViewById(R.id.distanceText);
        nameText = findViewById(R.id.waypointStatus);

        // Earn coins button
        Button btnEarn = findViewById(R.id.btnEarnCoins);
        if (btnEarn != null) {
            btnEarn.setOnClickListener(v -> {
                CoinManager.addCoins(this, 100);
                TextView coinCounter = findViewById(R.id.coinCounter);
                if (coinCounter != null) {
                    CoinManager.updateCoinDisplay(this, coinCounter);
                }
            });
        }

        // Waypoint button
        Button waypointButton = findViewById(R.id.waypointsButton);
        if (waypointButton != null) {
            waypointButton.setOnClickListener(v -> startActivity(new Intent(this, FolderActivity.class)));
        }

        // Compass + location setup
        compassSensorManager = new CompassSensorManager(this);
        compassSensorManager.setCompassListener(this);

        locationClient = LocationServices.getFusedLocationProviderClient(this);

        // Use Parcelable!
        targetWaypoint = getIntent().getParcelableExtra("WAYPOINT");
        if (targetWaypoint != null) {
            Toast.makeText(this, "Waypoint: " + targetWaypoint.getName() +
                    " @ " + targetWaypoint.getLat() + ", " + targetWaypoint.getLng(), Toast.LENGTH_LONG).show();
            Log.d("CompassActivity", "Waypoint: " + targetWaypoint.getName() +
                    " @ " + targetWaypoint.getLat() + ", " + targetWaypoint.getLng());
            nameText.setText(targetWaypoint.getName());
        } else {
            Log.d("CompassActivity", "No waypoint received!");
            nameText.setText("No waypoint selected");
            distanceText.setText("Distance: -");
        }

        navigationStartTime = System.currentTimeMillis();

        requestLocationAccess();
    }

    private void requestLocationAccess() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                    LOCATION_PERMISSION_REQUEST);
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
                if (locationResult == null)
                    return;
                currentLocation = locationResult.getLastLocation();
                // Track total distance traveled
                if (lastLocation != null) {
                    totalDistanceTraveled += lastLocation.distanceTo(currentLocation);
                }
                lastLocation = new Location(currentLocation);
                Log.d("CompassActivity", "Location update: " +
                        currentLocation.getLatitude() + ", " + currentLocation.getLongitude());
                updateDistanceDisplay();
                updateNeedleRotation();
            }
        };

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
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
            Log.d("CompassActivity", "Distance to waypoint: " + distance);
            distanceText.setText(String.format("Distance: %.1f meters", distance));

            // Show dialog if reached and not already shown
            if (distance <= COMPLETION_DISTANCE && !waypointReachedShown) {
                waypointReachedShown = true;
                showWaypointReachedDialog(distance);
            }
        } else {
            Log.d("CompassActivity", "updateDistanceDisplay: currentLocation or targetWaypoint is null");
            distanceText.setText("Distance: -");
        }
    }

    private void updateNeedleRotation() {
        if (currentLocation == null || targetWaypoint == null) {
            Log.d("CompassActivity", "updateNeedleRotation: currentLocation or targetWaypoint is null");
            return;
        }

        Location target = new Location("target");
        target.setLatitude(targetWaypoint.getLat());
        target.setLongitude(targetWaypoint.getLng());

        float bearingTo = currentLocation.bearingTo(target);
        float angle = (bearingTo - currentAzimuth + 540) % 360;

        Log.d("CompassActivity",
                "Needle angle: " + angle + " (bearingTo: " + bearingTo + ", currentAzimuth: " + currentAzimuth + ")");

        ObjectAnimator.ofFloat(compassNeedle, "rotation", compassNeedle.getRotation(), angle)
                .setDuration(300)
                .start();
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
        long now = System.currentTimeMillis();
        azimuthBuffer[azimuthBufferIdx] = azimuth;
        azimuthBufferIdx = (azimuthBufferIdx + 1) % AZIMUTH_AVG_WINDOW;
        float avgAzimuth = 0;
        for (float a : azimuthBuffer)
            avgAzimuth += a;
        avgAzimuth /= AZIMUTH_AVG_WINDOW;

        // Track compass corrections
        if (Math.abs(avgAzimuth - lastCompassAzimuth) > COMPASS_CORRECTION_THRESHOLD) {
            compassCorrections++;
            lastCompassAzimuth = avgAzimuth;
        }

        if (Math.abs(avgAzimuth - lastAnimatedAzimuth) > 2.0f && (now - lastUpdateTime > MIN_UPDATE_INTERVAL_MS)) {
            this.currentAzimuth = avgAzimuth;
            lastAnimatedAzimuth = avgAzimuth;
            lastUpdateTime = now;
            updateNeedleRotation();
        }
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

    // Settings panel
    private void showSettingsPanel() {
        View sheetView = getLayoutInflater().inflate(R.layout.side_panel_settings, null);

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.RightSlideDialog)
                .setView(sheetView)
                .create();

        TextView txtScanQR = sheetView.findViewById(R.id.txtQr);
        TextView txtImport = sheetView.findViewById(R.id.txtImport);

        txtScanQR.setOnClickListener(v -> {
            dialog.dismiss();
            ScanOptions options = new ScanOptions();
            options.setPrompt("Scan a QR code");
            options.setBeepEnabled(true);
            options.setOrientationLocked(false);
            qrScannerLauncher.launch(options);
        });

        txtImport.setOnClickListener(v -> {
            dialog.dismiss();
            showImportDialog();
        });

        dialog.show();
    }

    private void showImportDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Import Waypoint Code");

        final EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton("Import", (dialog, which) -> {
            String code = input.getText().toString().trim();
            try {
                Waypoint wp = Waypoint.decode(this, code);
                if (wp != null && wp.getName() != null) {
                    Toast.makeText(this, "Imported: " + wp.getName(), Toast.LENGTH_SHORT).show();
                    // Optionally update the waypoint here and refresh UI if desired
                } else {
                    Toast.makeText(this, "Invalid or corrupted waypoint", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Failed to import", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    // Optional: for live updates if you ever want to switch waypoint without
    // re-launching activity
    public void setWaypoint(Waypoint wp) {
        this.targetWaypoint = wp;
        if (wp != null) {
            nameText.setText(wp.getName());
            updateDistanceDisplay();
            updateNeedleRotation();
        } else {
            nameText.setText("No waypoint selected");
            distanceText.setText("Distance: -");
        }
    }

    private void showWaypointReachedDialog(float distance) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_waypoint_reached, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        TextView statsText = dialogView.findViewById(R.id.dialogStats);
        TextView titleText = dialogView.findViewById(R.id.dialogTitle);
        Button doneButton = dialogView.findViewById(R.id.dialogDoneButton);

        // Calculate stats
        String waypointName = targetWaypoint != null ? targetWaypoint.getName() : "-";
        float directDistance = 0f;
        if (targetWaypoint != null && lastLocation != null) {
            Location start = new Location("start");
            start.setLatitude(targetWaypoint.getLat());
            start.setLongitude(targetWaypoint.getLng());
            directDistance = start.distanceTo(lastLocation);
        }
        long timeTakenMillis = System.currentTimeMillis() - navigationStartTime;
        String timeTaken = formatDuration(timeTakenMillis);
        float efficiency = (totalDistanceTraveled > 0) ? (directDistance / totalDistanceTraveled) * 100f : 0f;

        String stats = "Waypoint: " + waypointName + "\n" +
                "Direct distance: " + String.format("%.1f m", distance) + "\n" +
                "Total traveled: " + String.format("%.1f m", totalDistanceTraveled) + "\n" +
                "Efficiency: " + String.format("%.0f%%", efficiency) + "\n" +
                "Compass corrections: " + compassCorrections + "\n" +
                "Time taken: " + timeTaken + "\n";
        statsText.setText(stats);

        doneButton.setOnClickListener(v -> {
            dialog.dismiss();
            finish(); // Optionally finish activity
        });

        dialog.show();
    }

    private String formatDuration(long millis) {
        long seconds = millis / 1000;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes, secs);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, secs);
        } else {
            return String.format("%ds", secs);
        }
    }
}
