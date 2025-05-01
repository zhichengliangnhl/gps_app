package com.nhlstenden.navigationapp.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nhlstenden.navigationapp.R;
import com.nhlstenden.navigationapp.adapters.WaypointAdapter;
import com.nhlstenden.navigationapp.models.Waypoint;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WaypointActivity extends AppCompatActivity implements WaypointAdapter.OnWaypointClickListener {

    private RecyclerView recyclerView;
    private WaypointAdapter adapter;
    private List<Waypoint> waypointList;
    private Button btnAddWaypoint;

    private final ActivityResultLauncher<Intent> createEditLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    String id = data.getStringExtra("id");
                    String name = data.getStringExtra("name");
                    String description = data.getStringExtra("description");
                    String imageUriString = data.getStringExtra("imageUri");
                    double lat = data.getDoubleExtra("lat", 0.0);
                    double lng = data.getDoubleExtra("lng", 0.0);
                    Uri imageUri = imageUriString != null ? Uri.parse(imageUriString) : null;

                    Waypoint newWaypoint = new Waypoint(id, name, description, imageUriString, lat, lng);
                    boolean updated = false;

                    for (int i = 0; i < waypointList.size(); i++) {
                        if (waypointList.get(i).getId().equals(id)) {
                            waypointList.set(i, newWaypoint);
                            updated = true;
                            break;
                        }
                    }

                    if (!updated) {
                        waypointList.add(newWaypoint);
                    }

                    adapter.updateList(waypointList);
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waypoint);

        recyclerView = findViewById(R.id.recyclerViewWaypoints);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        waypointList = new ArrayList<>();
        adapter = new WaypointAdapter(waypointList, this);
        recyclerView.setAdapter(adapter);

        btnAddWaypoint = findViewById(R.id.btnAddWaypoint);
        btnAddWaypoint.setOnClickListener(v -> openCreateWaypoint());
    }

    private void openCreateWaypoint() {
        Intent intent = new Intent(WaypointActivity.this, CreateWaypointActivity.class);
        intent.putExtra("mode", "create");
        intent.putExtra("id", UUID.randomUUID().toString());
        createEditLauncher.launch(intent);
    }

    @Override
    public void onEditClick(Waypoint waypoint) {
        Intent intent = new Intent(WaypointActivity.this, CreateWaypointActivity.class);
        intent.putExtra("mode", "edit");
        intent.putExtra("id", waypoint.getId());
        intent.putExtra("name", waypoint.getName());
        intent.putExtra("description", waypoint.getDescription());
        intent.putExtra("imageUri", waypoint.getImageUri());
        intent.putExtra("lat", waypoint.getLat());
        intent.putExtra("lng", waypoint.getLng());
        createEditLauncher.launch(intent);
    }

    @Override
    public void onDeleteClick(Waypoint waypoint) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Waypoint")
                .setMessage("Are you sure you want to delete this waypoint?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    waypointList.removeIf(w -> w.getId().equals(waypoint.getId()));
                    adapter.updateList(waypointList);
                    Toast.makeText(this, "Waypoint deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", null)
                .show();
    }
}
