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
import com.nhlstenden.navigationapp.helpers.LocationTracker;
import com.nhlstenden.navigationapp.helpers.NavigationTimer;
import com.nhlstenden.navigationapp.helpers.NavigationStats;
import com.nhlstenden.navigationapp.helpers.PreferencesHelper;
import com.nhlstenden.navigationapp.utils.TimeUtils;
import com.nhlstenden.navigationapp.helpers.WaypointReachedDialog;
import com.nhlstenden.navigationapp.helpers.SettingsPanelDialog;
import com.nhlstenden.navigationapp.helpers.ImportWaypointDialog;
import com.nhlstenden.navigationapp.helpers.CompassUIController;
import com.nhlstenden.navigationapp.utils.VibrationUtils;
import com.nhlstenden.navigationapp.utils.ArrowResourceUtils;

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

    private LocationTracker locationTracker;
    private NavigationTimer navigationTimer;
    private NavigationStats navigationStats;

    private CompassUIController uiController;

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

        this.uiController = new CompassUIController(distanceText, nameText, timerText, compassNeedle);

        // Set the selected arrow
        String selectedArrow = ArrowPurchaseManager.getSelectedArrow(this);
        int arrowResource = ArrowResourceUtils.getArrowResource(this, selectedArrow);
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

        // Use helpers
        this.locationTracker = new LocationTracker(this);
        this.locationTracker.setLocationUpdateListener(location -> {
            currentLocation = location;
            navigationStats.updateLocation(location);
            updateDistanceDisplay();
            updateNeedleRotation();
        });
        this.navigationStats = new NavigationStats();
        this.navigationTimer = new NavigationTimer(timerText);

        // Use Parcelable!

        if (this.targetWaypoint != null)
        {
            ToastUtils.show(this, "Waypoint: " + this.targetWaypoint.getName() +
                    " @ " + this.targetWaypoint.getLat() + ", " + this.targetWaypoint.getLng(), Toast.LENGTH_LONG);
            Log.d("CompassActivity", "Waypoint: " + this.targetWaypoint.getName() +
                    " @ " + this.targetWaypoint.getLat() + ", " + this.targetWaypoint.getLng());
            this.nameText.setText(this.targetWaypoint.getName());
            this.navigationStartTime = System.currentTimeMillis();
            this.navigationTimer.start(this.navigationStartTime);
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
            this.navigationTimer.start(this.navigationStartTime);
        }

        this.vibrator = VibrationUtils.getVibrator(this);

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
        locationTracker.start();
    }

    private void stopLocationUpdates()
    {
        locationTracker.stop();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void updateDistanceDisplay()
    {
        if (currentLocation == null || targetWaypoint == null)
        {
            boolean distanceDisplayEnabled = AppSettings.get(this, AppSettings.DISTANCE_DISPLAY, true);
            uiController.updateDistance(-1, distanceDisplayEnabled);
            return;
        }
        Location target = new Location("target");
        target.setLatitude(targetWaypoint.getLat());
        target.setLongitude(targetWaypoint.getLng());
        float distance = currentLocation.distanceTo(target);
        boolean distanceDisplayEnabled = AppSettings.get(this, AppSettings.DISTANCE_DISPLAY, true);
        if (distance <= 10f)
        {
            hasStoppedVibrating = true;
            uiController.showArrival();
        } else {
            uiController.updateDistance(distance, distanceDisplayEnabled);
        }
        boolean vibrationEnabled = AppSettings.get(this, AppSettings.VIBRATION, true);
        if (isActivityVisible && !hasStoppedVibrating && distance >= 10f && distance < 100f && vibrator != null && vibrationEnabled) {
            long interval = (long) (5000 * (distance / 100f));
            interval = Math.max(500, interval);
            long now = System.currentTimeMillis();
            if (now - lastVibrationTime >= interval) {
                VibrationUtils.vibrate(vibrator, 150);
                lastVibrationTime = now;
            }
        }
        if (distance <= COMPLETION_DISTANCE && !waypointReachedShown) {
            waypointReachedShown = true;
            WaypointReachedDialog.show(this, targetWaypoint, distance, navigationStats.getTotalDistanceTraveled(), navigationStats.getCompassCorrections(), navigationStartTime, navigationStats.getLastLocation(), () -> {
                if (targetWaypoint != null && navigationStartTime > 0) {
                    long elapsed = System.currentTimeMillis() - navigationStartTime;
                    saveTimerToWaypoint(targetWaypoint.getId(), elapsed);
                    PreferencesHelper.remove(this, "timer_elapsed_" + targetWaypoint.getId());
                    PreferencesHelper.saveBoolean(this, "waypoint_completed_" + targetWaypoint.getId(), true);
                    // Award 50 coins for reaching a waypoint
                    CoinManager.addCoins(this, 50);
                    // Immediately update coin display
                    TextView coinCounter = findViewById(R.id.coinCounter);
                    if (coinCounter != null) {
                        CoinManager.updateCoinDisplay(this, coinCounter);
                    }
                    // Update Runner achievements before clearing selected waypoint
                    AchievementManager.checkWaypointCompletion(this, distance);
                }
                clearSelectedWaypoint();
            });
        }
    }

    private void updateNeedleRotation()
    {
        if (currentLocation == null || targetWaypoint == null)
        {
            return;
        }
        Location target = new Location("target");
        target.setLatitude(targetWaypoint.getLat());
        target.setLongitude(targetWaypoint.getLng());
        float bearingTo = currentLocation.bearingTo(target);
        float angle = (bearingTo - currentAzimuth + 3600) % 360;
        uiController.updateNeedleRotation(angle);
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
        if (targetWaypoint != null && navigationStartTime > 0) {
            long elapsed = System.currentTimeMillis() - navigationStartTime;
            PreferencesHelper.saveLong(this, "timer_elapsed_" + targetWaypoint.getId(), elapsed);
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
        navigationStats.updateCompass(avgAzimuth);
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
        SettingsPanelDialog.show(this);
    }

    private void showImportDialog()
    {
        ImportWaypointDialog.show(this, wp -> {
            // Handle imported waypoint if needed
            ToastUtils.show(this, "Imported: " + wp.getName());
        });
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

    private void updateWaypointStatusText()
    {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String folderName = prefs.getString("selected_folder_name", null);
        String waypointName = targetWaypoint != null ? targetWaypoint.getName() : null;
        if (folderName != null && waypointName != null) {
            uiController.updateWaypointName(folderName + " | " + waypointName);
        } else if (waypointName != null) {
            uiController.updateWaypointName(waypointName);
        } else {
            uiController.updateWaypointName("");
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

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        navigationTimer.stop();
    }

    @Override
    public void finish()
    {
        super.finish();
    }
}