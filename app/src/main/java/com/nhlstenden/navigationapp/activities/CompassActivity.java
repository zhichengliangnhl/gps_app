package com.nhlstenden.navigationapp.activities;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import com.nhlstenden.navigationapp.BaseActivity;
import com.nhlstenden.navigationapp.R;
import com.nhlstenden.navigationapp.helpers.AppSettings;
import com.nhlstenden.navigationapp.helpers.CoinManager;
import com.nhlstenden.navigationapp.helpers.ToastUtils;
import com.nhlstenden.navigationapp.interfaces.CompassListener;
import com.nhlstenden.navigationapp.adapters.CompassSensorManager;
import com.nhlstenden.navigationapp.models.Waypoint;
import com.nhlstenden.navigationapp.models.Folder;
import com.nhlstenden.navigationapp.helpers.AchievementManager;
import com.nhlstenden.navigationapp.helpers.ArrowPurchaseManager;

import java.lang.reflect.Type;
import java.util.List;

public class CompassActivity extends BaseActivity implements CompassListener
{

    private static final int LOCATION_PERMISSION_REQUEST = 100;

    private CompassSensorManager compassSensorManager;
    private ImageView compassNeedle;
    private TextView distanceText, nameText, timerText;
    private FusedLocationProviderClient locationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private Waypoint targetWaypoint;
    private Location currentLocation;
    private float lastAnimatedAzimuth = 0f;
    private float currentAzimuth = 0f;
    private long lastUpdateTime = 0;
    private static final long MIN_UPDATE_INTERVAL_MS = 400; // e.g. 100ms, adjust as needed
    private static final int AZIMUTH_AVG_WINDOW = 5;
    private final float[] azimuthBuffer = new float[AZIMUTH_AVG_WINDOW];
    private int azimuthBufferIdx = 0;
    private final ActivityResultLauncher<ScanOptions> qrScannerLauncher = registerForActivityResult(new ScanContract(),
            result ->
            {
                if (result.getContents() != null)
                {
                    ToastUtils.show(this, "Scanned: " + result.getContents(), Toast.LENGTH_SHORT);
                }
            });

    // --- Add for waypoint reached dialog ---
    private boolean waypointReachedShown = false;
    private long navigationStartTime = 0L;
    private long elapsedTimeBeforePause = 0L;
    private static final float COMPLETION_DISTANCE = 10.0f; // meters

    // --- Enhanced stats tracking ---
    private float totalDistanceTraveled = 0f;
    private Location lastLocation = null;
    private int compassCorrections = 0;
    private float lastCompassAzimuth = 0f;
    private static final float COMPASS_CORRECTION_THRESHOLD = 30f; // degrees

    private Handler timerHandler = new Handler();
    private Runnable timerRunnable;

    private Vibrator vibrator;
    private long lastVibrationTime = 0;
    private boolean hasStoppedVibrating = false;
    private boolean hasEnteredCompletionRange = false;
    private boolean isActivityVisible = false;

