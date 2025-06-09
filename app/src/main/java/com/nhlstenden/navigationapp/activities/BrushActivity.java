package com.nhlstenden.navigationapp.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nhlstenden.navigationapp.BaseActivity;
import com.nhlstenden.navigationapp.R;

public class BrushActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brush);

        TextView headerTitle = findViewById(R.id.headerTitle);
        if (headerTitle != null) {
            headerTitle.setText("Treasure Themes");
        }

        LinearLayout themeClassic = findViewById(R.id.theme1);
        LinearLayout themeMacha = findViewById(R.id.theme2);
        LinearLayout themeCookieCream = findViewById(R.id.theme3);

        themeClassic.setOnClickListener(v -> {
            saveTheme("classic");
            recreate(); // or redirect to MainActivity
        });

        themeMacha.setOnClickListener(v -> {
            saveTheme("macha");
            recreate(); // or redirect to MainActivity
        });

        themeCookieCream.setOnClickListener(v -> {
            saveTheme("cookieCream");
            recreate(); // or redirect to MainActivity
        });
    }

    private void saveTheme(String themeName) {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        prefs.edit().putString("selected_theme", themeName).apply();
        Log.println(Log.DEBUG, "SELECTED_THEME",themeName);
    }
}
