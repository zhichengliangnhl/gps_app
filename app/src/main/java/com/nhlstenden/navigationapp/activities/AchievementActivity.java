package com.nhlstenden.navigationapp.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.nhlstenden.navigationapp.R;

public class AchievementActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievement);

        setupCard(R.id.card1, "Runner III", "Complete 10 waypoints");
        setupCard(R.id.card2, "Runner II", "Complete 5 waypoints");
        setupCard(R.id.card3, "Runner I", "Complete a waypoint");
        setupCard(R.id.card4, "Theme Collector III", "Unlock all coin purchased themes");
        setupCard(R.id.card5, "Theme Collector II", "Unlock two achievement themes");
        setupCard(R.id.card6, "Theme Collector I", "Unlock your first theme");
        setupCard(R.id.card7, "Collectionista", "Collect all achievements");
    }

    private void setupCard(int cardId, String title, String description) {
        LinearLayout card = findViewById(cardId);
        card.setOnClickListener(v -> showAchievementDialog(title, description));
    }

    private void showAchievementDialog(String title, String description) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_achievement, null);
        TextView dialogTitle = dialogView.findViewById(R.id.dialogText);
        ProgressBar progressBar = dialogView.findViewById(R.id.progressBar);
        TextView dialogDesc = dialogView.findViewById(R.id.dialogDesc);

        dialogTitle.setText(title);
        progressBar.setProgress(0);
        if (dialogDesc != null) {
            dialogDesc.setText(description);
        }

        new AlertDialog.Builder(this)
        .setView(dialogView)
        .setPositiveButton("OK", null)
        .show();
    }
}