    // Map color names to arrow resource numbers
    private String getArrowResourceName(String colorName)
    {
        switch (colorName.toLowerCase())
        {
            case "orange":
                return "1";
            case "red":
                return "2";
            case "yellow":
                return "3";
            case "green":
                return "4";
            case "cyan":
                return "5";
            case "blue":
                return "6";
            case "purple":
                return "7";
            case "rose":
                return "8";
            case "grey":
                return "9";
            case "white":
                return "10";
            default:
                return "1"; // Default to orange arrow
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_compass);

        this.targetWaypoint = this.getIntent().getParcelableExtra("WAYPOINT");

        Waypoint selectedWaypoint = this.loadSelectedWaypoint();

        if (selectedWaypoint == null)
        {
            Log.e("WAYPOINT", "No selected waypoint yet");
        }

        this.targetWaypoint = this.getIntent().getParcelableExtra("WAYPOINT");

        if (this.targetWaypoint == null)
        {
            Log.w("WAYPOINT", "No waypoint from Intent. Using saved waypoint as target.");
            this.targetWaypoint = selectedWaypoint;
        }

        this.compassNeedle = this.findViewById(R.id.arrowImage);
        this.distanceText = this.findViewById(R.id.distanceText);
        this.nameText = this.findViewById(R.id.waypointStatus);
        this.timerText = this.findViewById(R.id.timerText);

        // Set the selected arrow
        String selectedArrow = ArrowPurchaseManager.getSelectedArrow(this);
        String arrowNumber = this.getArrowResourceName(selectedArrow);
        int arrowResource = this.getResources().getIdentifier("arrow_" + arrowNumber, "drawable",
                this.getPackageName());

        if (arrowResource != 0)
        {
            this.compassNeedle.setImageResource(arrowResource);
        }
        else
        {
            Log.e("CompassActivity", "Failed to find arrow resource for: " + selectedArrow);
        }

        // Set top bar title
        TextView headerTitle = this.findViewById(R.id.headerTitle);

        if (headerTitle != null)
        {
            headerTitle.setText("Treasure Finder");
        }
        this.setupSettingsPanel();

        // Waypoint button
        Button waypointButton = this.findViewById(R.id.waypointsButton);

        if (waypointButton != null)
        {
            waypointButton.setOnClickListener(v -> this.startActivity(new Intent(this, FolderActivity.class)));
        }

        // Compass + location setup
        this.compassSensorManager = new CompassSensorManager(this);
        this.compassSensorManager.setCompassListener(this);

        this.locationClient = LocationServices.getFusedLocationProviderClient(this);

        // Use Parcelable!

        if (this.targetWaypoint != null)
        {
            ToastUtils.show(this, "Waypoint: " + this.targetWaypoint.getName() +
                    " @ " + this.targetWaypoint.getLat() + ", " + this.targetWaypoint.getLng(), Toast.LENGTH_LONG);
            Log.d("CompassActivity", "Waypoint: " + this.targetWaypoint.getName() +
                    " @ " + this.targetWaypoint.getLat() + ", " + this.targetWaypoint.getLng());
            this.nameText.setText(this.targetWaypoint.getName());
            this.navigationStartTime = System.currentTimeMillis();
            this.startLiveTimer();
        }
        else
        {
            Log.d("CompassActivity", "No waypoint received!");
            this.nameText.setText("No waypoint selected");
            this.distanceText.setText("Distance: -");
            this.timerText.setText("00:00");
        }

        // Check if this waypoint is already completed
        SharedPreferences prefs = this.getSharedPreferences("AppPrefs", MODE_PRIVATE);
        boolean completed = false;

        if (this.targetWaypoint != null)
        {
            completed = prefs.getBoolean("waypoint_completed_" + this.targetWaypoint.getId(), false);
        }
        if (completed)
        {
            // Clear selected waypoint and timer state
            if (this.targetWaypoint != null)
            {
                this.clearSelectedWaypoint();
                prefs.edit().remove("timer_elapsed_" + this.targetWaypoint.getId()).apply();
            }

            this.nameText.setText("No waypoint selected");
            this.distanceText.setText("Distance: -");
            this.timerText.setText("00:00");
            ToastUtils.show(this, "Waypoint already completed!", Toast.LENGTH_LONG);

            return;
        }

        this.requestLocationAccess();

        this.updateWaypointStatusText();

        // Restore timer state if available
        this.elapsedTimeBeforePause = prefs
                .getLong("timer_elapsed_" + (this.targetWaypoint != null ? this.targetWaypoint.getId() : ""), 0L);
        if (this.targetWaypoint != null)
        {
            this.navigationStartTime = System.currentTimeMillis() - this.elapsedTimeBeforePause;
            this.startLiveTimer();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        {
            VibratorManager vibratorManager = (VibratorManager) this.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            this.vibrator = vibratorManager.getDefaultVibrator();
        }
        else
        {
            this.vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        }

    }

    private void requestLocationAccess()
    {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
        }
        else
        {
            this.startLocationUpdates();
        }
    }

