package com.nhlstenden.navigationapp.activities;

import android.content.Intent;
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
import com.nhlstenden.navigationapp.interfaces.OnWaypointClickListener;
import com.nhlstenden.navigationapp.adapters.WaypointAdapter;
import com.nhlstenden.navigationapp.models.Folder;
import com.nhlstenden.navigationapp.models.Waypoint;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WaypointActivity extends AppCompatActivity implements OnWaypointClickListener {

    private RecyclerView recyclerView;
    private WaypointAdapter adapter;
    private List<Waypoint> waypointList;
    private Button btnAddWaypoint;
    private Folder folder; // ✅ store the folder reference

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

                    Waypoint newWaypoint = new Waypoint(id, name, description, imageUriString, lat, lng);
                    boolean updated = false;

                    for (int i = 0; i < waypointList.size(); i++) {
                        if (waypointList.get(i).getId().equals(id)) {
                            waypointList.set(i, newWaypoint);
                            updated = true;
                            Toast.makeText(this, "Waypoint updated successfully", Toast.LENGTH_SHORT).show();
                            break;
                        }
                    }

                    if (!updated) {
                        waypointList.add(newWaypoint);
                        Toast.makeText(this, "New waypoint added successfully", Toast.LENGTH_SHORT).show();
                    }

                    folder.getWaypoints().clear();
                    folder.getWaypoints().addAll(waypointList); // ✅ update folder reference if needed

                    adapter.updateList(waypointList);
                } else if (result.getResultCode() == RESULT_CANCELED) {
                    Toast.makeText(this, "Waypoint creation cancelled", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waypoint);

        folder = (Folder) getIntent().getSerializableExtra("FOLDER");
        if (folder == null) {
            Toast.makeText(this, "No folder provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        waypointList = new ArrayList<>(folder.getWaypoints());

        recyclerView = findViewById(R.id.recyclerViewWaypoints);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new WaypointAdapter(waypointList, this);
        recyclerView.setAdapter(adapter);

        btnAddWaypoint = findViewById(R.id.btnAddWaypoint);
        btnAddWaypoint.setOnClickListener(v -> openCreateWaypoint());

        Button btnNavigate = findViewById(R.id.btnNavigate);

        btnNavigate.setOnClickListener(v -> {
            if (!waypointList.isEmpty()) {
                Intent intent = new Intent(WaypointActivity.this, NavigationActivity.class);
                intent.putExtra("WAYPOINT", waypointList.get(0)); // Using first waypoint for navigation
                startActivity(intent);
            } else {
                Toast.makeText(this, "No waypoints available for navigation", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openCreateWaypoint() {
        Intent intent = new Intent(this, CreateWaypointActivity.class);
        intent.putExtra("mode", "create");
        intent.putExtra("id", UUID.randomUUID().toString());
        createEditLauncher.launch(intent);
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
        new AlertDialog.Builder(this)
                .setTitle("Delete Waypoint")
                .setMessage("Are you sure you want to delete this waypoint?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    waypointList.removeIf(w -> w.getId().equals(waypoint.getId()));
                    folder.getWaypoints().clear();
                    folder.getWaypoints().addAll(waypointList); // ✅ update folder
                    adapter.updateList(waypointList);
                    Toast.makeText(this, "Waypoint deleted successfully", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", null)
                .show();
    }
    @Override
    public void onNavigateClick(Waypoint waypoint) {
        Intent intent = new Intent(this, CompassActivity.class);
        intent.putExtra("WAYPOINT", waypoint);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("FOLDER", folder);
        setResult(RESULT_OK, resultIntent);
        super.onBackPressed();
    }

}
