package com.nhlstenden.navigationapp.helpers;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.nhlstenden.navigationapp.R;
import com.nhlstenden.navigationapp.models.Waypoint;
import com.nhlstenden.navigationapp.utils.TimeUtils;

public class WaypointReachedDialog {
    public static void show(Context context, Waypoint targetWaypoint, float distance, float totalDistanceTraveled, int compassCorrections, long navigationStartTime, Location lastLocation, Runnable onDone) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_waypoint_reached, null);
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView statsText = dialogView.findViewById(R.id.dialogStats);
        TextView titleText = dialogView.findViewById(R.id.dialogTitle);
        Button doneButton = dialogView.findViewById(R.id.dialogDoneButton);

        String waypointName = targetWaypoint != null ? targetWaypoint.getName() : "-";
        float directDistance = 0f;
        if (targetWaypoint != null && lastLocation != null) {
            Location start = new Location("start");
            start.setLatitude(targetWaypoint.getLat());
            start.setLongitude(targetWaypoint.getLng());
            directDistance = start.distanceTo(lastLocation);
        }
        long timeTakenMillis = System.currentTimeMillis() - navigationStartTime;
        String timeTaken = TimeUtils.formatDuration(timeTakenMillis);
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
            if (onDone != null) onDone.run();
        });

        dialog.show();
    }
} 