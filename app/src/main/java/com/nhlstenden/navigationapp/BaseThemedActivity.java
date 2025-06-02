package com.nhlstenden.navigationapp;

// BaseThemedActivity.java
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.nhlstenden.navigationapp.helpers.ThemeHelper;

public abstract class BaseThemedActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 👇 Apply theme before any UI loads
        setTheme(ThemeHelper.getThemeResId(ThemeHelper.getTheme(this)));
        super.onCreate(savedInstanceState);
    }
}
