package com.nhlstenden.navigationapp.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.GridLayout;
import com.nhlstenden.navigationapp.R;

import com.nhlstenden.navigationapp.BaseActivity;
import java.util.Arrays;
import java.util.List;

public class AchievementActivity extends BaseActivity {

    public enum Difficulty {
        ONE_STAR(R.drawable.star1),
        TWO_STAR(R.drawable.star2),
        THREE_STAR(R.drawable.star3);
        public final int starResId;
        Difficulty(int starResId) { this.starResId = starResId; }
    }

    public static class Achievement {
        public String title;
        public String description;
        public Difficulty difficulty;
        public String progress;
        public Achievement(String title, String description, Difficulty difficulty, String progress) {
            this.title = title;
            this.description = description;
            this.difficulty = difficulty;
            this.progress = progress;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievement);

        TextView headerTitle = findViewById(R.id.headerTitle);
        if (headerTitle != null) {
            headerTitle.setText("Treasure Trophies");
        }

        List<Achievement> achievements = Arrays.asList(
                new Achievement("First steps", "Create a waypoint", Difficulty.ONE_STAR, "Progress:0/1"),
                new Achievement("Runner I", "Complete a waypoint", Difficulty.ONE_STAR, "Progress: 0/1"),
                new Achievement("Theme I", "Unlock your first theme", Difficulty.ONE_STAR, "Progress: 0/1"),
                new Achievement("Runner II", "Complete 5 waypoints", Difficulty.TWO_STAR, "Progress: 0/5"),
                new Achievement("Theme II", "Unlock two achievement themes", Difficulty.TWO_STAR, "Progress: 0/2"),
                new Achievement("Runner III", "Complete 10 waypoints", Difficulty.THREE_STAR, "Progress: 0/10"),
                new Achievement("Theme III", "Unlock all coin purchased themes", Difficulty.THREE_STAR, "Progress: 0/"),
                new Achievement("Collectionista", "Collect all achievements", Difficulty.THREE_STAR, "Progress: 0/6")
        );

        GridLayout container = findViewById(R.id.achievementContainer);
        int columnCount = 2;
        container.setColumnCount(columnCount);
        for (Achievement achievement : achievements) {
            View card = LayoutInflater.from(this).inflate(R.layout.item_achievement, container, false);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(12, 12, 12, 12);
            card.setLayoutParams(params);

            ((TextView) card.findViewById(R.id.achievementTitle)).setText(achievement.title);
            ((ImageView) card.findViewById(R.id.achievementTrophy)).setImageResource(R.drawable.trophy);
            ((ImageView) card.findViewById(R.id.achievementStars)).setImageResource(achievement.difficulty.starResId);
            card.setOnClickListener(v -> showAchievementDialog(achievement));
            container.addView(card);
        }
    }

    private void showAchievementDialog(Achievement achievement) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_achievement, null);
        ((TextView) dialogView.findViewById(R.id.dialogText)).setText(achievement.title);
        ((TextView) dialogView.findViewById(R.id.dialogDesc)).setText(achievement.description);
        ((TextView) dialogView.findViewById(R.id.dialogProgress)).setText(achievement.progress);
        ProgressBar progressBar = dialogView.findViewById(R.id.progressBar);
        progressBar.setProgress(0);
        Button okButton = dialogView.findViewById(R.id.okButton);
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