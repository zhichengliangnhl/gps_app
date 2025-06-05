package com.nhlstenden.navigationapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.nhlstenden.navigationapp.activities.AchievementActivity;
import com.nhlstenden.navigationapp.activities.CompassActivity;
import com.nhlstenden.navigationapp.enums.ThemeMode;
import com.nhlstenden.navigationapp.helpers.CoinManager;
import com.nhlstenden.navigationapp.helpers.ThemeHelper;

public abstract class BaseActivity extends AppCompatActivity {
    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Update coin counter
        TextView coinCounter = findViewById(R.id.coinCounter);
        if (coinCounter != null) {
            CoinManager.updateCoinDisplay(this, coinCounter);
        }

        ImageView navBrush = findViewById(R.id.navBrush);

        ImageView navArrow = findViewById(R.id.navArrow);
        ImageView navTrophy = findViewById(R.id.navTrophy);

        if (navBrush != null) {
            navBrush.setOnClickListener(v -> {
                String[] options = {"Classic", "Splash", "Retro"};
                new AlertDialog.Builder(this)
                        .setTitle("Choose Theme")
                        .setItems(options, (dialog, which) -> {
                            ThemeMode selected = ThemeMode.values()[which];
                            ThemeHelper.setTheme(this, selected);
                            recreate(); // Restart activity to apply theme
                        })
                        .show();
            });
        }

        if (navArrow != null) {
            navArrow.setOnClickListener(v -> {
                if (!(this instanceof CompassActivity)) {
                    Intent intent = new Intent(this, CompassActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            });
        }

        if (navTrophy != null) {
            navTrophy.setOnClickListener(v -> {
                if (!(this instanceof AchievementActivity)) {
                    Intent intent = new Intent(this, AchievementActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(ThemeHelper.getThemeResId(ThemeHelper.getTheme(this)));
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Update coin counter when activity resumes
        TextView coinCounter = findViewById(R.id.coinCounter);
        if (coinCounter != null) {
            CoinManager.updateCoinDisplay(this, coinCounter);
        }
    }
}