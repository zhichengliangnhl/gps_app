package com.nhlstenden.navigationapp.adapters;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.nhlstenden.navigationapp.interfaces.CompassListener;

public class CompassSensorManager implements SensorEventListener {
    private final SensorManager sensorManager;
    private final Sensor accelSensor, magSensor;
    private final float[] accelVals = new float[3], magVals = new float[3];
    private final float[] rotationMatrix = new float[9], orientation = new float[3];
    private CompassListener listener;

    public CompassSensorManager(Context ctx) {
        sensorManager = (SensorManager) ctx.getSystemService(Context.SENSOR_SERVICE);
        accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    public void start() {
        sensorManager.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, magSensor, SensorManager.SENSOR_DELAY_UI);
    }

    public void stop() {
        sensorManager.unregisterListener(this);
    }

    public void setCompassListener(CompassListener listener) {
        this.listener = listener;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelVals, 0, accelVals.length);
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magVals, 0, magVals.length);
        }

        if (accelVals != null && magVals != null) {
            SensorManager.getRotationMatrix(rotationMatrix, null, accelVals, magVals);
            SensorManager.getOrientation(rotationMatrix, orientation);
            float azimuthRad = orientation[0];
            float azimuthDeg = (float) Math.toDegrees(azimuthRad);
            if (azimuthDeg < 0) azimuthDeg += 360;

            if (listener != null) {
                listener.onAzimuthChanged(azimuthDeg);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        // No-op
    }
}
