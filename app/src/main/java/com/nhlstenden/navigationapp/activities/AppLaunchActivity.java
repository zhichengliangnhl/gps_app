package com.nhlstenden.navigationapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import com.nhlstenden.navigationapp.R;


public class AppLaunchActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Important: remove any background left over from system splash
        getWindow().setBackgroundDrawable(null);

        setContentView(R.layout.activity_app_launch);

        new Handler().postDelayed(() -> {
            startActivity(new Intent(this, CompassActivity.class));
            finish();
        }, 2000);
    }

}
