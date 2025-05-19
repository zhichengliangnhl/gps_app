package com.nhlstenden.navigationapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.nhlstenden.navigationapp.R;
import com.nhlstenden.navigationapp.adapters.WaypointAdapter;
import com.nhlstenden.navigationapp.interfaces.OnWaypointClickListener;
import com.nhlstenden.navigationapp.models.Folder;
import com.nhlstenden.navigationapp.models.Waypoint;

import java.util.List;
import java.util.UUID;

public class WaypointActivity extends AppCompatActivity implements OnWaypointClickListener {

    private Folder folder;
    private RecyclerView recyclerView;
    private WaypointAdapter adapter;
    private List<Waypoint> waypointList;

    private ActivityResultLauncher<Intent> mapLauncher;
    private ActivityResultLauncher<Intent> createEditLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waypoint);

        // Get folder from intent
        folder = getIntent().getParcelableExtra("FOLDER");
        if (folder == null) {
            Toast.makeText(this, "No folder provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load waypoints
        waypointList = folder.getWaypoints();
        recyclerView = findViewById(R.id.rvWaypoints);
        adapter = new WaypointAdapter(waypointList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Launcher for creating/editing waypoint
        createEditLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Waypoint w = result.getData().getParcelableExtra("WAYPOINT");
                        String mode = result.getData().getStringExtra("mode");

                        if (w != null) {
                            if ("edit".equals(mode)) {
                                adapter.updateWaypoint(w);
                            } else {
                                folder.addWaypoint(w);
                                adapter.addWaypoint(w);
                            }
                        }
                    }
                });

        // Launcher to get location from map
        mapLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        double lat = result.getData().getDoubleExtra("lat", 0);
                        double lng = result.getData().getDoubleExtra("lng", 0);
                        launchCreateWaypointWithLocation(lat, lng);
                    }
                });

        // Setup bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation); // ID must match layout
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.navigation_map);

            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();

                if (id == R.id.navigation_back) {
                    startActivity(new Intent(this, FolderActivity.class));
                    finish();
                    return true;

                } else if (id == R.id.navigation_add) {
                    openCreateWaypoint();
                    return true;

                } else if (id == R.id.navigation_navigate) {
                    if (!waypointList.isEmpty()) {
                        Intent navIntent = new Intent(this, CompassActivity.class);
                        navIntent.putExtra("WAYPOINT", waypointList.get(0));
                        startActivity(navIntent);
                    } else {
                        Toast.makeText(this, "No waypoints available", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }

                return false;
            });
        }
    }

    private void openCreateWaypoint() {
        Intent intent = new Intent(this, CreateWaypointActivity.class);
        intent.putExtra("mode", "create");
        intent.putExtra("id", UUID.randomUUID().toString());
        createEditLauncher.launch(intent);
    }

    private void launchCreateWaypointWithLocation(double lat, double lng) {
        Intent intent = new Intent(this, CreateWaypointActivity.class);
        intent.putExtra("mode", "create");
        intent.putExtra("id", UUID.randomUUID().toString());
        intent.putExtra("lat", lat);
        intent.putExtra("lng", lng);
        createEditLauncher.launch(intent);
    }

    @Override
    public void onNavigateClick(Waypoint waypoint) {
        Intent intent = new Intent(this, CompassActivity.class);
        intent.putExtra("WAYPOINT", waypoint);
        startActivity(intent);
    }

    @Override
    public void onEditClick(Waypoint waypoint) {
        Intent intent = new Intent(this, CreateWaypointActivity.class);
        intent.putExtra("mode", "edit");
        intent.putExtra("WAYPOINT", waypoint);
        createEditLauncher.launch(intent);
    }

    @Override
    public void onDeleteClick(Waypoint waypoint) {
        folder.deleteWaypoint(waypoint);
        adapter.removeWaypoint(waypoint);
    }
}
