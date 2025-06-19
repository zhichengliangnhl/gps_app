package com.nhlstenden.navigationapp.helpers;

import android.location.Location;

public class NavigationStats {
    private float totalDistanceTraveled = 0f;
    private Location lastLocation = null;
    private int compassCorrections = 0;
    private float lastCompassAzimuth = 0f;
    private static final float COMPASS_CORRECTION_THRESHOLD = 30f;

    public void updateLocation(Location currentLocation) {
        if (lastLocation != null) {
            totalDistanceTraveled += lastLocation.distanceTo(currentLocation);
        }
        lastLocation = new Location(currentLocation);
    }

    public void updateCompass(float avgAzimuth) {
        if (Math.abs(avgAzimuth - lastCompassAzimuth) > COMPASS_CORRECTION_THRESHOLD) {
            compassCorrections++;
            lastCompassAzimuth = avgAzimuth;
        }
    }

    public float getTotalDistanceTraveled() {
        return totalDistanceTraveled;
    }

    public int getCompassCorrections() {
        return compassCorrections;
    }

    public Location getLastLocation() {
        return lastLocation;
    }
} 