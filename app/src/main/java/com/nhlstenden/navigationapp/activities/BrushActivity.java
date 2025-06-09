package com.nhlstenden.navigationapp.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nhlstenden.navigationapp.BaseActivity;
import com.nhlstenden.navigationapp.R;

import java.util.HashMap;
import java.util.Map;

public class BrushActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brush);

        TextView headerTitle = findViewById(R.id.headerTitle);
        if (headerTitle != null) {
            headerTitle.setText("Treasure Themes");
        }

        Map<Integer, String> themeMap = new HashMap<>();
        themeMap.put(R.id.theme1, "classic");
        themeMap.put(R.id.theme2, "macha");
        themeMap.put(R.id.theme3, "savana");
        themeMap.put(R.id.theme4, "aqua");
        themeMap.put(R.id.theme5, "lavander");
        themeMap.put(R.id.theme6, "sunset");
        themeMap.put(R.id.theme7, "navy");
        themeMap.put(R.id.theme8, "fakeHolland");
        themeMap.put(R.id.theme9, "macchiato");
        themeMap.put(R.id.theme10, "cookieCream");

        for (Map.Entry<Integer, String> entry : themeMap.entrySet()) {
            LinearLayout layout = findViewById(entry.getKey());
            String themeName = entry.getValue();
            if (layout != null) {
                layout.setOnClickListener(v -> {
                    saveTheme(themeName);
                    recreate();
                });
            }
        }
    }

    private void saveTheme(String themeName) {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        prefs.edit().putString("selected_theme", themeName).apply();
        Log.println(Log.DEBUG, "SELECTED_THEME",themeName);
    }
}
