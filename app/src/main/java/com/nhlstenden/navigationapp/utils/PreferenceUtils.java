package com.nhlstenden.navigationapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.ScrollView;

public class PreferenceUtils
{
    private static final String PREF_NAME = "AppPrefs";
    private static final String KEY_SCROLL_Y = "brush_scroll_y";
    private static final String KEY_THEME = "selected_theme";

    public static void saveScrollPosition(ScrollView scrollView, Context context)
    {
        if (scrollView != null)
        {
            int y = scrollView.getScrollY();
            getPrefs(context).edit().putInt(KEY_SCROLL_Y, y).apply();
        }
    }

    public static void restoreScrollPosition(ScrollView scrollView, Context context)
    {
        int y = getPrefs(context).getInt(KEY_SCROLL_Y, 0);
        if (scrollView != null && y > 0)
        {
            scrollView.post(() -> scrollView.scrollTo(0, y));
            getPrefs(context).edit().remove(KEY_SCROLL_Y).apply();
        }
    }

    public static void setTheme(Context context, String theme)
    {
        getPrefs(context).edit().putString(KEY_THEME, theme).apply();
    }

    public static String getTheme(Context context)
    {
        return getPrefs(context).getString(KEY_THEME, "classic");
    }

    private static SharedPreferences getPrefs(Context context)
    {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
}
