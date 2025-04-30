package activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import adapters.FolderAdapter; // ← UPDATED

import com.nhlstenden.navigationapp.R; // keep this import if your app package name is still com.nhlstenden.navigationapp

import java.util.ArrayList;
import java.util.List;

public class FolderActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FolderAdapter folderAdapter;
    private List<String> folderList;
    private Button addFolderButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder);

        recyclerView = findViewById(R.id.recyclerViewFolders);
        addFolderButton = findViewById(R.id.addFolderButton);

        folderList = new ArrayList<>();
        folderList.add("Example Folder 1");
        folderList.add("Example Folder 2");

        folderAdapter = new FolderAdapter(folderList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(folderAdapter);

        addFolderButton.setOnClickListener(v -> {
            folderList.add("New Folder " + (folderList.size() + 1));
            folderAdapter.notifyItemInserted(folderList.size() - 1);
        });
    }
}
