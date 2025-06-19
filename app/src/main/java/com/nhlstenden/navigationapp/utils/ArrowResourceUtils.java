package com.nhlstenden.navigationapp.utils;

import android.content.Context;

public class ArrowResourceUtils {
    public static int getArrowResource(Context context, String colorName) {
        String arrowNumber;
        switch (colorName.toLowerCase()) {
            case "orange": arrowNumber = "1"; break;
            case "red": arrowNumber = "2"; break;
            case "yellow": arrowNumber = "3"; break;
            case "green": arrowNumber = "4"; break;
            case "cyan": arrowNumber = "5"; break;
            case "blue": arrowNumber = "6"; break;
            case "purple": arrowNumber = "7"; break;
            case "rose": arrowNumber = "8"; break;
            case "grey": arrowNumber = "9"; break;
            case "white": arrowNumber = "10"; break;
            default: arrowNumber = "1"; break;
        }
        return context.getResources().getIdentifier("arrow_" + arrowNumber, "drawable", context.getPackageName());
    }
} 