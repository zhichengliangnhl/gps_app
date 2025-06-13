package com.nhlstenden.navigationapp.helpers;

import android.content.Context;
import androidx.preference.PreferenceManager;

public final class AppSettings {
    private AppSettings() {
    }

    public static final String VIBRATION = "vibration_enabled";
    public static final String TOAST_ENABLED = "toast_enabled";


    public static boolean get(Context ctx, String key, boolean def) {
        return PreferenceManager.getDefaultSharedPreferences(ctx).getBoolean(key, def);
    }

    public static void set(Context ctx, String key, boolean val) {
        PreferenceManager.getDefaultSharedPreferences(ctx).edit().putBoolean(key, val).apply();
    }
}