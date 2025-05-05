package com.nhlstenden.navigationapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.nhlstenden.navigationapp.activities.CompassActivity;
import com.nhlstenden.navigationapp.activities.WaypointActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnCompass = findViewById(R.id.btnOpenCompass);
        Button btnWaypoints = findViewById(R.id.btnOpenWaypoints);

        btnCompass.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CompassActivity.class);
            startActivity(intent);
        });

        btnWaypoints.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, WaypointActivity.class);
            startActivity(intent);
        });
    }
}
