package activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nhlstenden.navigationapp.R;

import java.util.ArrayList;
import java.util.List;

import adapters.WaypointAdapter;
import models.Waypoint;

public class WaypointActivity extends AppCompatActivity {

    private static final int CREATE_WAYPOINT_REQUEST = 1;

    private Button createWaypointButton;
    private RecyclerView recyclerView;
    private WaypointAdapter waypointAdapter;
    private List<Waypoint> waypointList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waypoint);

        createWaypointButton = findViewById(R.id.createWaypointButton);
        recyclerView = findViewById(R.id.recyclerViewWaypoints);

        waypointList = new ArrayList<>();
        waypointAdapter = new WaypointAdapter(waypointList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(waypointAdapter);

        createWaypointButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WaypointActivity.this, CreateWaypointActivity.class);
                startActivityForResult(intent, CREATE_WAYPOINT_REQUEST);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CREATE_WAYPOINT_REQUEST && resultCode == RESULT_OK && data != null) {
            Waypoint waypoint = (Waypoint) data.getSerializableExtra("WAYPOINT");
            if (waypoint != null) {
                waypointList.add(waypoint);
                waypointAdapter.notifyItemInserted(waypointList.size() - 1);
            }
        }
    }
}