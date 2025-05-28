package com.nhlstenden.navigationapp.helpers;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.nhlstenden.navigationapp.R;
import com.nhlstenden.navigationapp.activities.CompassActivity;

public class BottomNavHelper {
    public static void setupBottomNav(Activity activity) {
        View root = activity.findViewById(android.R.id.content);
        if (root == null) return;

        ImageView navBrush = root.findViewById(R.id.navBrush);
        ImageView navArrow = root.findViewById(R.id.navArrow);
        ImageView navTrophy = root.findViewById(R.id.navTrophy);

        if (navBrush != null) {
            navBrush.setOnClickListener(v ->
                    Toast.makeText(activity, "Brush feature coming soon", Toast.LENGTH_SHORT).show()
            );
        }

        if (navArrow != null) {
            navArrow.setOnClickListener(v -> {
                Intent intent = new Intent(activity, CompassActivity.class);
                activity.startActivity(intent);
            });
        }

        if (navTrophy != null) {
            navTrophy.setOnClickListener(v ->
                    Toast.makeText(activity, "Trophies coming soon", Toast.LENGTH_SHORT).show()
            );
        }
    }
}
