package com.nhlstenden.navigationapp.helpers;

import android.content.Context;
import android.widget.Toast;

public class ToastUtils
{
    public static final int SHORT = Toast.LENGTH_SHORT;
    public static final int LONG = Toast.LENGTH_LONG;

    public static void show(Context context, String message)
    {
        if (AppSettings.get(context, AppSettings.TOAST_ENABLED, true))
        {
            Toast.makeText(context.getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    public static void show(Context context, String message, int duration)
    {
        if (AppSettings.get(context, AppSettings.TOAST_ENABLED, true))
        {
            Toast.makeText(context.getApplicationContext(), message, duration).show();
        }
    }
}
