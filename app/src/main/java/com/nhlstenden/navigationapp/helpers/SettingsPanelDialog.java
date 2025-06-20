package com.nhlstenden.navigationapp.helpers;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.nhlstenden.navigationapp.R;

public class SettingsPanelDialog {
    public static void show(Context context) {
        View sheetView = LayoutInflater.from(context).inflate(R.layout.side_panel_settings, null);
        AlertDialog dialog = new AlertDialog.Builder(context, R.style.RightSlideDialog)
                .setView(sheetView)
                .create();
        dialog.show();
    }
} 