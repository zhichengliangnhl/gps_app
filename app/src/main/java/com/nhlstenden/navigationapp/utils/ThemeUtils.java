package com.nhlstenden.navigationapp.utils;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nhlstenden.navigationapp.R;
import com.nhlstenden.navigationapp.helpers.ThemePurchaseManager;
import com.nhlstenden.navigationapp.helpers.ArrowPurchaseManager;

public class ThemeUtils
{
    public static void updateThemeCardUI(Context context, LinearLayout layout, String themeName, String selectedTheme)
    {
        RelativeLayout priceLayout = null;
        TextView priceText = null;

        for (int i = 0; i < layout.getChildCount(); i++)
        {
            View child = layout.getChildAt(i);
            if (child instanceof RelativeLayout)
            {
                priceLayout = (RelativeLayout) child;
                for (int j = 0; j < priceLayout.getChildCount(); j++)
                {
                    View grandChild = priceLayout.getChildAt(j);
                    if (grandChild instanceof TextView)
                    {
                        priceText = (TextView) grandChild;
                        break;
                    }
                }
                break;
            }
        }

        boolean isSelected = themeName.equals(selectedTheme);

        if (ThemePurchaseManager.isThemePurchased(context, themeName))
        {
            // Hide price layout if it exists
            if (priceLayout != null)
            {
                priceLayout.setVisibility(View.GONE);
            }
            layout.setAlpha(1.0f);
            
            if (isSelected)
            {
                int themeColor = getThemeColor(themeName);
                int lightColor = lightenColor(themeColor, 0.5f);
                android.graphics.drawable.GradientDrawable roundedBg = new android.graphics.drawable.GradientDrawable();
                roundedBg.setColor(lightColor);
                roundedBg.setCornerRadius(40 * context.getResources().getDisplayMetrics().density);
                layout.setBackground(roundedBg);
                layout.setForeground(context.getDrawable(R.drawable.selected_card_background));
            }
            else
            {
                layout.setBackgroundResource(R.drawable.rounded_card_bg);
                layout.setForeground(null);
            }
        }
        else
        {
            // Show price layout if it exists
            if (priceLayout != null && priceText != null)
            {
                priceLayout.setVisibility(View.VISIBLE);
                priceText.setText(String.valueOf(ThemePurchaseManager.getThemePrice(themeName)));
            }
            layout.setAlpha(0.7f);
            layout.setBackgroundResource(R.drawable.rounded_card_bg);
            layout.setForeground(null);
        }
    }

    public static void updateArrowCardUI(Context context, LinearLayout layout, String arrowName, String selectedArrow)
    {
        RelativeLayout priceLayout = null;
        TextView priceText = null;

        for (int i = 0; i < layout.getChildCount(); i++)
        {
            View child = layout.getChildAt(i);
            if (child instanceof RelativeLayout)
            {
                priceLayout = (RelativeLayout) child;
                for (int j = 0; j < priceLayout.getChildCount(); j++)
                {
                    View grandChild = priceLayout.getChildAt(j);
                    if (grandChild instanceof TextView)
                    {
                        priceText = (TextView) grandChild;
                        break;
                    }
                }

                break;
            }
        }

        boolean isSelected = arrowName.equals(selectedArrow);

        if (ArrowPurchaseManager.isArrowPurchased(context, arrowName))
        {
            // Hide price layout if it exists
            if (priceLayout != null)
            {
                priceLayout.setVisibility(View.GONE);
            }
            layout.setAlpha(1.0f);
            
            if (isSelected)
            {
                int arrowColor = getArrowColor(arrowName);
                int lightColor = lightenColor(arrowColor, 0.5f);
                android.graphics.drawable.GradientDrawable roundedBg = new android.graphics.drawable.GradientDrawable();
                roundedBg.setColor(lightColor);
                roundedBg.setCornerRadius(40 * context.getResources().getDisplayMetrics().density);
                layout.setBackground(roundedBg);
                layout.setForeground(context.getDrawable(R.drawable.selected_card_background));
            }
            else
            {
                layout.setBackgroundResource(R.drawable.rounded_card_bg);
                layout.setForeground(null);
            }
        }
        else
        {
            // Show price layout if it exists
            if (priceLayout != null && priceText != null)
            {
                priceLayout.setVisibility(View.VISIBLE);
                priceText.setText(String.valueOf(ArrowPurchaseManager.getArrowPrice(arrowName)));
            }
            layout.setAlpha(0.7f);
            layout.setBackgroundResource(R.drawable.rounded_card_bg);
            layout.setForeground(null);
        }
    }

    public static int lightenColor(int color, float factor)
    {
        int r = (int) ((Color.red(color) * (1 - factor) / 255f + factor) * 255);
        int g = (int) ((Color.green(color) * (1 - factor) / 255f + factor) * 255);
        int b = (int) ((Color.blue(color) * (1 - factor) / 255f + factor) * 255);
        return Color.rgb(r, g, b);
    }

    private static int getThemeColor(String theme)
    {
        switch (theme.toLowerCase())
        {
            case "classic":
                return Color.parseColor("#a86c00");
            case "macha":
                return Color.parseColor("#687351");
            case "savana":
                return Color.parseColor("#EA2F14");
            case "aqua":
                return Color.parseColor("#4300FF");
            case "lavander":
                return Color.parseColor("#441752");
            case "sunset":
                return Color.parseColor("#4C3A51");
            case "navy":
                return Color.parseColor("#183B4E");
            case "fakeholland":
                return Color.parseColor("#000000");
            case "macchiato":
                return Color.parseColor("#3B3030");
            case "cookiecream":
                return Color.parseColor("#252422");
            default:
                return Color.parseColor("#a86c00");
        }
    }

    private static int getArrowColor(String arrow)
    {
        switch (arrow.toLowerCase())
        {
            case "orange":
                return Color.parseColor("#FF8C00");
            case "red":
                return Color.parseColor("#DC143C");
            case "yellow":
                return Color.parseColor("#FFD700");
            case "green":
                return Color.parseColor("#32CD32");
            case "cyan":
                return Color.parseColor("#00CED1");
            case "blue":
                return Color.parseColor("#4169E1");
            case "purple":
                return Color.parseColor("#9370DB");
            case "rose":
                return Color.parseColor("#FF69B4");
            case "grey":
                return Color.parseColor("#696969");
            case "white":
                return Color.parseColor("#F5F5F5");
            default:
                return Color.parseColor("#FF8C00");
        }
    }
}
