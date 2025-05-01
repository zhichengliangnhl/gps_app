package com.nhlstenden.navigationapp;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private EditText waypointName, waypointDesc;
    private Button addWaypointBtn;
    private ListView waypointListView;

    private ArrayList<String> waypointList;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        waypointName = findViewById(R.id.waypointName);
        waypointDesc = findViewById(R.id.waypointDesc);
        addWaypointBtn = findViewById(R.id.addWaypointBtn);
        waypointListView = findViewById(R.id.waypointListView);

        waypointList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, waypointList);
        waypointListView.setAdapter(adapter);

        addWaypointBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = waypointName.getText().toString().trim();
                String desc = waypointDesc.getText().toString().trim();
                if (!name.isEmpty() && !desc.isEmpty()) {
                    waypointList.add(name + ": " + desc);
                    adapter.notifyDataSetChanged();
                    waypointName.setText("");
                    waypointDesc.setText("");
                } else {
                    Toast.makeText(MainActivity.this, "Please enter both name and description", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
