package com.nhlstenden.navigationapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import android.app.AlertDialog;
import android.text.InputType;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.nhlstenden.navigationapp.activities.AchievementActivity;
import com.nhlstenden.navigationapp.activities.BrushActivity;
import com.nhlstenden.navigationapp.activities.CompassActivity;
import com.nhlstenden.navigationapp.helpers.AppSettings;
import com.nhlstenden.navigationapp.helpers.CoinManager;
import com.nhlstenden.navigationapp.helpers.ToastUtils;
import com.nhlstenden.navigationapp.helpers.AchievementManager;

public abstract class BaseActivity extends AppCompatActivity
{

    public static final int SIDE_PANEL_WIDTH = 600;
    public static final int SCRIM_COLOR = 0x88000000;

    private View sidePanel;
    private View touchInterceptor;
    private boolean isSidePanelVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        applyDynamicTheme();
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);

        TextView coinCounter = findViewById(R.id.coinCounter);
        if (coinCounter != null)
        {
            CoinManager.updateCoinDisplay(this, coinCounter);
        }

        setupBottomNavigation();
    }

    protected void setupSettingsPanel()
    {
        ImageView topBarSettingsIcon = findViewById(R.id.settingsIcon);
        if (topBarSettingsIcon != null)
        {
            topBarSettingsIcon.setOnClickListener(v -> toggleSidePanel());
        }

        ViewGroup root = (ViewGroup) getWindow().getDecorView();

        if (sidePanel != null)
            return;

        touchInterceptor = new View(this);
        touchInterceptor.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        touchInterceptor.setBackgroundColor(SCRIM_COLOR);
        touchInterceptor.setClickable(true);
        touchInterceptor.setVisibility(View.GONE);
        touchInterceptor.setOnClickListener(v -> toggleSidePanel());
        root.addView(touchInterceptor);

        sidePanel = getLayoutInflater().inflate(R.layout.side_panel_settings, root, false);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                SIDE_PANEL_WIDTH,
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.END);
        sidePanel.setLayoutParams(params);
        sidePanel.setVisibility(View.GONE);
        root.addView(sidePanel);

        ImageView panelSettingsIcon = sidePanel.findViewById(R.id.sidePanelSettingsIcon);
        if (panelSettingsIcon != null)
        {
            panelSettingsIcon.setOnClickListener(v -> toggleSidePanel());
        }

        bindToggle(R.id.btnToggleVibration, AppSettings.VIBRATION, "Vibration");
        bindToggle(R.id.btnToggleToast, AppSettings.TOAST_ENABLED, "Toast");
        bindToggle(R.id.btnToggleDistance, AppSettings.DISTANCE_DISPLAY, "Distance");

        // Add Reset App button logic
        Button btnResetApp = sidePanel.findViewById(R.id.btnResetApp);
        if (btnResetApp != null)
        {
            btnResetApp.setBackgroundTintList(null);
            btnResetApp.setOnClickListener(v ->
            {
                // Use the already declared 'root' variable from above
                View overlay = new View(this);
                overlay.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
                overlay.setBackgroundColor(0x88000000); // semi-transparent black
                overlay.setId(View.generateViewId());
                root.addView(overlay);
                // Use a custom themed dialog for better UI
                View dialogView = getLayoutInflater().inflate(R.layout.dialog_reset_confirm, null);
                final EditText input = dialogView.findViewById(R.id.editConfirmText);
                androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this,
                        R.style.Dialog_Rounded)
                        .setView(dialogView)
                        .setCancelable(true)
                        .create();
                Button btnCancel = dialogView.findViewById(R.id.btnCancel);
                Button btnConfirm = dialogView.findViewById(R.id.btnConfirm);
                btnCancel.setOnClickListener(v2 ->
                {
                    dialog.dismiss();
                });
                btnConfirm.setOnClickListener(v2 ->
                {
                    if ("clear".equalsIgnoreCase(input.getText().toString().trim()))
                    {
                        getSharedPreferences("coin_prefs", MODE_PRIVATE).edit().clear().apply();
                        AchievementManager.resetAchievements(this);
                        getSharedPreferences("AppPrefs", MODE_PRIVATE).edit().clear().apply();
                        getSharedPreferences("com.nhlstenden.navigationapp.PREFS", MODE_PRIVATE).edit().clear().apply();
                        androidx.preference.PreferenceManager.getDefaultSharedPreferences(this).edit().clear().apply();
                        this.getSharedPreferences("theme_purchase_prefs", MODE_PRIVATE).edit().clear().apply();
                        this.getSharedPreferences("arrow_purchase_prefs", MODE_PRIVATE).edit().clear().apply();
                        this.getSharedPreferences("theme_prefs", MODE_PRIVATE).edit().clear().apply();
                        ToastUtils.show(this, "App data reset! Restarting...", Toast.LENGTH_LONG);
                        dialog.dismiss();
                        recreate();
                    }
                    else
                    {
                        input.setError("You must type 'clear' to confirm.");
                    }
                });
                dialog.setOnDismissListener(d ->
                {
                    // Remove the overlay when dialog is dismissed
                    root.removeView(overlay);
                });
                dialog.show();
            });
        }

        // Add 500 Gold button logic
        Button btnAddGold = sidePanel.findViewById(R.id.btnAddGold);
        if (btnAddGold != null)
        {
            btnAddGold.setOnClickListener(v ->
            {
                CoinManager.addCoins(this, 500);
                ToastUtils.show(this, "+500 gold added!", Toast.LENGTH_SHORT);
                // Optionally update coin display if visible
                TextView coinCounter = findViewById(R.id.coinCounter);
                if (coinCounter != null)
                {
                    CoinManager.updateCoinDisplay(this, coinCounter);
                }
            });
        }
    }

    private void toggleSidePanel()
    {
        if (sidePanel == null)
        {
            setupSettingsPanel();
        }

        boolean show = !isSidePanelVisible;
        sidePanel.setVisibility(show ? View.VISIBLE : View.GONE);
        touchInterceptor.setVisibility(show ? View.VISIBLE : View.GONE);
        isSidePanelVisible = show;
    }

    private void bindToggle(int btnId, String prefKey, String label)
    {
        Button b = sidePanel.findViewById(btnId);
        if (b == null)
            return;

        updateToggleLabel(b, label, AppSettings.get(this, prefKey, true));
        b.setOnClickListener(v ->
        {
            boolean newState = !AppSettings.get(this, prefKey, true);
            AppSettings.set(this, prefKey, newState);
            updateToggleLabel(b, label, newState);
            ToastUtils.show(this, label + (newState ? " ON" : " OFF"), Toast.LENGTH_SHORT);
        });
    }

    private void updateToggleLabel(Button b, String label, boolean on)
    {
        b.setText(label + ": " + (on ? "ON" : "OFF"));
    }

    private void setupBottomNavigation()
    {
        ImageView navBrush = findViewById(R.id.navBrush);
        ImageView navArrow = findViewById(R.id.navArrow);
        ImageView navTrophy = findViewById(R.id.navTrophy);

        if (navBrush != null)
        {
            navBrush.setOnClickListener(v ->
            {
                if (!(this instanceof BrushActivity))
                {
                    Intent intent = new Intent(this, BrushActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            });
        }

        if (navArrow != null)
        {
            navArrow.setOnClickListener(v ->
            {
                if (!(this instanceof CompassActivity))
                {
                    Intent intent = new Intent(this, CompassActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            });
        }

        if (navTrophy != null)
        {
            navTrophy.setOnClickListener(v ->
            {
                if (!(this instanceof AchievementActivity))
                {
                    Intent intent = new Intent(this, AchievementActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            });
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        TextView coinCounter = findViewById(R.id.coinCounter);
        if (coinCounter != null)
        {
            CoinManager.updateCoinDisplay(this, coinCounter);
        }
    }

    private void applyDynamicTheme()
    {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String selectedTheme = prefs.getString("selected_theme", "classic");

        switch (selectedTheme)
        {
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
