package com.nhlstenden.navigationapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.journeyapps.barcodescanner.ScanOptions;
import com.nhlstenden.navigationapp.BaseActivity;
import com.nhlstenden.navigationapp.R;
import com.nhlstenden.navigationapp.adapters.FolderAdapter;
import com.nhlstenden.navigationapp.helpers.ToastUtils;
import com.nhlstenden.navigationapp.interfaces.OnFolderClickListener;
import com.nhlstenden.navigationapp.models.Folder;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class FolderActivity extends BaseActivity implements OnFolderClickListener
{

    private RecyclerView recyclerView;
    private FolderAdapter folderAdapter;
    private List<Folder> folderList;
    private Button addFolderButton;
    private EditText folderNameInput;
    private static final String PREFS_NAME = "com.nhlstenden.navigationapp.PREFS";
    private static final String KEY_FOLDERS = "folders_json";

    private final ActivityResultLauncher<Intent> folderResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result ->
            {
                if (result.getResultCode() == RESULT_OK && result.getData() != null)
                {
                    Folder updatedFolder = result.getData().getParcelableExtra("FOLDER");
                    if (updatedFolder != null)
                    {
                        folderAdapter.updateFolder(updatedFolder);
                        for (int i = 0; i < folderList.size(); i++)
                        {
                            if (folderList.get(i).getId().equals(updatedFolder.getId()))
                            {
                                folderList.set(i, updatedFolder);
                                saveFolders();
                                break;
                            }
                        }
                    }
                }
            });

    private final ActivityResultLauncher<ScanOptions> qrScanner = registerForActivityResult(
            new com.journeyapps.barcodescanner.ScanContract(), result ->
            {
                if (result.getContents() != null)
                {
                    String encodedWaypoint = result.getContents();
                    Toast.makeText(this, "Scanned: " + encodedWaypoint, Toast.LENGTH_SHORT).show();
                    // Optional: Decode or handle it
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder);

        final View topBar = findViewById(R.id.top_bar);
        final View bottomNav = findViewById(R.id.bottom_nav_container);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) ->
        {
            Insets systemInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            if (bottomNav != null)
            {
                bottomNav.setPadding(
                        bottomNav.getPaddingLeft(),
                        bottomNav.getPaddingTop(),
                        bottomNav.getPaddingRight(),
                        systemInsets.bottom);
            }
            return insets;
        });

        ImageView settingsIcon = findViewById(R.id.settingsIcon);
        if (settingsIcon != null)
        {
            settingsIcon.setOnClickListener(v -> showSettingsPanel());
        }

        // Set top bar title
        TextView headerTitle = findViewById(R.id.headerTitle);
        if (headerTitle != null)
        {
            headerTitle.setText("Treasure Collections");
        }
        setupSettingsPanel();

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
        addFolderButton.setOnClickListener(v ->
        {
            String folderName = folderNameInput.getText().toString().trim();
            if (TextUtils.isEmpty(folderName))
            {
                Toast.makeText(this, "Please enter a folder name", Toast.LENGTH_SHORT);
                return;
            }
            for (Folder f : this.folderList)
            {
                if (f.getName().equalsIgnoreCase(folderName))
                {
                    Toast.makeText(this, "Folder name must be unique", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            Folder folder = new Folder(folderName);
            folderList.add(folder);
            folderAdapter.notifyItemInserted(folderList.size() - 1);
            saveFolders();
            folderNameInput.setText("");
            onFolderClicked(folder); // Open folder immediately
        });
    }

    @Override
    public void onFolderClicked(Folder folder)
    {
        Intent intent = new Intent(this, WaypointActivity.class);
        intent.putExtra("FOLDER", folder);
        folderResultLauncher.launch(intent);
    }

    @Override
    public void onEditFolder(Folder folder)
    {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_folder, null);
        EditText editFolderName = dialogView.findViewById(R.id.editFolderName);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnSave = dialogView.findViewById(R.id.btnSave);
        editFolderName.setText(folder.getName());
        editFolderName.requestFocus();
        editFolderName.setSelection(editFolderName.getText().length());

        com.google.android.material.bottomsheet.BottomSheetDialog dialog = new com.google.android.material.bottomsheet.BottomSheetDialog(
                this, R.style.Dialog_Rounded);
        dialog.setContentView(dialogView);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet != null)
        {
            com.google.android.material.bottomsheet.BottomSheetBehavior<View> behavior = com.google.android.material.bottomsheet.BottomSheetBehavior
                    .from(bottomSheet);
            behavior.setDraggable(true);
            behavior.setState(com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED);
        }
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v ->
        {
            String newName = editFolderName.getText().toString().trim();
            if (!TextUtils.isEmpty(newName))
            {
                folder.setName(newName);
                folderAdapter.notifyDataSetChanged();
                saveFolders();
                dialog.dismiss();
            }
            else
            {
                editFolderName.setError("Folder name cannot be empty");
            }
        });
        btnCancel.setBackgroundTintList(null);
        btnSave.setBackgroundTintList(null);
        dialog.show();
        if (dialog.getWindow() != null)
        {
            dialog.getWindow().setDimAmount(0.6f);
        }
        if (bottomSheet != null)
        {
            bottomSheet.setBackgroundResource(android.R.color.transparent);
            int desiredHeight = (int) (getResources().getDisplayMetrics().heightPixels * 0.4); // 60% of screen
            bottomSheet.getLayoutParams().height = desiredHeight;
            bottomSheet.requestLayout();
            com.google.android.material.bottomsheet.BottomSheetBehavior<View> behavior = com.google.android.material.bottomsheet.BottomSheetBehavior
                    .from(bottomSheet);
            behavior.setState(com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED);
            behavior.setSkipCollapsed(true);
        }
    }

    @Override
    public void onDeleteFolder(Folder folder)
    {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_delete_folder, null);
        TextView previewFolderName = dialogView.findViewById(R.id.previewFolderName);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnDelete = dialogView.findViewById(R.id.btnDelete);
        previewFolderName.setText(folder.getName());

        com.google.android.material.bottomsheet.BottomSheetDialog dialog = new com.google.android.material.bottomsheet.BottomSheetDialog(
                this, R.style.Dialog_Rounded);
        dialog.setContentView(dialogView);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet != null)
        {
            com.google.android.material.bottomsheet.BottomSheetBehavior<View> behavior = com.google.android.material.bottomsheet.BottomSheetBehavior
                    .from(bottomSheet);
            behavior.setDraggable(true);
            behavior.setState(com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED);
        }
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnDelete.setOnClickListener(v ->
        {
            this.folderList.remove(folder);
            this.folderAdapter.notifyDataSetChanged();
            this.saveFolders();
            Toast.makeText(this, "Folder deleted", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        btnCancel.setBackgroundTintList(null);
        btnDelete.setBackgroundTintList(null);
        dialog.show();
        if (dialog.getWindow() != null)
        {
            dialog.getWindow().setDimAmount(0.6f);
        }
        if (bottomSheet != null)
        {
            bottomSheet.setBackgroundResource(android.R.color.transparent);
            int desiredHeight = (int) (getResources().getDisplayMetrics().heightPixels * 0.6); // 60% of screen
            bottomSheet.getLayoutParams().height = desiredHeight;
            bottomSheet.requestLayout();
            com.google.android.material.bottomsheet.BottomSheetBehavior<View> behavior = com.google.android.material.bottomsheet.BottomSheetBehavior
                    .from(bottomSheet);
            behavior.setState(com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED);
            behavior.setSkipCollapsed(true);
        }
    }

    @Override
    public void onShareFolder(Folder folder)
    {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Check out my folder");
        intent.putExtra(Intent.EXTRA_TEXT, "Folder: " + folder.getName());
        startActivity(Intent.createChooser(intent, "Share Folder via"));
    }

    private void loadFolders()
    {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String json = prefs.getString(KEY_FOLDERS, null);
        if (json != null)
        {
            Type type = new TypeToken<List<Folder>>()
            {
            }.getType();
            List<Folder> saved = new Gson().fromJson(json, type);
            folderList.clear();
            folderList.addAll(saved);
        }
    }

    private void saveFolders()
    {
        String json = new Gson().toJson(folderList);
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putString(KEY_FOLDERS, json)
                .apply();
    }

    private void showSettingsPanel()
    {
        View sidePanelView = getLayoutInflater().inflate(R.layout.side_panel_settings, null);

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.RightSlideDialog)
                .setView(sidePanelView)
                .create();

        dialog.show();
    }

}
