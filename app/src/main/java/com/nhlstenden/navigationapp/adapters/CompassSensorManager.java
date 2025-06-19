package com.nhlstenden.navigationapp.adapters;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.nhlstenden.navigationapp.interfaces.CompassListener;

public class CompassSensorManager implements SensorEventListener
{
    private final SensorManager sensorManager;
    private final Sensor accelSensor, magSensor;
    private final float[] accelVals = new float[3], magVals = new float[3];
    private final float[] rotationMatrix = new float[9], orientation = new float[3];

    private static final int WINDOW = 5;
    private final float[] azimuthHistory = new float[WINDOW];
    private int azimuthIdx = 0;
    private CompassListener listener;

    public CompassSensorManager(Context ctx)
    {
        this.sensorManager = (SensorManager) ctx.getSystemService(Context.SENSOR_SERVICE);
        this.accelSensor = this.sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.magSensor = this.sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    public void start()
    {
        this.sensorManager.registerListener(this, this.accelSensor, SensorManager.SENSOR_DELAY_UI);
        this.sensorManager.registerListener(this, this.magSensor, SensorManager.SENSOR_DELAY_UI);
    }

    public void stop()
    {
        this.sensorManager.unregisterListener(this);
    }

    public void setCompassListener(CompassListener listener)
    {
        this.listener = listener;
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
        {
            System.arraycopy(event.values, 0, this.accelVals, 0, this.accelVals.length);
        }
        else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
        {
            System.arraycopy(event.values, 0, this.magVals, 0, this.magVals.length);
        }

        if (this.accelVals != null && this.magVals != null)
        {
            SensorManager.getRotationMatrix(this.rotationMatrix, null, this.accelVals, this.magVals);
            SensorManager.getOrientation(this.rotationMatrix, this.orientation);
            float azimuthRad = this.orientation[0];
            float azimuthDeg = (float) Math.toDegrees(azimuthRad);
            if (azimuthDeg < 0) azimuthDeg += 360;

            // --- Smoothing ---
            this.azimuthHistory[this.azimuthIdx] = azimuthDeg;
            this.azimuthIdx = (this.azimuthIdx + 1) % WINDOW;
            float avgAzimuth = 0;
            for (float a : this.azimuthHistory) avgAzimuth += a;
            avgAzimuth /= WINDOW;

            if (this.listener != null)
            {
                this.listener.onAzimuthChanged(avgAzimuth);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i)
    {
        // No-op
    }
}
