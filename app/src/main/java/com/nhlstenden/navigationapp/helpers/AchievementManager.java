package com.nhlstenden.navigationapp.helpers;

import android.content.Context;
import android.content.SharedPreferences;

public class AchievementManager
{
    private static final String PREF_NAME = "achievement_prefs";
    private static final String KEY_FIRST_STEPS = "first_steps_progress";
    private static final String KEY_RUNNER_I = "runner_i_progress";
    private static final String KEY_RUNNER_II = "runner_ii_progress";
    private static final String KEY_RUNNER_III = "runner_iii_progress";
    private static final String KEY_GRINDER_I = "grinder_i_progress";
    private static final String KEY_GRINDER_II = "grinder_ii_progress";
    private static final String KEY_GRINDER_III = "grinder_iii_progress";
    private static final String PREFIX_CLAIMED = "claimed_";
    private static final float COMPLETION_DISTANCE = 10.0f; // 10 meters
    private static final String KEY_LAST_COMPLETED_WAYPOINT = "last_completed_waypoint";
    private static final String KEY_COLLECTIONISTA = "collectionista_progress";
    private static final int TOTAL_ACHIEVEMENTS = 6; // First Steps, Runner I, Runner II, Runner III, Grinder I, Grinder II, Grinder III

    public static void updateFirstStepsProgress(Context context)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        int current = prefs.getInt(KEY_FIRST_STEPS, 0);
        if (current < 1)
        {
            prefs.edit().putInt(KEY_FIRST_STEPS, 1).apply();
            // Update Collectionista progress when First Steps is completed
            updateCollectionistaProgress(context);
        }
    }

    public static int getFirstStepsProgress(Context context)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_FIRST_STEPS, 0);
    }

    public static void updateCollectionistaProgress(Context context)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        int completedAchievements = 0;

        // Check First Steps
        if (getFirstStepsProgress(context) >= 1) completedAchievements++;

        // Check Runner achievements
        if (getRunnerIProgress(context) >= 1) completedAchievements++;
        if (getRunnerIIProgress(context) >= 5) completedAchievements++;
        if (getRunnerIIIProgress(context) >= 10) completedAchievements++;

        // Check Grinder achievements
        int coins = CoinManager.getCoins(context);
        if (coins >= 1000) completedAchievements++;
        if (coins >= 10000) completedAchievements++;
        if (coins >= 100000) completedAchievements++;

        prefs.edit().putInt(KEY_COLLECTIONISTA, completedAchievements).apply();
    }

    public static int getCollectionistaProgress(Context context)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_COLLECTIONISTA, 0);
    }

    public static void checkWaypointCompletion(Context context, float distance)
    {
        if (distance <= COMPLETION_DISTANCE)
        {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            SharedPreferences appPrefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);

            // Get the current waypoint ID from AppPrefs
            String currentWaypointId = appPrefs.getString("selected_wp_id", "");
            if (currentWaypointId.isEmpty())
            {
                return;
            }

            // Check if this waypoint was already completed
            String lastCompletedWaypoint = prefs.getString(KEY_LAST_COMPLETED_WAYPOINT, "");
            if (lastCompletedWaypoint.equals(currentWaypointId))
            {
                return; // This waypoint was already completed
            }

            // Mark this waypoint as completed
            prefs.edit().putString(KEY_LAST_COMPLETED_WAYPOINT, currentWaypointId).apply();

            // Update achievements
            int current = prefs.getInt(KEY_RUNNER_I, 0);
            if (current < 1)
            {
                prefs.edit().putInt(KEY_RUNNER_I, 1).apply();
            }

            int runnerIIProgress = prefs.getInt(KEY_RUNNER_II, 0);
            if (runnerIIProgress < 5)
            {
                prefs.edit().putInt(KEY_RUNNER_II, runnerIIProgress + 1).apply();
            }

            int runnerIIIProgress = prefs.getInt(KEY_RUNNER_III, 0);
            if (runnerIIIProgress < 10)
            {
                prefs.edit().putInt(KEY_RUNNER_III, runnerIIIProgress + 1).apply();
            }

            // Update Collectionista progress
            updateCollectionistaProgress(context);
        }
    }

    public static int getRunnerIProgress(Context context)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_RUNNER_I, 0);
    }

    public static int getRunnerIIProgress(Context context)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_RUNNER_II, 0);
    }

    public static int getRunnerIIIProgress(Context context)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_RUNNER_III, 0);
    }

    public static void updateGrinderProgress(Context context, int coins)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        // Update Grinder I (1000 coins)
        if (coins >= 1000)
        {
            prefs.edit().putInt(KEY_GRINDER_I, 1).apply();
        }

        // Update Grinder II (10000 coins)
        if (coins >= 10000)
        {
            prefs.edit().putInt(KEY_GRINDER_II, 1).apply();
        }

        // Update Grinder III (100000 coins)
        if (coins >= 100000)
        {
            prefs.edit().putInt(KEY_GRINDER_III, 1).apply();
        }

        // Update Collectionista progress
        updateCollectionistaProgress(context);
    }

    public static int getGrinderIProgress(Context context)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_GRINDER_I, 0);
    }

    public static int getGrinderIIProgress(Context context)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_GRINDER_II, 0);
    }

    public static int getGrinderIIIProgress(Context context)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_GRINDER_III, 0);
    }

    public static void resetAchievements(Context context)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }

    public static boolean isRewardClaimed(Context context, String achievementTitle)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(PREFIX_CLAIMED + achievementTitle, false);
    }

    public static void markRewardClaimed(Context context, String achievementTitle)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(PREFIX_CLAIMED + achievementTitle, true).apply();
    }
} 