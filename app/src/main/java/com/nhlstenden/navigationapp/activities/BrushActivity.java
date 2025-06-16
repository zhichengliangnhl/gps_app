package com.nhlstenden.navigationapp.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RelativeLayout;

import com.nhlstenden.navigationapp.BaseActivity;
import com.nhlstenden.navigationapp.R;
import com.nhlstenden.navigationapp.helpers.ThemePurchaseManager;
import com.nhlstenden.navigationapp.helpers.ToastUtils;
import com.nhlstenden.navigationapp.helpers.ArrowPurchaseManager;

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
        setupSettingsPanel();

        // Handle themes
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
                updateThemeCardUI(layout, themeName);
                layout.setOnClickListener(v -> handleThemeClick(themeName));
            }
        }

        // Handle arrows
        Map<Integer, String> arrowMap = new HashMap<>();
        arrowMap.put(R.id.arrow1, "orange");
        arrowMap.put(R.id.arrow2, "red");
        arrowMap.put(R.id.arrow3, "yellow");
        arrowMap.put(R.id.arrow4, "green");
        arrowMap.put(R.id.arrow5, "cyan");
        arrowMap.put(R.id.arrow6, "blue");
        arrowMap.put(R.id.arrow7, "purple");
        arrowMap.put(R.id.arrow8, "rose");
        arrowMap.put(R.id.arrow9, "grey");
        arrowMap.put(R.id.arrow10, "white");

        for (Map.Entry<Integer, String> entry : arrowMap.entrySet()) {
            LinearLayout layout = findViewById(entry.getKey());
            String arrowName = entry.getValue();
            if (layout != null) {
                updateArrowCardUI(layout, arrowName);
                layout.setOnClickListener(v -> handleArrowClick(arrowName));
            }
        }
    }

    private void updateThemeCardUI(LinearLayout layout, String themeName) {
        RelativeLayout priceLayout = null;
        TextView priceText = null;
        
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            if (child instanceof RelativeLayout) {
                priceLayout = (RelativeLayout) child;
                for (int j = 0; j < priceLayout.getChildCount(); j++) {
                    View grandChild = priceLayout.getChildAt(j);
                    if (grandChild instanceof TextView) {
                        priceText = (TextView) grandChild;
                        break;
                    }
                }
                break;
            }
        }

        if (priceLayout != null && priceText != null) {
            if (ThemePurchaseManager.isThemePurchased(this, themeName)) {
                priceLayout.setVisibility(View.GONE);
                layout.setAlpha(1.0f);
            } else {
                priceLayout.setVisibility(View.VISIBLE);
                int price = ThemePurchaseManager.getThemePrice(themeName);
                priceText.setText(String.valueOf(price));
                layout.setAlpha(0.7f);
            }
        }
    }

    private void updateArrowCardUI(LinearLayout layout, String arrowName) {
        RelativeLayout priceLayout = null;
        TextView priceText = null;
        
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            if (child instanceof RelativeLayout) {
                priceLayout = (RelativeLayout) child;
                for (int j = 0; j < priceLayout.getChildCount(); j++) {
                    View grandChild = priceLayout.getChildAt(j);
                    if (grandChild instanceof TextView) {
                        priceText = (TextView) grandChild;
                        break;
                    }
                }
                break;
            }
        }

        if (priceLayout != null && priceText != null) {
            if (ArrowPurchaseManager.isArrowPurchased(this, arrowName)) {
                priceLayout.setVisibility(View.GONE);
                layout.setAlpha(1.0f);
            } else {
                priceLayout.setVisibility(View.VISIBLE);
                int price = ArrowPurchaseManager.getArrowPrice(arrowName);
                priceText.setText(String.valueOf(price));
                layout.setAlpha(0.7f);
            }
        }
    }

    private void handleThemeClick(String themeName) {
        if (ThemePurchaseManager.isThemePurchased(this, themeName)) {
            saveTheme(themeName);
            recreate();
        } else {
            if (ThemePurchaseManager.purchaseTheme(this, themeName)) {
                ToastUtils.show(this, "Theme purchased successfully!", Toast.LENGTH_SHORT);
                saveTheme(themeName);
                recreate();
            } else {
                ToastUtils.show(this, "Not enough coins to purchase this theme!", Toast.LENGTH_SHORT);
            }
        }
    }

    private void handleArrowClick(String arrowName) {
        if (ArrowPurchaseManager.isArrowPurchased(this, arrowName)) {
            ArrowPurchaseManager.setSelectedArrow(this, arrowName);
            recreate();
        } else {
            if (ArrowPurchaseManager.purchaseArrow(this, arrowName)) {
                Toast.makeText(this, "Arrow purchased successfully!", Toast.LENGTH_SHORT).show();
                ArrowPurchaseManager.setSelectedArrow(this, arrowName);
                recreate();
            } else {
                Toast.makeText(this, "Not enough coins to purchase this arrow!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveTheme(String themeName) {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        prefs.edit().putString("selected_theme", themeName).apply();
        Log.println(Log.DEBUG, "SELECTED_THEME", themeName);
    }
}
