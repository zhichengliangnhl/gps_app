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

public class WaypointActivity extends BaseActivity implements OnWaypointClickListener {

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

        View topBar = findViewById(R.id.top_bar);
        View bottomNav = findViewById(R.id.bottom_nav_container);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
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
            headerTitle.setText(folder.getName());
        }

        // Setup the base settings panel from BaseActivity
        setupSettingsPanel();

        // DON'T override the settings icon behavior - let BaseActivity handle it
        // The settings icon will now properly show the settings panel, not the import
        // panel

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
                            saveFolderToPrefs(folder);
                        } else {
                            Toast.makeText(this, "Invalid or corrupted waypoint", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        Button btnAddWaypoint = findViewById(R.id.btnAddWaypoint);
        btnAddWaypoint.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateWaypointActivity.class);
            intent.putExtra("mode", "create");
            intent.putExtra("id", java.util.UUID.randomUUID().toString());
            createEditLauncher.launch(intent);
        });

        createEditLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Waypoint w = result.getData().getParcelableExtra("WAYPOINT");
                        Log.d("WAYPOINT_EDIT",
                                "Edited waypoint: " + (w != null ? w.getName() : "null") + ", icon: "
                                        + (w != null ? w.getIconName() : "null") + ", color: "
                                        + (w != null ? w.getIconColor() : "null"));
                        if (w != null) {
                            String mode = result.getData().getStringExtra("mode");
                            if ("edit".equals(mode)) {
                                // Update the waypoint in the list (which is folder.getWaypoints())
                                for (int i = 0; i < waypointList.size(); i++) {
                                    if (waypointList.get(i).getId().equals(w.getId())) {
                                        waypointList.set(i, w);
                                        break;
                                    }
                                }
                                // Log the updated list
                                for (Waypoint wp : waypointList) {
                                    Log.d("WAYPOINT_LIST", "Waypoint: " + wp.getName() + ", icon: " + wp.getIconName()
                                            + ", color: " + wp.getIconColor());
                                }
                                adapter.notifyDataSetChanged();
                                saveFolderToPrefs(folder);
                            } else {
                                // Check for duplicate waypoint name in this folder
                                for (Waypoint wp : folder.getWaypoints()) {
                                    if (wp.getName().equalsIgnoreCase(w.getName())) {
                                        Toast.makeText(this, "Waypoint name must be unique in this folder",
                                                Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                }
                                adapter.addWaypoint(w);
                                saveFolderToPrefs(folder);
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
        saveSelectedWaypoint(waypoint);
        // Save selected folder name for CompassActivity display
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        prefs.edit().putString("selected_folder_name", folder.getName()).apply();
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
        saveFolderToPrefs(folder);
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
        if (encoded == null) {
            Toast.makeText(this, "Failed to encode waypoint", Toast.LENGTH_SHORT).show();
            return;
        }

        Bitmap qrBitmap = QRCodeUtils.generateQRCode(encoded);

        // Inflate the custom layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_share_waypoint, null);
        ImageView qrCodeImage = dialogView.findViewById(R.id.qrCodeImage);
        EditText importLink = dialogView.findViewById(R.id.importLink);
        Button btnCopy = dialogView.findViewById(R.id.btnCopy);
        Button btnShare = dialogView.findViewById(R.id.btnShare);

        // Set up the QR code with a fade-in animation
        qrCodeImage.setAlpha(0f);
        qrCodeImage.setImageBitmap(qrBitmap);
        qrCodeImage.animate()
                .alpha(1f)
                .setDuration(300)
                .setInterpolator(new android.view.animation.DecelerateInterpolator())
                .start();

        importLink.setText(encoded);

        // Show as a BottomSheetDialog with custom behavior
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        dialog.setContentView(dialogView);

        // Set the dialog to be draggable and dismissible
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        // Set up the bottom sheet behavior
        View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
            behavior.setDraggable(true);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }

        btnCopy.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Waypoint Link", encoded);
            clipboard.setPrimaryClip(clip);

            // Show a nice animation for the copy button
            String originalText = btnCopy.getText().toString();
            btnCopy.setText("Copied!");
            btnCopy.animate()
                    .scaleX(0.9f)
                    .scaleY(0.9f)
                    .setDuration(100)
                    .withEndAction(() -> btnCopy.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .withEndAction(() -> {
                                btnCopy.setText(originalText);
                            })
                            .start())
                    .start();

            Toast.makeText(this, "Link copied to clipboard", Toast.LENGTH_SHORT).show();
        });

        btnShare.setOnClickListener(v -> {
            // Add a small delay to show the button press animation
            v.postDelayed(() -> {
                try {
                    // Validate inputs
                    if (encoded == null || encoded.isEmpty()) {
                        Toast.makeText(this, "Invalid waypoint data", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (qrBitmap == null) {
                        Toast.makeText(this, "Failed to generate QR code", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Create a simple text share intent first
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Share Waypoint: " + waypoint.getName());
                    shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out this waypoint: " + encoded);

                    // Try to add the QR code image if possible
                    boolean imageAdded = false;
                    try {
                        File cachePath = new File(getCacheDir(), "images");
                        if (!cachePath.exists()) {
                            cachePath.mkdirs();
                        }

                        File imageFile = new File(cachePath, "qr_code.png");
                        FileOutputStream stream = new FileOutputStream(imageFile);
                        qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        stream.close();

                        // Get the URI for the image file
                        Uri contentUri = androidx.core.content.FileProvider.getUriForFile(
                                this,
                                getPackageName() + ".fileprovider",
                                imageFile);

                        // Add the image to the share intent
                        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        imageAdded = true;
                    } catch (Exception e) {
                        // If image sharing fails, just share text
                        Log.w("ShareWaypoint", "Failed to add image to share intent: " + e.getMessage());
                    }

                    // Check if there are apps that can handle this intent
                    if (shareIntent.resolveActivity(getPackageManager()) != null) {
                        startActivity(Intent.createChooser(shareIntent, "Share Waypoint"));
                        dialog.dismiss();
                    } else {
                        // Fallback: try to share just text
                        Intent textShareIntent = new Intent(Intent.ACTION_SEND);
                        textShareIntent.setType("text/plain");
                        textShareIntent.putExtra(Intent.EXTRA_TEXT, "Check out this waypoint: " + encoded);

                        if (textShareIntent.resolveActivity(getPackageManager()) != null) {
                            startActivity(Intent.createChooser(textShareIntent, "Share Waypoint"));
                            dialog.dismiss();
                        } else {
                            Toast.makeText(this, "No apps available to share", Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (Exception e) {
                    Log.e("ShareWaypoint", "Failed to share waypoint: " + e.getMessage());
                    Toast.makeText(this, "Failed to share waypoint", Toast.LENGTH_SHORT).show();
                }
            }, 100);
        });

        dialog.show();
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

    public void showImportLinkDialog() {
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
                    saveFolderToPrefs(folder);
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

    public void showImportDialog() {
        // Create a simple dialog with import options instead of using a side panel
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Import Waypoint");

        String[] options = { "Import from Code", "Scan QR Code" };
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0: // Import from Code
                    showImportLinkDialog();
                    break;
                case 1: // Scan QR Code
                    ScanOptions scanOptions = new ScanOptions();
                    scanOptions.setPrompt("Scan QR Code");
                    scanOptions.setCaptureActivity(PortraitCaptureActivity.class);
                    scanOptions.setOrientationLocked(true);
                    qrScanner.launch(scanOptions);
                    break;
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

    @Override
    public void finish() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("FOLDER", folder);
        setResult(RESULT_OK, resultIntent);
        super.finish();
    }

    private void saveFolderToPrefs(Folder folder) {
        SharedPreferences prefs = getSharedPreferences("com.nhlstenden.navigationapp.PREFS", MODE_PRIVATE);
        String json = prefs.getString("folders_json", null);
        List<Folder> folderList = new java.util.ArrayList<>();
        if (json != null) {
            Type type = new TypeToken<List<Folder>>() {
            }.getType();
            folderList = new Gson().fromJson(json, type);
        }
        boolean found = false;
        for (int i = 0; i < folderList.size(); i++) {
            if (folderList.get(i).getId().equals(folder.getId())) {
                folderList.set(i, folder);
                found = true;
                break;
            }
        }
        if (!found) {
            folderList.add(folder);
        }
        prefs.edit().putString("folders_json", new Gson().toJson(folderList)).apply();
    }
}