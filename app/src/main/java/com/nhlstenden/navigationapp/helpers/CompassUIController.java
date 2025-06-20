package com.nhlstenden.navigationapp.helpers;

import android.animation.ObjectAnimator;
import android.location.Location;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nhlstenden.navigationapp.models.Waypoint;

public class CompassUIController {
    private final TextView distanceText;
    private final TextView nameText;
    private final TextView timerText;
    private final ImageView compassNeedle;

    public CompassUIController(TextView distanceText, TextView nameText, TextView timerText, ImageView compassNeedle) {
        this.distanceText = distanceText;
        this.nameText = nameText;
        this.timerText = timerText;
        this.compassNeedle = compassNeedle;
    }

    public void updateDistance(float distance, boolean show) {
        if (show) {
            distanceText.setVisibility(View.VISIBLE);
            distanceText.setText(String.format("Distance: %.1f meters", distance));
        } else {
            distanceText.setVisibility(View.GONE);
        }
    }

    public void showArrival() {
        distanceText.setText("You're here!");
    }

    public void updateWaypointName(String name) {
        nameText.setText(name);
    }

    public void updateTimer(String formattedTime) {
        timerText.setText(formattedTime);
    }

    public void updateNeedleRotation(float angle) {
        float currentRotation = compassNeedle.getRotation();

        // Prevent unnecessary animation for very small changes
        if (Math.abs(currentRotation - angle) < 1.0f) return;

        ObjectAnimator animator = ObjectAnimator.ofFloat(compassNeedle, "rotation", currentRotation, angle);
        animator.setDuration(300); // You can tweak this for faster/slower animation
        animator.start();
    }
} 