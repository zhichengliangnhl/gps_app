package com.nhlstenden.navigationapp.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

public class ThemePurchaseManager
{
    private static final String PREF_NAME = "theme_purchase_prefs";
    private static final String PREFIX_PURCHASED = "purchased_";

    // Theme prices in coins
    public static final int PRICE_MACHA = 100;
    public static final int PRICE_SAVANA = 200;
    public static final int PRICE_AQUA = 200;
    public static final int PRICE_LAVANDER = 200;
    public static final int PRICE_SUNSET = 200;
    public static final int PRICE_NAVY = 200;
    public static final int PRICE_FAKE_HOLLAND = 500;
    public static final int PRICE_MACCHIATO = 500;
    public static final int PRICE_COOKIE_CREAM = 1000;

    public static boolean isThemePurchased(Context context, String themeName)
    {
        if (themeName.equals("classic"))
        {
            return true;
        }
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(PREFIX_PURCHASED + themeName, false);
    }

    public static int getThemePrice(String themeName)
    {
        switch (themeName)
        {
            case "macha":
                return PRICE_MACHA;
            case "savana":
                return PRICE_SAVANA;
            case "aqua":
                return PRICE_AQUA;
            case "lavander":
                return PRICE_LAVANDER;
            case "sunset":
                return PRICE_SUNSET;
            case "navy":
                return PRICE_NAVY;
            case "fakeHolland":
                return PRICE_FAKE_HOLLAND;
            case "macchiato":
                return PRICE_MACCHIATO;
            case "cookieCream":
                return PRICE_COOKIE_CREAM;
            default:
                return Integer.MAX_VALUE;
        }
    }

    public static boolean purchaseTheme(Context context, String themeName)
    {
        if (isThemePurchased(context, themeName))
        {
            return true;
        }

        int price = getThemePrice(themeName);
        int currentCoins = CoinManager.getCoins(context);

        if (currentCoins >= price)
        {
            CoinManager.addCoins(context, -price);

            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            prefs.edit().putBoolean(PREFIX_PURCHASED + themeName, true).apply();

            return true;
        }

        return false;
    }
} 