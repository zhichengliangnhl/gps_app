package com.nhlstenden.navigationapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.nhlstenden.navigationapp.activities.AchievementActivity;
import com.nhlstenden.navigationapp.activities.BrushActivity;
import com.nhlstenden.navigationapp.activities.CompassActivity;
import com.nhlstenden.navigationapp.helpers.CoinManager;

public abstract class BaseActivity extends AppCompatActivity {

    private View sidePanel;
    private boolean isSidePanelVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyDynamicTheme(); // apply early
        super.onCreate(savedInstanceState);
        // Note: You must call setupSettingsPanel() in subclasses AFTER setContentView()
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Update coin counter
        TextView coinCounter = findViewById(R.id.coinCounter);
        if (coinCounter != null) {
            CoinManager.updateCoinDisplay(this, coinCounter);
        }

        setupBottomNavigation();
    }

    protected void setupSettingsPanel() {
        ImageView settingsIcon = findViewById(R.id.settingsIcon);
        if (settingsIcon != null) {
            settingsIcon.setOnClickListener(v -> toggleSidePanel());
        }
    }

    private void toggleSidePanel() {
        ViewGroup root = (ViewGroup) getWindow().getDecorView();

        if (sidePanel == null) {
            sidePanel = getLayoutInflater().inflate(R.layout.side_panel_settings, root, false);

            int widthPx = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 300, getResources().getDisplayMetrics());

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    widthPx, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.END);

            sidePanel.setLayoutParams(params);
            sidePanel.setTranslationX(widthPx);  // start hidden offscreen
            sidePanel.setVisibility(View.GONE);
            root.addView(sidePanel);
        }

        if (isSidePanelVisible) {
            // Hide side panel
            sidePanel.setVisibility(View.GONE);
            isSidePanelVisible = false;
        } else {
            // Show side panel
            sidePanel.setVisibility(View.VISIBLE);
            sidePanel.setTranslationX(0);
            isSidePanelVisible = true;
        }
    }

    private void setupBottomNavigation() {
        ImageView navBrush = findViewById(R.id.navBrush);
        ImageView navArrow = findViewById(R.id.navArrow);
        ImageView navTrophy = findViewById(R.id.navTrophy);

        if (navBrush != null) {
            navBrush.setOnClickListener(v -> {
                if (!(this instanceof BrushActivity)) {
                    Intent intent = new Intent(this, BrushActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
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
    protected void onResume() {
        super.onResume();
        TextView coinCounter = findViewById(R.id.coinCounter);
        if (coinCounter != null) {
            CoinManager.updateCoinDisplay(this, coinCounter);
        }
    }

    private void applyDynamicTheme() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String selectedTheme = prefs.getString("selected_theme", "classic");

        switch (selectedTheme) {
            case "macha":
                setTheme(R.style.Theme_NavigationApp_Macha);
                break;
            case "savana":
                setTheme(R.style.Theme_NavigationApp_Savana);
                break;
            case "aqua":
                setTheme(R.style.Theme_NavigationApp_Aqua);
                break;
            case "lavander":
                setTheme(R.style.Theme_NavigationApp_Lavander);
                break;
            case "sunset":
                setTheme(R.style.Theme_NavigationApp_Sunset);
                break;
            case "navy":
                setTheme(R.style.Theme_NavigationApp_Navy);
                break;
            case "fakeHolland":
                setTheme(R.style.Theme_NavigationApp_FakeHolland);
                break;
            case "macchiato":
                setTheme(R.style.Theme_NavigationApp_Macchiato);
                break;
            case "cookieCream":
                setTheme(R.style.Theme_NavigationApp_CookieCream);
                break;
            default:
                setTheme(R.style.Theme_NavigationApp_Classic);
                break;
        }
    }
}
