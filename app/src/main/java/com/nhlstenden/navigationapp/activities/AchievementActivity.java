package com.nhlstenden.navigationapp.activities;

import android.app.AlertDialog;
import android.content.Context;
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
import com.nhlstenden.navigationapp.helpers.AchievementManager;
import com.nhlstenden.navigationapp.helpers.CoinManager;

public class AchievementActivity extends BaseActivity {

    public enum Difficulty {
        ONE_STAR(R.drawable.star1),
        TWO_STAR(R.drawable.star2),
        THREE_STAR(R.drawable.star3);
        public final int starResId;
        Difficulty(int starResId) { this.starResId = starResId; }
    }

    public enum AchievementType {
        FIRST_STEPS("First steps", "Create a waypoint", Difficulty.ONE_STAR, 100) {
            @Override
            public AchievementProgress getProgress(Context context) {
                int progress = AchievementManager.getFirstStepsProgress(context);
                return new AchievementProgress(progress, 1, progress >= 1);
            }
        },
        RUNNER_I("Runner I", "Complete a waypoint", Difficulty.ONE_STAR, 200) {
            @Override
            public AchievementProgress getProgress(Context context) {
                int progress = AchievementManager.getRunnerIProgress(context);
                return new AchievementProgress(progress, 1, progress >= 1);
            }
        },
        RUNNER_II("Runner II", "Complete 5 waypoints", Difficulty.TWO_STAR, 1000) {
            @Override
            public AchievementProgress getProgress(Context context) {
                int progress = AchievementManager.getRunnerIIProgress(context);
                return new AchievementProgress(progress, 5, progress >= 5);
            }
        },
        RUNNER_III("Runner III", "Complete 10 waypoints", Difficulty.THREE_STAR, 5000) {
            @Override
            public AchievementProgress getProgress(Context context) {
                int progress = AchievementManager.getRunnerIIIProgress(context);
                return new AchievementProgress(progress, 10, progress >= 10);
            }
        },
        GRINDER_I("Grinder I", "Earn 1000 coins", Difficulty.ONE_STAR, 500) {
            @Override
            public AchievementProgress getProgress(Context context) {
                int progress = CoinManager.getCoins(context);
                return new AchievementProgress(progress, 1000, progress >= 1000);
            }
        },
        GRINDER_II("Grinder II", "Earn 10.000 coins", Difficulty.TWO_STAR, 2000) {
            @Override
            public AchievementProgress getProgress(Context context) {
                int progress = CoinManager.getCoins(context);
                return new AchievementProgress(progress, 10000, progress >= 10000);
            }
        },
        GRINDER_III("Grinder III", "Earn 100.000 coins", Difficulty.THREE_STAR, 10000) {
            @Override
            public AchievementProgress getProgress(Context context) {
                int progress = CoinManager.getCoins(context);
                return new AchievementProgress(progress, 100000, progress >= 100000);
            }
        },
        COLLECTIONISTA("Collectionista", "Collect all achievements", Difficulty.THREE_STAR, 10000) {
            @Override
            public AchievementProgress getProgress(Context context) {
                int progress = AchievementManager.getCollectionistaProgress(context);
                return new AchievementProgress(progress, 7, progress >= 7);
            }
        };

        public final String title;
        public final String description;
        public final Difficulty difficulty;
        public final int reward;

        AchievementType(String title, String description, Difficulty difficulty, int reward) {
            this.title = title;
            this.description = description;
            this.difficulty = difficulty;
            this.reward = reward;
        }

        public abstract AchievementProgress getProgress(Context context);
    }

    public static class AchievementProgress {
        public final int current;
        public final int max;
        public final boolean isCompleted;

        public AchievementProgress(int current, int max, boolean isCompleted) {
            this.current = current;
            this.max = max;
            this.isCompleted = isCompleted;
        }

        public String getProgressText() {
            return String.format("Progress: %d/%d", current, max);
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

        updateAchievementProgress();

        GridLayout container = findViewById(R.id.achievementContainer);
        int columnCount = 2;
        container.setColumnCount(columnCount);

        for (AchievementType type : AchievementType.values()) {
            View card = LayoutInflater.from(this).inflate(R.layout.item_achievement, container, false);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(12, 12, 12, 12);
            card.setLayoutParams(params);

            ((TextView) card.findViewById(R.id.achievementTitle)).setText(type.title);
            ((ImageView) card.findViewById(R.id.achievementTrophy)).setImageResource(R.drawable.trophy);
            ((ImageView) card.findViewById(R.id.achievementStars)).setImageResource(type.difficulty.starResId);
            
            AchievementProgress progress = type.getProgress(this);
            ImageView checkmark = card.findViewById(R.id.checked);
            checkmark.setVisibility(progress.isCompleted ? View.VISIBLE : View.GONE);
            
            card.setOnClickListener(v -> showAchievementDialog(type));
            container.addView(card);
        }
    }

    private void showAchievementDialog(AchievementType type) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_achievement, null);
        ((TextView) dialogView.findViewById(R.id.dialogText)).setText(type.title);
        ((TextView) dialogView.findViewById(R.id.dialogDesc)).setText(type.description);
        
        AchievementProgress progress = type.getProgress(this);
        ((TextView) dialogView.findViewById(R.id.dialogProgress)).setText(progress.getProgressText());
        
        ProgressBar progressBar = dialogView.findViewById(R.id.progressBar);
        progressBar.setMax(progress.max);
        progressBar.setProgress(progress.current);
        
        TextView coinReward = dialogView.findViewById(R.id.coinReward);
        coinReward.setText(String.valueOf(type.reward));
        
        Button okButton = dialogView.findViewById(R.id.okButton);
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setView(dialogView)
            .create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        
        if (progress.isCompleted) {
            okButton.setOnClickListener(v -> {
                if (!AchievementManager.isRewardClaimed(this, type.title)) {
                    CoinManager.addCoins(this, type.reward);
                    AchievementManager.markRewardClaimed(this, type.title);
                }
                dialog.dismiss();
            });
        } else {
            okButton.setOnClickListener(v -> dialog.dismiss());
        }
        
        dialog.show();
    }

    private void updateAchievementProgress() {
        GridLayout container = findViewById(R.id.achievementContainer);
        if (container == null) return;

        for (int i = 0; i < container.getChildCount(); i++) {
            View card = container.getChildAt(i);
            TextView titleView = card.findViewById(R.id.achievementTitle);
            if (titleView == null) continue;

            String title = titleView.getText().toString();
            ImageView checkmark = card.findViewById(R.id.checked);
            
            for (AchievementType type : AchievementType.values()) {
                if (type.title.equals(title)) {
                    AchievementProgress progress = type.getProgress(this);
                    checkmark.setVisibility(progress.isCompleted ? View.VISIBLE : View.GONE);
                    break;
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateAchievementProgress();
    }
}