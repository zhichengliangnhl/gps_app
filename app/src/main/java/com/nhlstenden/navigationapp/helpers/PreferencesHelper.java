package com.nhlstenden.navigationapp.helpers;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesHelper {
    private static final String PREFS_NAME = "AppPrefs";

    public static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static void saveString(Context context, String key, String value) {
        getPrefs(context).edit().putString(key, value).apply();
    }

    public static String getString(Context context, String key, String defValue) {
        return getPrefs(context).getString(key, defValue);
    }

    public static void saveLong(Context context, String key, long value) {
        getPrefs(context).edit().putLong(key, value).apply();
    }

    public static long getLong(Context context, String key, long defValue) {
        return getPrefs(context).getLong(key, defValue);
    }

    public static void saveBoolean(Context context, String key, boolean value) {
        getPrefs(context).edit().putBoolean(key, value).apply();
    }

    public static boolean getBoolean(Context context, String key, boolean defValue) {
        return getPrefs(context).getBoolean(key, defValue);
    }

    public static void remove(Context context, String key) {
        getPrefs(context).edit().remove(key).apply();
    }
} 