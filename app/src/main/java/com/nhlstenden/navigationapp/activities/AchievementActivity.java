package com.nhlstenden.navigationapp.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.nhlstenden.navigationapp.R;

import com.nhlstenden.navigationapp.BaseActivity;

public class AchievementActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievement);

        TextView headerTitle = findViewById(R.id.headerTitle);
        if (headerTitle != null) {
            headerTitle.setText("Treasure Trophy");
        }

        setupCard(R.id.card1, "Runner III", "Complete 10 waypoints", "Progress: 0/10");
        setupCard(R.id.card2, "Runner II", "Complete 5 waypoints", "Progress: 0/5");
        setupCard(R.id.card3, "Runner I", "Complete a waypoint", "Progress: 0/1");
        setupCard(R.id.card4, "Theme Collector III", "Unlock all coin purchased themes", "Progress: 0/");
        setupCard(R.id.card5, "Theme Collector II", "Unlock two achievement themes", "Progress: 0/2");
        setupCard(R.id.card6, "Theme Collector I", "Unlock your first theme", "Progress: 0/1");
        setupCard(R.id.card7, "Collectionista", "Collect all achievements", "Progress: 0/6");
    }

    private void setupCard(int cardId, String title, String description, String progress) {
        LinearLayout card = findViewById(cardId);
        card.setOnClickListener(v -> showAchievementDialog(title, description, progress));
    }

    private void showAchievementDialog(String title, String description, String progressText) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_achievement, null);
        TextView dialogTitle = dialogView.findViewById(R.id.dialogText);
        ProgressBar progressBar = dialogView.findViewById(R.id.progressBar);
        TextView dialogDesc = dialogView.findViewById(R.id.dialogDesc);
        TextView dialogProgress = dialogView.findViewById(R.id.dialogProgress);
        Button okButton = dialogView.findViewById(R.id.okButton);

        dialogTitle.setText(title);
        dialogDesc.setText(description);
        dialogProgress.setText(progressText);
        progressBar.setProgress(0);

        AlertDialog dialog = new AlertDialog.Builder(this)
        .setView(dialogView)
        .create();
        if (dialog.getWindow() != null) {
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        okButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}