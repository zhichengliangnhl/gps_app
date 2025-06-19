// AchievementActivity.java
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
import com.nhlstenden.navigationapp.enums.AchievementType;
import com.nhlstenden.navigationapp.models.AchievementProgress;
import com.nhlstenden.navigationapp.helpers.AchievementManager;
import com.nhlstenden.navigationapp.helpers.CoinManager;

public class AchievementActivity extends BaseActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_achievement);

        TextView headerTitle = this.findViewById(R.id.headerTitle);
        if (headerTitle != null)
        {
            headerTitle.setText("Treasure Trophies");
        }

        this.updateAchievementProgress();
        this.setupSettingsPanel();

        GridLayout container = this.findViewById(R.id.achievementContainer);
        container.setColumnCount(2);

        for (AchievementType type : AchievementType.values())
        {
            View card = LayoutInflater.from(this).inflate(R.layout.item_achievement, container, false);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(12, 12, 12, 12);
            card.setLayoutParams(params);

            ((TextView) card.findViewById(R.id.achievementTitle)).setText(type.getTitle());
            ((ImageView) card.findViewById(R.id.achievementTrophy)).setImageResource(R.drawable.trophy);
            ((ImageView) card.findViewById(R.id.achievementStars)).setImageResource(type.getDifficulty().starResId);

            AchievementProgress progress = type.getProgress(this);
            ImageView checkmark = card.findViewById(R.id.checked);
            checkmark.setVisibility(progress.isCompleted ? View.VISIBLE : View.GONE);

            card.setOnClickListener(v -> this.showAchievementDialog(type));
            container.addView(card);
        }
    }

    private void showAchievementDialog(AchievementType type)
    {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_achievement, null);
        ((TextView) dialogView.findViewById(R.id.dialogText)).setText(type.getTitle());
        ((TextView) dialogView.findViewById(R.id.dialogDesc)).setText(type.getDescription());

        AchievementProgress progress = type.getProgress(this);
        ((TextView) dialogView.findViewById(R.id.dialogProgress)).setText(progress.getProgressText());

        ProgressBar progressBar = dialogView.findViewById(R.id.progressBar);
        progressBar.setMax(progress.max);
        progressBar.setProgress(progress.current);

        TextView coinReward = dialogView.findViewById(R.id.coinReward);
        coinReward.setText(String.valueOf(type.getReward()));

        Button okButton = dialogView.findViewById(R.id.okButton);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null)
        {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        if (progress.isCompleted)
        {
            okButton.setOnClickListener(v ->
            {
                if (!AchievementManager.isRewardClaimed(this, type.getTitle()))
                {
                    CoinManager.addCoins(this, type.getReward());
                    AchievementManager.markRewardClaimed(this, type.getTitle());
                }
                dialog.dismiss();
            });
        }
        else
        {
            okButton.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }

    private void updateAchievementProgress()
    {
        GridLayout container = this.findViewById(R.id.achievementContainer);
        if (container == null) return;

        for (int i = 0; i < container.getChildCount(); i++)
        {
            View card = container.getChildAt(i);
            TextView titleView = card.findViewById(R.id.achievementTitle);
            if (titleView == null) continue;

            String title = titleView.getText().toString();
            ImageView checkmark = card.findViewById(R.id.checked);

            for (AchievementType type : AchievementType.values())
            {
                if (type.getTitle().equals(title))
                {
                    AchievementProgress progress = type.getProgress(this);
                    checkmark.setVisibility(progress.isCompleted ? View.VISIBLE : View.GONE);
                    break;
                }
            }
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        this.updateAchievementProgress();
    }
}
