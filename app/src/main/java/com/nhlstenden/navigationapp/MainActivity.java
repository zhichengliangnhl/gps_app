package com.nhlstenden.navigationapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.nhlstenden.navigationapp.activities.WaypointActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // redirect to WaypointActivity
        Intent intent = new Intent(this, WaypointActivity.class);
        startActivity(intent);
        finish(); // prevents this activity from staying in the back stack
    }
}
