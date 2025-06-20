package com.nhlstenden.navigationapp.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nhlstenden.navigationapp.BaseActivity;
import com.nhlstenden.navigationapp.R;
import com.nhlstenden.navigationapp.adapters.WaypointAdapter;
import com.nhlstenden.navigationapp.dialogs.QrScannerBottomSheet;
import com.nhlstenden.navigationapp.helpers.ToastUtils;
import com.nhlstenden.navigationapp.interfaces.OnWaypointClickListener;
import com.nhlstenden.navigationapp.models.Folder;
import com.nhlstenden.navigationapp.models.Waypoint;
import com.nhlstenden.navigationapp.utils.QRCodeUtils;
import com.journeyapps.barcodescanner.ScanOptions;
import com.journeyapps.barcodescanner.ScanContract;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.ArrayList;

import androidx.core.graphics.Insets;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.shape.CornerFamily;

import android.content.res.ColorStateList;

import androidx.core.content.ContextCompat;

import com.nhlstenden.navigationapp.helpers.DeleteWaypointDialog;
import com.nhlstenden.navigationapp.helpers.ImportWaypointDialog;
import com.nhlstenden.navigationapp.helpers.ShareWaypointDialog;
import com.nhlstenden.navigationapp.helpers.WaypointPersistenceHelper;

public class WaypointActivity extends BaseActivity implements OnWaypointClickListener {

    private Folder folder;
    private RecyclerView recyclerView;
    private WaypointAdapter adapter;
    private List<Waypoint> waypointList;

    private ActivityResultLauncher<Intent> createEditLauncher;
    private ActivityResultLauncher<Intent> mapLauncher;
    private ActivityResultLauncher<ScanOptions> qrScanner;

