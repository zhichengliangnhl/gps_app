package com.nhlstenden.navigationapp.activities;

import android.content.Intent;
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

import com.nhlstenden.navigationapp.R;
import com.nhlstenden.navigationapp.adapters.FolderAdapter;
import com.nhlstenden.navigationapp.interfaces.OnFolderClickListener;
import com.nhlstenden.navigationapp.models.Folder;

import java.util.ArrayList;
import java.util.List;

public class FolderActivity extends AppCompatActivity implements OnFolderClickListener {

    private RecyclerView recyclerView;
    private FolderAdapter folderAdapter;
    private List<Folder> folderList;
    private Button addFolderButton;
    private EditText folderNameInput;

    private final ActivityResultLauncher<Intent> folderResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Folder updatedFolder = result.getData().getParcelableExtra("FOLDER");
                    if (updatedFolder == null) return;

                    for (int i = 0; i < folderList.size(); i++) {
                        if (folderList.get(i).getName().equals(updatedFolder.getName())) {
                            folderList.set(i, updatedFolder);
                            folderAdapter.notifyItemChanged(i);
                            return;
                        }
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder);

        recyclerView = findViewById(R.id.recyclerViewFolders);
        addFolderButton = findViewById(R.id.addFolderButton);
        folderNameInput = findViewById(R.id.folderNameInput);

        folderList = new ArrayList<>();

        folderAdapter = new FolderAdapter(folderList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(folderAdapter);

        findViewById(R.id.backToCompassButton).setOnClickListener(v -> {
            Intent intent = new Intent(FolderActivity.this, CompassActivity.class);
            startActivity(intent);
        });

        addFolderButton.setOnClickListener(v -> {
            String folderName = folderNameInput.getText().toString().trim();
            if (TextUtils.isEmpty(folderName)) {
                Toast.makeText(this, "Please enter a folder name", Toast.LENGTH_SHORT).show();
                return;
            }

            Folder folder = new Folder(folderName);
            folderList.add(folder);
            folderAdapter.notifyItemInserted(folderList.size() - 1);
            folderNameInput.setText("");
        });
    }

    @Override
    public void onFolderClicked(Folder folder) {
        Intent intent = new Intent(this, WaypointActivity.class);
        intent.putExtra("FOLDER", folder); // now using Parcelable
        folderResultLauncher.launch(intent);
    }
}
