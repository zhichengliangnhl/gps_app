package com.nhlstenden.navigationapp.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.nhlstenden.navigationapp.R;
import com.nhlstenden.navigationapp.models.Waypoint;

public class NavigationActivity extends AppCompatActivity {

    private Waypoint waypoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        // Check if a waypoint was passed
        if (getIntent().hasExtra("WAYPOINT")) {
            waypoint = (Waypoint) getIntent().getSerializableExtra("WAYPOINT");
            Toast.makeText(this, "Navigating to: " + waypoint.getName(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No waypoint provided", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}