    private Waypoint loadSelectedWaypoint()
    {
        SharedPreferences prefs = this.getSharedPreferences("AppPrefs", MODE_PRIVATE);

        String id = prefs.getString("selected_wp_id", null);
        String name = prefs.getString("selected_wp_name", null);
        String latStr = prefs.getString("selected_wp_lat", null);
        String lngStr = prefs.getString("selected_wp_lng", null);

        if (id == null || name == null || latStr == null || lngStr == null)
        {
            Log.d("WAYPOINT_LOAD_ERROR", "WAYPOINT data is null");
            return null;
        }

        Log.d("WAYPOINT_LOAD", "Waypoint has been loaded");
        Log.d("WAYPOINT_DATA", name + ", " + latStr + ", " + lngStr);

        try
        {
            Log.d("WAYPOINT_LOAD", "Waypoint lat, lng parsing...");
            double lat = Double.parseDouble(latStr);
            double lng = Double.parseDouble(lngStr);
            Log.d("WAYPOINT_LOAD", "Waypoint lat, lng parsed!");

            return new Waypoint(id, name, "", "icon1", Color.BLACK, lat, lng);
        }
        catch (NumberFormatException e)
        {
            e.printStackTrace();
            Log.d("WAYPOINT_LOAD_ERROR", e.getMessage());
            return null;
        }
    }

    private void startLocationUpdates()
    {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(3000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback()
        {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onLocationResult(LocationResult locationResult)
            {
                if (locationResult == null)
                    return;
                currentLocation = locationResult.getLastLocation();
                // Track total distance traveled
                if (lastLocation != null)
                {
                    totalDistanceTraveled += lastLocation.distanceTo(currentLocation);
                }
                lastLocation = new Location(currentLocation);
                // Log.d("CompassActivity", "Location update: " + currentLocation.getLatitude()
                // + ", " + currentLocation.getLongitude());
                updateDistanceDisplay();
                updateNeedleRotation();
            }
        };

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            locationClient.requestLocationUpdates(locationRequest, locationCallback, getMainLooper());
        }
    }

    private void stopLocationUpdates()
    {
        locationClient.removeLocationUpdates(locationCallback);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void updateDistanceDisplay()
    {
        if (currentLocation == null || targetWaypoint == null)
        {
            Log.d("CompassActivity", "updateDistanceDisplay: currentLocation or targetWaypoint is null");

            boolean distanceDisplayEnabled = AppSettings.get(this, AppSettings.DISTANCE_DISPLAY, true);
            if (distanceDisplayEnabled)
            {
                distanceText.setText("Distance: -");
                distanceText.setVisibility(View.VISIBLE);
            }
            else
            {
                distanceText.setVisibility(View.GONE);
            }

            return;
        }

        Location target = new Location("target");
        target.setLatitude(targetWaypoint.getLat());
        target.setLongitude(targetWaypoint.getLng());
        float distance = currentLocation.distanceTo(target);

        Log.d("CompassActivity", "Distance to waypoint: " + distance + " meters");

        boolean distanceDisplayEnabled = AppSettings.get(this, AppSettings.DISTANCE_DISPLAY, true);

        if (distanceDisplayEnabled)
        {
            distanceText.setVisibility(View.VISIBLE);
        }
        else
        {
            distanceText.setVisibility(View.GONE);
        }

        if (distance <= 10f)
        {
            hasStoppedVibrating = true;
            distanceText.setText("You're here!");
        }
        else
        {
            distanceText.setText(String.format("Distance: %.1f meters", distance));
        }

        boolean vibrationEnabled = AppSettings.get(this, AppSettings.VIBRATION, true);
        if (isActivityVisible &&
                !hasStoppedVibrating &&
                distance >= 10f && distance < 100f &&
                vibrator != null && vibrationEnabled)
        {

            long interval = (long) (5000 * (distance / 100f));
            interval = Math.max(500, interval); // Minimum interval: 500ms

            long now = System.currentTimeMillis();

            if (now - lastVibrationTime >= interval)
            {
                vibrator.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE));
                lastVibrationTime = now;
            }
        }

