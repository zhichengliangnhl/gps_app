package com.nhlstenden.navigationapp.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.nhlstenden.navigationapp.R;
import com.nhlstenden.navigationapp.adapters.WaypointAdapter;
import com.nhlstenden.navigationapp.interfaces.OnWaypointClickListener;
import com.nhlstenden.navigationapp.models.Folder;
import com.nhlstenden.navigationapp.models.Waypoint;
import com.nhlstenden.navigationapp.utils.QRCodeUtils;
import com.journeyapps.barcodescanner.ScanOptions;
import com.journeyapps.barcodescanner.ScanContract;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class WaypointActivity extends AppCompatActivity implements OnWaypointClickListener {

    private Folder folder;
    private RecyclerView recyclerView;
    private WaypointAdapter adapter;
    private List<Waypoint> waypointList;

    private ActivityResultLauncher<Intent> createEditLauncher;
    private ActivityResultLauncher<Intent> mapLauncher;
    private ActivityResultLauncher<ScanOptions> qrScanner;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waypoint);

        folder = getIntent().getParcelableExtra("FOLDER");
        if (folder == null) {
            Toast.makeText(this, "No folder provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        waypointList = folder.getWaypoints();
        recyclerView = findViewById(R.id.rvWaypoints);
        adapter = new WaypointAdapter(waypointList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        qrScanner = registerForActivityResult(
                new ScanContract(),
                result -> {
                    if (result.getContents() != null) {
                        String encodedWaypoint = result.getContents();
                        Waypoint imported = Waypoint.decode(this, encodedWaypoint);
                        if (imported != null && imported.getName() != null) {
                            waypointList.add(imported);
                            adapter.updateList(waypointList);
                            Toast.makeText(this, "Waypoint imported!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Invalid or corrupted waypoint", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        createEditLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Waypoint w = result.getData().getParcelableExtra("WAYPOINT");
                        if (w != null) {
                            String mode = result.getData().getStringExtra("mode");
                            if ("edit".equals(mode)) {
                                adapter.updateWaypoint(w);
                            } else {
                                adapter.addWaypoint(w);
                            }
                        }
                    }
                });

        mapLauncher = registerForActivityResult(
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
                        createEditLauncher.launch(intent);
                    }
                });

        ImageView navBrush = findViewById(R.id.navBrush);
        ImageView navArrow = findViewById(R.id.navArrow);
        ImageView navTrophy = findViewById(R.id.navTrophy);

        navBrush.setOnClickListener(v -> {
            openCreateWaypoint();
        });

        navArrow.setOnClickListener(v -> {
            if (!waypointList.isEmpty()) {
                Intent intent = new Intent(this, CompassActivity.class);
                intent.putExtra("WAYPOINT", waypointList.get(0));
                startActivity(intent);
            } else {
                Toast.makeText(this, "No waypoints available", Toast.LENGTH_SHORT).show();
            }
        });

        navTrophy.setOnClickListener(v -> {
            onBackPressed();
        });

        Button btnImport = findViewById(R.id.btnImport);
        btnImport.setOnClickListener(v -> showImportDialog());
    }

    private void openCreateWaypoint() {
        Intent intent = new Intent(this, CreateWaypointActivity.class);
        intent.putExtra("mode", "create");
        intent.putExtra("id", java.util.UUID.randomUUID().toString());
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
        folder.getWaypoints().remove(waypoint);
        adapter.updateList(folder.getWaypoints());
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
        String encoded = waypoint.encode();
        if (encoded != null) {
            Bitmap qrBitmap = QRCodeUtils.generateQRCode(encoded);
            showQrBottomSheet(qrBitmap);
        } else {
            Toast.makeText(this, "Failed to encode waypoint", Toast.LENGTH_SHORT).show();
        }
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Import Waypoint Code");

        final EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton("Import", (dialog, which) -> {
            String code = input.getText().toString().trim();
            try {
                Waypoint wp = Waypoint.decode(this, code);
                if (wp != null && wp.getName() != null) {
                    waypointList.add(wp);
                    adapter.updateList(waypointList);
                    Toast.makeText(this, "Waypoint imported!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Invalid or corrupted waypoint", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Failed to import", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
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
}
