package com.nhlstenden.navigationapp.helpers;

import android.content.Context;
import android.content.SharedPreferences;

public class AchievementManager {
    private static final String PREF_NAME = "achievement_prefs";
    private static final String KEY_FIRST_STEPS = "first_steps_progress";
    private static final String KEY_RUNNER_I = "runner_i_progress";
    private static final String KEY_RUNNER_II = "runner_ii_progress";
    private static final String KEY_RUNNER_III = "runner_iii_progress";
    private static final float COMPLETION_DISTANCE = 10.0f; // 10 meters

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

    public static void checkWaypointCompletion(Context context, float distance) {
        if (distance <= COMPLETION_DISTANCE) {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            int current = prefs.getInt(KEY_RUNNER_I, 0);
            if (current < 1) {
                prefs.edit().putInt(KEY_RUNNER_I, 1).apply();
            }
            
            int runnerIIProgress = prefs.getInt(KEY_RUNNER_II, 0);
            if (runnerIIProgress < 5) {
                prefs.edit().putInt(KEY_RUNNER_II, runnerIIProgress + 1).apply();
            }
            
            int runnerIIIProgress = prefs.getInt(KEY_RUNNER_III, 0);
            if (runnerIIIProgress < 10) {
                prefs.edit().putInt(KEY_RUNNER_III, runnerIIIProgress + 1).apply();
            }
        }
    }

    public static int getRunnerIProgress(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_RUNNER_I, 0);
    }

    public static int getRunnerIIProgress(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_RUNNER_II, 0);
    }

    public static int getRunnerIIIProgress(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_RUNNER_III, 0);
    }

    public static void resetAchievements(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }
} 