        if (distance <= COMPLETION_DISTANCE && !waypointReachedShown)
        {
            waypointReachedShown = true;
            showWaypointReachedDialog(distance);
        }
    }

    private void updateNeedleRotation()
    {
        if (currentLocation == null || targetWaypoint == null)
        {
            // Log.d("CompassActivity", "updateNeedleRotation: currentLocation or
            // targetWaypoint is null");
            return;
        }

        Location target = new Location("target");
        target.setLatitude(targetWaypoint.getLat());
        target.setLongitude(targetWaypoint.getLng());

        float bearingTo = currentLocation.bearingTo(target);

        float angle = (bearingTo - currentAzimuth + 3600) % 360;

        ObjectAnimator.ofFloat(compassNeedle, "rotation", compassNeedle.getRotation(), angle)
                .setDuration(300)
                .start();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        isActivityVisible = true;
        Waypoint wp = loadSelectedWaypoint();

        if (wp != null)
        {
            targetWaypoint.setName(wp.getName());
            targetWaypoint.setLat(wp.getLat());
            targetWaypoint.setLng(wp.getLng());
            hasStoppedVibrating = false;
            distanceText.setVisibility(View.VISIBLE);
        }

        compassSensorManager.start();
        startLocationUpdates();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        isActivityVisible = false;
        compassSensorManager.stop();
        stopLocationUpdates();
        // Save timer state
        if (targetWaypoint != null && navigationStartTime > 0)
        {
            long elapsed = System.currentTimeMillis() - navigationStartTime;
            SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
            prefs.edit().putLong("timer_elapsed_" + targetWaypoint.getId(), elapsed).apply();
            // Save to waypoint in folder list
            saveTimerToWaypoint(targetWaypoint.getId(), elapsed);
        }
    }

    @Override
    public void onAzimuthChanged(float azimuth)
    {
        long now = System.currentTimeMillis();
        azimuthBuffer[azimuthBufferIdx] = azimuth;
        azimuthBufferIdx = (azimuthBufferIdx + 1) % AZIMUTH_AVG_WINDOW;
        float avgAzimuth = 0;
        for (float a : azimuthBuffer)
            avgAzimuth += a;
        avgAzimuth /= AZIMUTH_AVG_WINDOW;

        // Track compass corrections
        if (Math.abs(avgAzimuth - lastCompassAzimuth) > COMPASS_CORRECTION_THRESHOLD)
        {
            compassCorrections++;
            lastCompassAzimuth = avgAzimuth;
        }

        if (Math.abs(avgAzimuth - lastAnimatedAzimuth) > 2.0f && (now - lastUpdateTime > MIN_UPDATE_INTERVAL_MS))
        {
            this.currentAzimuth = avgAzimuth;
            lastAnimatedAzimuth = avgAzimuth;
            lastUpdateTime = now;
            updateNeedleRotation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            startLocationUpdates();
        }
        else
        {
            ToastUtils.show(this, "Location permission required", Toast.LENGTH_SHORT);
            finish();
        }
    }

    // Settings panel
    private void showSettingsPanel()
    {
        View sheetView = getLayoutInflater().inflate(R.layout.side_panel_settings, null);

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.RightSlideDialog)
                .setView(sheetView)
                .create();
        dialog.show();
    }

    private void showImportDialog()
    {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Import Waypoint Code");

        final EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton("Import", (dialog, which) ->
        {
            String code = input.getText().toString().trim();
            try
            {
                Waypoint wp = Waypoint.decode(this, code);
                if (wp != null && wp.getName() != null)
                {
                    ToastUtils.show(this, "Imported: " + wp.getName());

                    // Optionally update the waypoint here and refresh UI if desired
                }
                else
                {
                    ToastUtils.show(this, "Invalid or corrupted waypoint", Toast.LENGTH_SHORT);
                }
            } catch (Exception e)
            {
                ToastUtils.show(this, "Failed to import", Toast.LENGTH_SHORT);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    // Optional: for live updates if you ever want to switch waypoint without
    // re-launching activity
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void setWaypoint(Waypoint wp)
    {
        this.targetWaypoint = wp;
        if (wp != null)
        {
            nameText.setText(wp.getName());
            updateDistanceDisplay();
            updateNeedleRotation();
        }
        else
        {
            nameText.setText("No waypoint selected");
            distanceText.setText("Distance: -");
        }
    }

    private void clearSelectedWaypoint()
    {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        prefs.edit().remove("selected_wp_id")
                .remove("selected_wp_name")
                .remove("selected_wp_lat")
                .remove("selected_wp_lng")
                .apply();
    }

    private void showWaypointReachedDialog(float distance)
    {
        stopLiveTimer();
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_waypoint_reached, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        // Set transparent background for the dialog window
        if (dialog.getWindow() != null)
        {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView statsText = dialogView.findViewById(R.id.dialogStats);
        TextView titleText = dialogView.findViewById(R.id.dialogTitle);
        Button doneButton = dialogView.findViewById(R.id.dialogDoneButton);

        // Calculate stats
        String waypointName = targetWaypoint != null ? targetWaypoint.getName() : "-";
        float directDistance = 0f;
        if (targetWaypoint != null && lastLocation != null)
        {
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

        // Check if waypoint was already completed
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        boolean wasCompleted = false;
        if (targetWaypoint != null)
        {
            wasCompleted = prefs.getBoolean("waypoint_completed_" + targetWaypoint.getId(), false);
        }

        // Only give rewards if not already completed
        if (!wasCompleted)
        {
            // Check achievements when waypoint is actually reached
            AchievementManager.checkWaypointCompletion(this, distance);

            CoinManager.addCoins(this, 50);
            TextView coinCounter = findViewById(R.id.coinCounter);
            if (coinCounter != null)
            {
                CoinManager.updateCoinDisplay(this, coinCounter);
            }
        }

        doneButton.setOnClickListener(v ->
        {
            // Save final timer to waypoint and clear timer state
            if (targetWaypoint != null && navigationStartTime > 0)
            {
                long elapsed = System.currentTimeMillis() - navigationStartTime;
                saveTimerToWaypoint(targetWaypoint.getId(), elapsed);
                SharedPreferences preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                preferences.edit()
                        .remove("timer_elapsed_" + targetWaypoint.getId())
                        .putBoolean("waypoint_completed_" + targetWaypoint.getId(), true)
                        .apply();
            }
            clearSelectedWaypoint();
            dialog.dismiss();
        });

        dialog.show();
    }

    private String formatDuration(long millis)
    {
        long seconds = millis / 1000;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        if (hours > 0)
        {
            return String.format("%dh %dm %ds", hours, minutes, secs);
        }
        else if (minutes > 0)
        {
            return String.format("%dm %ds", minutes, secs);
        }
        else
        {
            return String.format("%ds", secs);
        }
    }

    private void startLiveTimer()
    {
        timerRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                long elapsed = System.currentTimeMillis() - navigationStartTime;
                timerText.setText(formatTimer(elapsed));
                timerHandler.postDelayed(this, 1000);
            }
        };

        timerHandler.post(timerRunnable);
    }

    private void stopLiveTimer()
    {
        timerHandler.removeCallbacks(timerRunnable);
    }

    private String formatTimer(long millis)
    {
        long seconds = millis / 1000;
        long minutes = (seconds % 3600) / 60;
        long hours = seconds / 3600;
        long secs = seconds % 60;
        if (hours > 0)
        {
            return String.format("%02d:%02d:%02d", hours, minutes, secs);
        }
        else
        {
            return String.format("%02d:%02d", minutes, secs);
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        stopLiveTimer();
    }

    @Override
    public void finish()
    {
        super.finish();
    }

    private void updateWaypointStatusText()
    {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String folderName = prefs.getString("selected_folder_name", null);
        String waypointName = targetWaypoint != null ? targetWaypoint.getName() : null;

        if (folderName != null && waypointName != null)
        {
            nameText.setText(folderName + " | " + waypointName);
        }
        else if (waypointName != null)
        {
            nameText.setText(waypointName);
        }
        else
        {
            nameText.setText("");
        }
    }

    private void saveTimerToWaypoint(String waypointId, long elapsedMillis)
    {
        // Save timer to waypoint in folder list in SharedPreferences
        SharedPreferences prefs = getSharedPreferences("com.nhlstenden.navigationapp.PREFS", MODE_PRIVATE);
        String json = prefs.getString("folders_json", null);
        if (json != null)
        {
            Type type = new TypeToken<List<Folder>>()
            {
            }.getType();
            List<Folder> folderList = new Gson().fromJson(json, type);
            boolean updated = false;
            for (Folder folder : folderList)
            {
                for (Waypoint wp : folder.getWaypoints())
                {
                    if (wp.getId().equals(waypointId))
                    {
                        wp.setNavigationTimeMillis(elapsedMillis);
                        updated = true;
                        break;
                    }
                }
                if (updated)
                    break;
            }
            if (updated)
            {
                prefs.edit().putString("folders_json", new Gson().toJson(folderList)).apply();
            }
        }
    }
}
