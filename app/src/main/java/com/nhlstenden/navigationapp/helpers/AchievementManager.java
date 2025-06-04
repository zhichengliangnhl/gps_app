package com.nhlstenden.navigationapp.helpers;

import android.content.Context;
import android.content.SharedPreferences;

public class AchievementManager {
    private static final String PREF_NAME = "achievement_prefs";
    private static final String KEY_FIRST_STEPS = "first_steps_progress";

    public static void updateFirstStepsProgress(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        int current = prefs.getInt(KEY_FIRST_STEPS, 0);
        if (current < 1) {
            prefs.edit().putInt(KEY_FIRST_STEPS, 1).apply();
        }
    }

    public static int getFirstStepsProgress(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_FIRST_STEPS, 0);
    }

    public static void resetAchievements(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }
} 