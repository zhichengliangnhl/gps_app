package com.nhlstenden.navigationapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.nhlstenden.navigationapp.BaseActivity;
import com.nhlstenden.navigationapp.R;
import com.nhlstenden.navigationapp.helpers.ThemePurchaseManager;
import com.nhlstenden.navigationapp.helpers.ToastUtils;
import com.nhlstenden.navigationapp.helpers.ArrowPurchaseManager;

import java.util.HashMap;
import java.util.Map;

public class BrushActivity extends BaseActivity {
    private static final String PREF_SCROLL_Y = "brush_scroll_y";
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brush);
        TextView headerTitle = findViewById(R.id.headerTitle);
        if (headerTitle != null) {
            headerTitle.setText("Treasure Themes");
        }
        setupSettingsPanel();

        // Find the ScrollView for later use
        scrollView = findViewById(R.id.scrollView);

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

    // Utility to lighten a color by a given factor (0..1, e.g. 0.3 for 30% lighter)
    private int lightenColor(int color, float factor) {
        int r = (int) ((Color.red(color) * (1 - factor) / 255f + factor) * 255);
        int g = (int) ((Color.green(color) * (1 - factor) / 255f + factor) * 255);
        int b = (int) ((Color.blue(color) * (1 - factor) / 255f + factor) * 255);
        return Color.rgb(r, g, b);
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

        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String selectedTheme = prefs.getString("selected_theme", "classic");
        boolean isSelected = themeName.equals(selectedTheme);

        if (priceLayout != null && priceText != null) {
            if (ThemePurchaseManager.isThemePurchased(this, themeName)) {
                priceLayout.setVisibility(View.GONE);
                layout.setAlpha(1.0f);
                if (isSelected) {
                    int themeColor = getThemePrimaryColor(themeName);
                    int lightColor = lightenColor(themeColor, 0.5f); // 50% lighter
                    layout.setBackgroundColor(lightColor);
                    layout.setForeground(getResources().getDrawable(R.drawable.selected_card_background));
                } else {
                    layout.setBackgroundResource(R.drawable.rounded_card_bg);
                    layout.setForeground(null);
                }
            } else {
                priceLayout.setVisibility(View.VISIBLE);
                int price = ThemePurchaseManager.getThemePrice(themeName);
                priceText.setText(String.valueOf(price));
                layout.setAlpha(0.7f);
                layout.setBackgroundResource(R.drawable.rounded_card_bg);
                layout.setForeground(null);
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

        String selectedArrow = ArrowPurchaseManager.getSelectedArrow(this);
        boolean isSelected = arrowName.equals(selectedArrow);

        if (priceLayout != null && priceText != null) {
            if (ArrowPurchaseManager.isArrowPurchased(this, arrowName)) {
                priceLayout.setVisibility(View.GONE);
                layout.setAlpha(1.0f);
                if (isSelected) {
                    int arrowColor = getArrowColor(arrowName);
                    int lightColor = lightenColor(arrowColor, 0.5f); // 50% lighter
                    layout.setBackgroundColor(lightColor);
                    layout.setForeground(getResources().getDrawable(R.drawable.selected_card_background));
                } else {
                    layout.setBackgroundResource(R.drawable.rounded_card_bg);
                    layout.setForeground(null);
                }
            } else {
                priceLayout.setVisibility(View.VISIBLE);
                int price = ArrowPurchaseManager.getArrowPrice(arrowName);
                priceText.setText(String.valueOf(price));
                layout.setAlpha(0.7f);
                layout.setBackgroundResource(R.drawable.rounded_card_bg);
                layout.setForeground(null);
            }
        }
    }

    private int getThemePrimaryColor(String themeName) {
        switch (themeName.toLowerCase()) {
            case "classic":
                return Color.parseColor("#a86c00");
            case "macha":
                return Color.parseColor("#687351");
            case "savana":
                return Color.parseColor("#EA2F14");
            case "aqua":
                return Color.parseColor("#4300FF");
            case "lavander":
                return Color.parseColor("#441752");
            case "sunset":
                return Color.parseColor("#4C3A51");
            case "navy":
                return Color.parseColor("#183B4E");
            case "fakeholland":
                return Color.parseColor("#000000");
            case "macchiato":
                return Color.parseColor("#3B3030");
            case "cookiecream":
                return Color.parseColor("#252422");
            default:
                return Color.parseColor("#a86c00"); // classic as default
        }
    }

    private int getArrowColor(String arrowName) {
        switch (arrowName.toLowerCase()) {
            case "orange":
                return Color.parseColor("#FF8C00"); // Dark orange
            case "red":
                return Color.parseColor("#DC143C"); // Crimson red
            case "yellow":
                return Color.parseColor("#FFD700"); // Gold
            case "green":
                return Color.parseColor("#32CD32"); // Lime green
            case "cyan":
                return Color.parseColor("#00CED1"); // Dark turquoise
            case "blue":
                return Color.parseColor("#4169E1"); // Royal blue
            case "purple":
                return Color.parseColor("#9370DB"); // Medium purple
            case "rose":
                return Color.parseColor("#FF69B4"); // Hot pink
            case "grey":
                return Color.parseColor("#696969"); // Dim grey
            case "white":
                return Color.parseColor("#F5F5F5"); // Light grey (since pure white might be too bright)
            default:
                return Color.parseColor("#FF8C00"); // Dark orange as default
        }
    }

    private void handleThemeClick(String themeName) {
        if (ThemePurchaseManager.isThemePurchased(this, themeName)) {
            saveScrollPosition();
            saveTheme(themeName);
            recreate();
        } else {
            if (ThemePurchaseManager.purchaseTheme(this, themeName)) {
                ToastUtils.show(this, "Theme purchased successfully!", Toast.LENGTH_SHORT);
                saveScrollPosition();
                saveTheme(themeName);
                recreate();
            } else {
                ToastUtils.show(this, "Not enough coins to purchase this theme!", Toast.LENGTH_SHORT);
            }
        }
    }

    private void handleArrowClick(String arrowName) {
        if (ArrowPurchaseManager.isArrowPurchased(this, arrowName)) {
            saveScrollPosition();
            ArrowPurchaseManager.setSelectedArrow(this, arrowName);
            recreate();
        } else {
            if (ArrowPurchaseManager.purchaseArrow(this, arrowName)) {
                ToastUtils.show(this, "Arrow purchased successfully!", Toast.LENGTH_SHORT);
                saveScrollPosition();
                ArrowPurchaseManager.setSelectedArrow(this, arrowName);
                recreate();
            } else {
                ToastUtils.show(this, "Not enough coins to purchase this arrow!", Toast.LENGTH_SHORT);
            }
        }
    }

    private void saveTheme(String themeName) {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        prefs.edit().putString("selected_theme", themeName).apply();
        Log.println(Log.DEBUG, "SELECTED_THEME", themeName);
    }

    private void saveScrollPosition() {
        if (scrollView != null) {
            int y = scrollView.getScrollY();
            getSharedPreferences("AppPrefs", MODE_PRIVATE).edit().putInt(PREF_SCROLL_Y, y).apply();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Restore scroll position if available
        scrollView = findViewById(R.id.scrollView);
        int y = getSharedPreferences("AppPrefs", MODE_PRIVATE).getInt(PREF_SCROLL_Y, 0);
        if (scrollView != null && y > 0) {
            scrollView.post(() -> scrollView.scrollTo(0, y));
            // Clear after restoring
            getSharedPreferences("AppPrefs", MODE_PRIVATE).edit().remove(PREF_SCROLL_Y).apply();
        }
        // Refresh the UI to show current selections
        refreshUI();
    }

    private void refreshUI() {
        // Refresh theme cards
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
            }
        }

        // Refresh arrow cards
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
            }
        }
    }
}
