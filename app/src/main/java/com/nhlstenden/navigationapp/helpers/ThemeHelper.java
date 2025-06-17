package com.nhlstenden.navigationapp.helpers;

import android.content.Context;

import com.nhlstenden.navigationapp.R;
import com.nhlstenden.navigationapp.enums.ThemeMode;

public class ThemeHelper {
    private static final String PREFS_NAME = "theme_prefs";
    private static final String KEY_THEME = "app_theme";

    public static void setTheme(Context context, ThemeMode mode) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_THEME, mode.name())
                .apply();
    }

    public static ThemeMode getTheme(Context context) {
        String name = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_THEME, ThemeMode.CLASSIC.name());
        return ThemeMode.valueOf(name);
    }
}