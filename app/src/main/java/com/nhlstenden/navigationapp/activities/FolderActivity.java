package com.nhlstenden.navigationapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nhlstenden.navigationapp.R;
import com.nhlstenden.navigationapp.adapters.FolderAdapter;
import com.nhlstenden.navigationapp.interfaces.OnFolderClickListener;
import com.nhlstenden.navigationapp.models.Folder;
import com.nhlstenden.navigationapp.models.Waypoint;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class FolderActivity extends AppCompatActivity implements OnFolderClickListener {

    private RecyclerView recyclerView;
    private FolderAdapter folderAdapter;
    private List<Folder> folderList;
    private Button addFolderButton;
    private EditText folderNameInput;
    private static final String PREFS_NAME = "com.nhlstenden.navigationapp.PREFS";
    private static final String KEY_FOLDERS = "folders_json";

    private final ActivityResultLauncher<Intent> folderResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Folder updatedFolder = result.getData().getParcelableExtra("FOLDER");
                    if (updatedFolder != null) {
                        folderAdapter.updateFolder(updatedFolder);
                        for (int i = 0; i < folderList.size(); i++) {
                            if (folderList.get(i).getName().equals(updatedFolder.getName())) {
                                folderList.set(i, updatedFolder);
                                break;
                            }
                        }
                        saveFolders();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder);

        // Initialize views
        recyclerView = findViewById(R.id.recyclerViewFolders);
        addFolderButton = findViewById(R.id.addFolderButton);
        folderNameInput = findViewById(R.id.folderNameInput);

        // Folder list
        folderList = new ArrayList<>();
        loadFolders();

        folderAdapter = new FolderAdapter(folderList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(folderAdapter);

        // Add folder logic
        addFolderButton.setOnClickListener(v -> {
            String folderName = folderNameInput.getText().toString().trim();
            if (TextUtils.isEmpty(folderName)) {
                Toast.makeText(this, "Please enter a folder name", Toast.LENGTH_SHORT).show();
                return;
            }

            Folder folder = new Folder(folderName);
            folderList.add(folder);
            folderAdapter.notifyItemInserted(folderList.size() - 1);
            saveFolders();
            folderNameInput.setText("");

            onFolderClicked(folder); // open it right away
        });

        // Bottom Navigation handling
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.navigation_navigate); // highlight current

            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();

                if (id == R.id.navigation_back) {
                    onBackPressed();
                    return true;

                } else if (id == R.id.navigation_map) {
                    startActivity(new Intent(this, MapActivity.class));
                    return true;

                } else if (id == R.id.navigation_add) {
                    startActivity(new Intent(this, CreateWaypointActivity.class));
                    return true;

                } else if (id == R.id.navigation_navigate) {
                    if (!folderList.isEmpty() && !folderList.get(0).getWaypoints().isEmpty()) {
                        Waypoint first = folderList.get(0).getWaypoints().get(0);
                        Intent navIntent = new Intent(this, CompassActivity.class);
                        navIntent.putExtra("WAYPOINT", first);
                        startActivity(navIntent);
                    } else {
                        Toast.makeText(this, "No waypoints available to navigate", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }

                return false;
            });
        }
    }

    @Override
    public void onFolderClicked(Folder folder) {
        Intent intent = new Intent(this, WaypointActivity.class);
        intent.putExtra("FOLDER", folder);
        folderResultLauncher.launch(intent);
    }

    private void loadFolders() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String json = prefs.getString(KEY_FOLDERS, null);
        if (json != null) {
            Type type = new TypeToken<List<Folder>>() {}.getType();
            List<Folder> saved = new Gson().fromJson(json, type);
            folderList.clear();
            folderList.addAll(saved);
        }
    }

    private void saveFolders() {
        String json = new Gson().toJson(folderList);
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putString(KEY_FOLDERS, json)
                .apply();
    }
}
