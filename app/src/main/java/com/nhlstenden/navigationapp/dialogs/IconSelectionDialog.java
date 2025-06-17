package com.nhlstenden.navigationapp.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialog;

import com.nhlstenden.navigationapp.R;

public class IconSelectionDialog extends AppCompatDialog
{
    private String selectedIconName = "icon1";
    private int selectedColor = Color.BLACK;
    private String initialIconName = "icon1";
    private int initialColor = Color.BLACK;
    private OnIconSelectedListener listener;

    public interface OnIconSelectedListener
    {
        void onIconSelected(String iconName, int color);
    }

    public IconSelectionDialog(@NonNull Context context, String initialIconName, int initialColor, OnIconSelectedListener listener)
    {
        super(context);
        this.listener = listener;
        this.initialIconName = initialIconName;
        this.initialColor = initialColor;
        this.selectedIconName = initialIconName;
        this.selectedColor = initialColor;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_icon);

        // Set transparent background for the dialog window and control its size
        if (getWindow() != null)
        {
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
            int width = metrics.widthPixels;
            getWindow().setLayout((int) (width * 0.90), WindowManager.LayoutParams.WRAP_CONTENT);
        }

        // Initialize icon grid
        GridLayout iconGrid = findViewById(R.id.iconGrid);
        for (int i = 1; i <= 10; i++)
        {
            int iconId = getContext().getResources().getIdentifier("icon" + i, "id", getContext().getPackageName());
            ImageView iconView = findViewById(iconId);
            if (iconView != null)
            {
                int finalIconResId = getContext().getResources().getIdentifier("icon" + i, "drawable", getContext().getPackageName());
                int finalI = i;
                iconView.setOnClickListener(v ->
                {
                    selectedIconName = "icon" + finalI;
                    updateAllIconsColor(selectedColor);
                    updateSelectedIcon(finalI);
                });
            }
        }

        // Set initial selection based on initialIconName
        int initialIndex = 1;
        for (int i = 1; i <= 10; i++)
        {
            int resId = getContext().getResources().getIdentifier("icon" + i, "drawable", getContext().getPackageName());
            if (resId == getContext().getResources().getIdentifier(initialIconName, "drawable", getContext().getPackageName()))
            {
                initialIndex = i;
                break;
            }
        }
        updateSelectedIcon(initialIndex);
        updateAllIconsColor(initialColor);

        // Initialize color grid
        for (int i = 1; i <= 10; i++)
        {
            int colorId = getContext().getResources().getIdentifier("color" + i, "id", getContext().getPackageName());
            ImageView colorView = findViewById(colorId);
            if (colorView != null)
            {
                int finalColor = getColorForIndex(i);
                colorView.setOnClickListener(v ->
                {
                    selectedColor = finalColor;
                    updateAllIconsColor(selectedColor);
                });
            }
        }

        // OK button
        Button okButton = findViewById(R.id.okButton);
        okButton.setOnClickListener(v ->
        {
            if (listener != null)
            {
                listener.onIconSelected(selectedIconName, selectedColor);
            }
            dismiss();
        });
    }

    private void updateAllIconsColor(int color)
    {
        for (int i = 1; i <= 10; i++)
        {
            int iconId = getContext().getResources().getIdentifier("icon" + i, "id", getContext().getPackageName());
            ImageView iconView = findViewById(iconId);
            if (iconView != null)
            {
                iconView.setColorFilter(color);
            }
        }
    }

    private void updateSelectedIcon(int selectedIconIndex)
    {
        // Hide all selection indicators
        for (int i = 1; i <= 10; i++)
        {
            int selectedId = getContext().getResources().getIdentifier("icon" + i + "_selected", "id", getContext().getPackageName());
            ImageView selectedView = findViewById(selectedId);
            if (selectedView != null)
            {
                selectedView.setVisibility(View.GONE);
            }
        }

        // Show selection indicator for selected icon
        int selectedId = getContext().getResources().getIdentifier("icon" + selectedIconIndex + "_selected", "id", getContext().getPackageName());
        ImageView selectedView = findViewById(selectedId);
        if (selectedView != null)
        {
            selectedView.setVisibility(View.VISIBLE);
        }
    }

    private int getColorForIndex(int index)
    {
        switch (index)
        {
            case 1:
                return Color.parseColor("#0074d9"); // Blue
            case 2:
                return Color.parseColor("#39cccc"); // Light Blue
            case 3:
                return Color.parseColor("#2ecc40"); // Green
            case 4:
                return Color.parseColor("#ffdc00"); // Yellow
            case 5:
                return Color.parseColor("#ff851b"); // Orange
            case 6:
                return Color.parseColor("#ff4136"); // Red
            case 7:
                return Color.parseColor("#f012be"); // Pink
            case 8:
                return Color.parseColor("#b10dc9"); // Purple
            case 9:
                return Color.parseColor("#111111"); // Black
            case 10:
                return Color.parseColor("#DDDDDD"); // Light Gray
            default:
                return Color.BLACK;
        }
    }
} 