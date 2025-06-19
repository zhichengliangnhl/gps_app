package com.nhlstenden.navigationapp.helpers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class LocationTracker {
    public interface LocationUpdateListener {
        void onLocationUpdate(Location location);
    }

    private final FusedLocationProviderClient locationClient;
    private final LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private final Context context;
    private LocationUpdateListener listener;

    public LocationTracker(Context context) {
        this.context = context;
        this.locationClient = LocationServices.getFusedLocationProviderClient(context);
        this.locationRequest = LocationRequest.create();
        this.locationRequest.setInterval(3000);
        this.locationRequest.setFastestInterval(2000);
        this.locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public void setLocationUpdateListener(LocationUpdateListener listener) {
        this.listener = listener;
    }

    @SuppressLint("MissingPermission")
    public void start() {
        if (locationCallback == null) {
            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    if (listener != null && locationResult.getLastLocation() != null) {
                        listener.onLocationUpdate(locationResult.getLastLocation());
                    }
                }
            };
        }
        locationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    public void stop() {
        if (locationCallback != null) {
            locationClient.removeLocationUpdates(locationCallback);
        }
    }
} 