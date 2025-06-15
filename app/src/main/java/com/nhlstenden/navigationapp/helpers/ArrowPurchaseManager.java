package com.nhlstenden.navigationapp.helpers;

import android.content.Context;
import android.content.SharedPreferences;

public class ArrowPurchaseManager {
    private static final String PREF_NAME = "arrow_purchase_prefs";
    private static final String PREFIX_PURCHASED = "purchased_";
    private static final String SELECTED_ARROW = "selected_arrow";

    // Arrow prices in coins
    public static final int PRICE_RED = 50;
    public static final int PRICE_YELLOW = 50;
    public static final int PRICE_GREEN = 50;
    public static final int PRICE_CYAN = 50;
    public static final int PRICE_BLUE = 50;
    public static final int PRICE_PURPLE = 50;
    public static final int PRICE_ROSE = 50;
    public static final int PRICE_GREY = 50;
    public static final int PRICE_WHITE = 50;

    public static boolean isArrowPurchased(Context context, String arrowName) {
        if (arrowName.equals("orange")) {
            return true; // Orange arrow is free
        }
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(PREFIX_PURCHASED + arrowName, false);
    }

    public static int getArrowPrice(String arrowName) {
        switch (arrowName) {
            case "red":
                return PRICE_RED;
            case "yellow":
                return PRICE_YELLOW;
            case "green":
                return PRICE_GREEN;
            case "cyan":
                return PRICE_CYAN;
            case "blue":
                return PRICE_BLUE;
            case "purple":
                return PRICE_PURPLE;
            case "rose":
                return PRICE_ROSE;
            case "grey":
                return PRICE_GREY;
            case "white":
                return PRICE_WHITE;
            default:
                return Integer.MAX_VALUE;
        }
    }

    public static boolean purchaseArrow(Context context, String arrowName) {
        if (isArrowPurchased(context, arrowName)) {
            return true;
        }

        int price = getArrowPrice(arrowName);
        int currentCoins = CoinManager.getCoins(context);

        if (currentCoins >= price) {
            CoinManager.addCoins(context, -price);

            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            prefs.edit().putBoolean(PREFIX_PURCHASED + arrowName, true).apply();
            
            return true;
        }
        
        return false;
    }

    public static void setSelectedArrow(Context context, String arrowName) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(SELECTED_ARROW, arrowName).apply();
    }

    public static String getSelectedArrow(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(SELECTED_ARROW, "orange"); // Default to orange arrow
    }
} 