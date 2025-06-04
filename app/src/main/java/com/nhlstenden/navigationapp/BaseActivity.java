package com.nhlstenden.navigationapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.nhlstenden.navigationapp.activities.AchievementActivity;
import com.nhlstenden.navigationapp.activities.CompassActivity;
import com.nhlstenden.navigationapp.helpers.ThemeHelper;

public abstract class BaseActivity extends AppCompatActivity {
    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        ImageView navBrush = findViewById(R.id.navBrush);
        ImageView navArrow = findViewById(R.id.navArrow);
        ImageView navTrophy = findViewById(R.id.navTrophy);

        if (navBrush != null) {
            navBrush.setOnClickListener(v ->
                    Toast.makeText(this, "Brush feature coming soon", Toast.LENGTH_SHORT).show()
            );
        }

        if (navArrow != null) {
            navArrow.setOnClickListener(v -> {
                Intent intent = new Intent(this, CompassActivity.class);
                startActivity(intent);
            });
        }

        if (navTrophy != null) {
            navTrophy.setOnClickListener(v -> {
                Intent intent = new Intent(this, AchievementActivity.class);
                startActivity(intent);
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(ThemeHelper.getThemeResId(ThemeHelper.getTheme(this)));
        super.onCreate(savedInstanceState);
    }
}