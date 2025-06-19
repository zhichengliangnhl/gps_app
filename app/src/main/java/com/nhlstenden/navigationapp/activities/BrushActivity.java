package com.nhlstenden.navigationapp.activities;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.nhlstenden.navigationapp.BaseActivity;
import com.nhlstenden.navigationapp.R;
import com.nhlstenden.navigationapp.helpers.ArrowPurchaseManager;
import com.nhlstenden.navigationapp.helpers.ThemePurchaseManager;
import com.nhlstenden.navigationapp.helpers.ToastUtils;
import com.nhlstenden.navigationapp.utils.IDMapper;
import com.nhlstenden.navigationapp.utils.PreferenceUtils;
import com.nhlstenden.navigationapp.utils.ThemeUtils;

import java.util.Map;

public class BrushActivity extends BaseActivity
{
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brush);

        TextView headerTitle = findViewById(R.id.headerTitle);
        if (headerTitle != null)
        {
            headerTitle.setText("Treasure Themes");
        }

        this.setupSettingsPanel();
        this.scrollView = findViewById(R.id.scrollView);

        this.setupThemes();
        this.setupArrows();
    }

    private void setupThemes()
    {
        Map<Integer, String> themeMap = IDMapper.getThemeMap();
        String selectedTheme = PreferenceUtils.getTheme(this);

        for (Map.Entry<Integer, String> entry : themeMap.entrySet())
        {
            LinearLayout layout = findViewById(entry.getKey());
            String themeName = entry.getValue();

            if (layout != null)
            {
                ThemeUtils.updateThemeCardUI(this, layout, themeName, selectedTheme);
                layout.setOnClickListener(v -> handleThemeClick(themeName));
            }
        }
    }

    private void setupArrows()
    {
        Map<Integer, String> arrowMap = IDMapper.getArrowMap();
        String selectedArrow = ArrowPurchaseManager.getSelectedArrow(this);

        for (Map.Entry<Integer, String> entry : arrowMap.entrySet())
        {
            LinearLayout layout = findViewById(entry.getKey());
            String arrowName = entry.getValue();

            if (layout != null)
            {
                ThemeUtils.updateArrowCardUI(this, layout, arrowName, selectedArrow);
                layout.setOnClickListener(v -> handleArrowClick(arrowName));
            }
        }
    }

    private void handleThemeClick(String themeName)
    {
        if (ThemePurchaseManager.isThemePurchased(this, themeName))
        {
            PreferenceUtils.saveScrollPosition(this.scrollView, this);
            PreferenceUtils.setTheme(this, themeName);
            this.recreate();
        }
        else if (ThemePurchaseManager.purchaseTheme(this, themeName))
        {
            ToastUtils.show(this, "Theme purchased successfully!", ToastUtils.SHORT);
            PreferenceUtils.saveScrollPosition(this.scrollView, this);
            PreferenceUtils.setTheme(this, themeName);
            this.recreate();
        }
        else
        {
            ToastUtils.show(this, "Not enough coins to purchase this theme!", ToastUtils.SHORT);
        }
    }

    private void handleArrowClick(String arrowName)
    {
        if (ArrowPurchaseManager.isArrowPurchased(this, arrowName))
        {
            PreferenceUtils.saveScrollPosition(this.scrollView, this);
            ArrowPurchaseManager.setSelectedArrow(this, arrowName);
            this.recreate();
        }
        else if (ArrowPurchaseManager.purchaseArrow(this, arrowName))
        {
            ToastUtils.show(this, "Arrow purchased successfully!", ToastUtils.SHORT);
            PreferenceUtils.saveScrollPosition(this.scrollView, this);
            ArrowPurchaseManager.setSelectedArrow(this, arrowName);
            this.recreate();
        }
        else
        {
            ToastUtils.show(this, "Not enough coins to purchase this arrow!", ToastUtils.SHORT);
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        this.scrollView = findViewById(R.id.scrollView);
        PreferenceUtils.restoreScrollPosition(this.scrollView, this);
        this.setupThemes();
        this.setupArrows();
    }
}