    // Add a field to hold the last import EditText for QR callback
    private EditText lastImportEditText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_waypoint);

        this.folder = this.getIntent().getParcelableExtra("FOLDER");
        if (this.folder == null) {
            ToastUtils.show(this, "No folder provided", Toast.LENGTH_SHORT);
            this.finish();
            return;
        }

        View topBar = this.findViewById(R.id.top_bar);
        View bottomNav = this.findViewById(R.id.bottom_nav_container);
        ViewCompat.setOnApplyWindowInsetsListener(this.findViewById(android.R.id.content), (v, insets) -> {
            Insets systemInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            if (bottomNav != null) {
                bottomNav.setPadding(
                        bottomNav.getPaddingLeft(),
                        bottomNav.getPaddingTop(),
                        bottomNav.getPaddingRight(),
                        systemInsets.bottom);
            }
            return insets;
        });

        TextView headerTitle = topBar.findViewById(R.id.headerTitle);
        if (headerTitle != null) {
            headerTitle.setText(this.folder.getName());
        }

        this.setupSettingsPanel();

        this.waypointList = this.folder.getWaypoints();
        this.recyclerView = this.findViewById(R.id.rvWaypoints);
        this.adapter = new WaypointAdapter(this.waypointList, this);
        this.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        this.recyclerView.setAdapter(this.adapter);

        this.qrScanner = this.registerForActivityResult(
                new ScanContract(),
                result -> {
                    if (result.getContents() != null) {
                        String encodedWaypoint = result.getContents();
                        if (this.lastImportEditText != null && "awaiting_qr".equals(this.lastImportEditText.getTag())) {
                            this.lastImportEditText.setText(encodedWaypoint);
                            this.lastImportEditText.setTag(null);
                            this.showImportDialog();
                        } else {
                            Waypoint imported = Waypoint.decode(this, encodedWaypoint);
                            if (imported != null && imported.getName() != null) {
                                imported.setImported(true);
                                this.waypointList.add(imported);
                                this.adapter.updateList(this.waypointList);
                                ToastUtils.show(this, "Waypoint imported!", Toast.LENGTH_SHORT);
                                this.saveFolderToPrefs(this.folder);
                            } else {
                                ToastUtils.show(this, "Invalid or corrupted waypoint", Toast.LENGTH_SHORT);
                            }
                        }
                    }
                });

        Button btnAddWaypoint = this.findViewById(R.id.btnAddWaypoint);
        btnAddWaypoint.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateWaypointActivity.class);
            intent.putExtra("mode", "create");
            intent.putExtra("id", java.util.UUID.randomUUID().toString());
            this.createEditLauncher.launch(intent);
        });

        this.createEditLauncher = this.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Waypoint w = result.getData().getParcelableExtra("WAYPOINT");
                        if (w != null) {
                            String mode = result.getData().getStringExtra("mode");
                            if ("edit".equals(mode)) {
                                for (int i = 0; i < this.waypointList.size(); i++) {
                                    if (this.waypointList.get(i).getId().equals(w.getId())) {
                                        this.waypointList.set(i, w);
                                        break;
                                    }
                                }
                                this.adapter.notifyDataSetChanged();
                                this.saveFolderToPrefs(this.folder);
                            } else {
                                for (Waypoint wp : this.folder.getWaypoints()) {
                                    if (wp.getName().equalsIgnoreCase(w.getName())) {
                                        ToastUtils.show(this, "Waypoint name must be unique in this folder",
                                                Toast.LENGTH_SHORT);
                                        return;
                                    }
                                }
                                this.adapter.addWaypoint(w);
                                this.saveFolderToPrefs(this.folder);
                            }
                        }
                    }
                });

        this.mapLauncher = this.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        double lat = result.getData().getDoubleExtra("lat", 0);
                        double lng = result.getData().getDoubleExtra("lng", 0);

                        Intent intent = new Intent(this, CreateWaypointActivity.class);
                        intent.putExtra("mode", "create");
                        intent.putExtra("id", java.util.UUID.randomUUID().toString());
                        intent.putExtra("lat", lat);
                        intent.putExtra("lng", lng);
                        this.createEditLauncher.launch(intent);
                    }
                });

        ImageView navBrush = this.findViewById(R.id.navBrush);
        ImageView navArrow = this.findViewById(R.id.navArrow);
        ImageView navTrophy = this.findViewById(R.id.navTrophy);

        navBrush.setOnClickListener(v -> this.openCreateWaypoint());

        navArrow.setOnClickListener(v -> {
            if (!this.waypointList.isEmpty()) {
                Intent intent = new Intent(this, CompassActivity.class);
                intent.putExtra("WAYPOINT", this.waypointList.get(0));
                this.startActivity(intent);
            } else {
                ToastUtils.show(this, "No waypoints available", Toast.LENGTH_SHORT);
            }
        });

        navTrophy.setOnClickListener(v -> this.onBackPressed());

        Button btnImport = this.findViewById(R.id.btnImport);
        btnImport.setOnClickListener(v -> this.showImportDialog());
    }

    private void openCreateWaypoint() {
        Intent intent = new Intent(this, CreateWaypointActivity.class);
        intent.putExtra("mode", "create");
        intent.putExtra("id", java.util.UUID.randomUUID().toString());
        this.createEditLauncher.launch(intent);
    }

    @Override
    public void onNavigateClick(Waypoint waypoint) {
        this.saveSelectedWaypoint(waypoint);
        // Save selected folder name for CompassActivity display
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        prefs.edit().putString("selected_folder_name", folder.getName()).apply();
        Intent intent = new Intent(this, CompassActivity.class);
        intent.putExtra("WAYPOINT", waypoint);
        this.startActivity(intent);
    }

    @Override
    public void onEditClick(Waypoint waypoint) {
        Intent intent = new Intent(this, CreateWaypointActivity.class);
        intent.putExtra("mode", "edit");
        intent.putExtra("WAYPOINT", waypoint);
        this.createEditLauncher.launch(intent);
    }

    @Override
    public void onDeleteClick(Waypoint waypoint) {
        DeleteWaypointDialog.show(this, waypoint, folder, waypointList, deletedWaypoint -> {
            adapter.updateList(folder.getWaypoints());
            WaypointPersistenceHelper.saveFolder(this, folder);
        });
    }

    @Override
    public void onBackPressed() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("FOLDER", folder);
        setResult(RESULT_OK, resultIntent);
        super.onBackPressed();
    }

    @Override
    public void onShareClick(Waypoint waypoint) {
        ShareWaypointDialog.show(this, waypoint);
    }

    private void saveSelectedWaypoint(Waypoint waypoint) {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        prefs.edit().putString("selected_wp_id", waypoint.getId())
                .putString("selected_wp_name", waypoint.getName())
                .putString("selected_wp_lat", String.valueOf(waypoint.getLat()))
                .putString("selected_wp_lng", String.valueOf(waypoint.getLng()))
                .apply();
    }

    private void showQrBottomSheet(Bitmap qrBitmap) {
        View sheetView = getLayoutInflater().inflate(R.layout.activity_qr, null);
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(sheetView);

        ImageView qrImage = sheetView.findViewById(R.id.qrImage);
        qrImage.setImageBitmap(qrBitmap);

        sheetView.findViewById(R.id.txtManual).setVisibility(View.GONE);
        sheetView.findViewById(R.id.editLink).setVisibility(View.GONE);
        sheetView.findViewById(R.id.btnInsertLink).setVisibility(View.GONE);

        Button btnCancel = sheetView.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(v -> bottomSheetDialog.dismiss());

        bottomSheetDialog.show();
    }

    public void showImportDialog() {
        ImportWaypointDialog.show(this, importedWaypoint -> {
            importedWaypoint.setImported(true);
            waypointList.add(importedWaypoint);
                adapter.updateList(waypointList);
            WaypointPersistenceHelper.saveFolder(this, folder);
        });
    }

    public static String decodeBase64ToImageFile(Context context, String base64Data) {
        try {
            byte[] imageBytes = Base64.decode(base64Data, Base64.NO_WRAP);
            File file = new File(context.getFilesDir(), "shared_img_" + System.currentTimeMillis() + ".jpg");

            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(imageBytes);
                fos.flush();
            }

            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void finish() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("FOLDER", folder);
        setResult(RESULT_OK, resultIntent);
        super.finish();
    }

    private void saveFolderToPrefs(Folder folder) {
        WaypointPersistenceHelper.saveFolder(this, folder);
    }
}