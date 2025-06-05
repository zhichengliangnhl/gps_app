package com.nhlstenden.navigationapp.helpers;

import android.content.Context;
import android.content.SharedPreferences;

public class CoinManager {
    private static final String PREF_NAME = "coin_prefs";
    private static final String KEY_COIN_COUNT = "coin_count";

    public static void addCoins(Context context, int amount) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        int current = prefs.getInt(KEY_COIN_COUNT, 0);
        int newTotal = current + amount;
        prefs.edit().putInt(KEY_COIN_COUNT, newTotal).apply();
        
        // Update Grinder achievements
        AchievementManager.updateGrinderProgress(context, newTotal);
    }

    public static int getCoins(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_COIN_COUNT, 0);
    }

    public static void resetCoins(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_COIN_COUNT, 0).apply();
    }
}