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
    public void onDeleteClick(Waypoint waypoint)
    {
        this.showDeleteConfirmationDialog(waypoint);
    }

    private void showDeleteConfirmationDialog(Waypoint waypoint) {
        // Inflate the custom bottom sheet layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_delete_waypoint, null);

        // Get dialog elements
        ImageView previewWaypointIcon = dialogView.findViewById(R.id.previewWaypointIcon);
        ImageView previewWaypointCrown = dialogView.findViewById(R.id.previewWaypointCrown);
        ImageView previewWaypointImport = dialogView.findViewById(R.id.previewWaypointImport);
        TextView previewWaypointName = dialogView.findViewById(R.id.previewWaypointName);
        TextView previewWaypointDescription = dialogView.findViewById(R.id.previewWaypointDescription);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnDelete = dialogView.findViewById(R.id.btnDelete);

        // Set waypoint details in preview
        previewWaypointName.setText(waypoint.getName());
        previewWaypointDescription.setText(waypoint.getDescription());

        // Set icon and color
        int iconResId = getResources().getIdentifier(waypoint.getIconName(), "drawable", getPackageName());
        if (iconResId != 0) {
            previewWaypointIcon.setImageResource(iconResId);
            previewWaypointIcon.setColorFilter(waypoint.getIconColor());
        }

        // Check if waypoint is completed
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        boolean completed = false;
        if (waypoint.getId() != null) {
            completed = prefs.getBoolean("waypoint_completed_" + waypoint.getId(), false);
        }

        // Show appropriate status indicators
        if (completed) {
            previewWaypointCrown.setVisibility(View.VISIBLE);
            previewWaypointImport.setVisibility(View.GONE);
        } else if (waypoint.isImported()) {
            previewWaypointCrown.setVisibility(View.GONE);
            previewWaypointImport.setVisibility(View.VISIBLE);
        } else {
            previewWaypointCrown.setVisibility(View.GONE);
            previewWaypointImport.setVisibility(View.GONE);
        }

        // Create and configure the dialog
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.Dialog_Rounded);
        dialog.setContentView(dialogView);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        // Configure bottom sheet behavior
        View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
            behavior.setDraggable(true);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }

        // Set up button click listeners
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnDelete.setOnClickListener(v -> {
            // Actually delete the waypoint
            folder.getWaypoints().remove(waypoint);
            adapter.updateList(folder.getWaypoints());
            saveFolderToPrefs(folder);

            // Show success message
            ToastUtils.show(this, "Waypoint deleted", Toast.LENGTH_SHORT);

            dialog.dismiss();
        });

        // Remove background tint from buttons to preserve custom styling
        btnCancel.setBackgroundTintList(null);
        btnDelete.setBackgroundTintList(null);

        dialog.show();
        // Set a stronger dim and remove default white background
        if (dialog.getWindow() != null) {
            dialog.getWindow().setDimAmount(0.6f); // Stronger dim
        }
        if (bottomSheet != null) {
            bottomSheet.setBackgroundResource(android.R.color.transparent);
        }
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
            ToastUtils.show(this, "Failed to encode waypoint", Toast.LENGTH_SHORT);
            return;
        }

        // Generate and display the QR code in the dialog
        Bitmap qrBitmap = QRCodeUtils.generateQRCode(encoded);

        // Inflate the custom layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_share_waypoint, null);
        ImageView qrCodeImage = dialogView.findViewById(R.id.qrCodeImage);
        EditText importLink = dialogView.findViewById(R.id.importLink);
        Button btnCopy = dialogView.findViewById(R.id.btnCopy);
        Button btnShare = dialogView.findViewById(R.id.btnShare);

        // Display the QR code in the dialog
        if (qrBitmap != null && qrCodeImage != null) {
            qrCodeImage.setImageBitmap(qrBitmap);
        }

        importLink.setText(encoded);

        // Show as a BottomSheetDialog with custom behavior
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.Dialog_Rounded);
        dialog.setContentView(dialogView);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
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
            ToastUtils.show(this, "Link copied to clipboard", Toast.LENGTH_SHORT);
        });

        btnShare.setOnClickListener(v -> {
            v.postDelayed(() -> {
                try {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_TEXT, encoded); // Only the link
                    startActivity(Intent.createChooser(shareIntent, "Share Waypoint"));
                    dialog.dismiss();
                } catch (Exception e) {
                    Log.e("ShareWaypoint", "Failed to share waypoint: " + e.getMessage());
                    ToastUtils.show(this, "Failed to share waypoint", Toast.LENGTH_SHORT);
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

    public void showImportDialog() {
        // Inflate the custom bottom sheet layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_import_waypoint, null);
        EditText editImportCode = dialogView.findViewById(R.id.editImportCode);
        Button btnScanQR = dialogView.findViewById(R.id.btnScanQR);
        Button btnImport = dialogView.findViewById(R.id.btnImport);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        // Preview elements
        View previewCard = dialogView.findViewById(R.id.previewCard);
        ImageView previewWaypointIcon = dialogView.findViewById(R.id.previewWaypointIcon);
        ImageView previewImportBadge = dialogView.findViewById(R.id.previewImportBadge);
        TextView previewWaypointName = dialogView.findViewById(R.id.previewWaypointName);
        TextView previewWaypointDescription = dialogView.findViewById(R.id.previewWaypointDescription);
        TextView previewWaypointDate = dialogView.findViewById(R.id.previewWaypointDate);

        // Store the current waypoint being previewed
        final Waypoint[] previewWaypoint = { null };

        // Real-time validation and preview
        editImportCode.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
                String code = s.toString().trim();
                if (code.length() > 10) { // Only validate if there's enough text
                    try {
                        Waypoint wp = Waypoint.decode(WaypointActivity.this, code);
                        if (wp != null && wp.getName() != null) {
                            // Show preview
                            previewWaypoint[0] = wp;
                            previewWaypointName.setText(wp.getName());
                            previewWaypointDescription.setText(wp.getDescription());
                            previewWaypointDate.setText(wp.getDate());

                            // Set icon and color
                            int iconResId = getResources().getIdentifier(wp.getIconName(), "drawable",
                                    getPackageName());
                            if (iconResId != 0) {
                                previewWaypointIcon.setImageResource(iconResId);
                                previewWaypointIcon.setColorFilter(wp.getIconColor());
                            }

                            // Show preview elements
                            previewCard.setVisibility(View.VISIBLE);
                            btnImport.setVisibility(View.VISIBLE);

                            // Animate the preview card appearance
                            previewCard.setAlpha(0f);
                            previewCard.setTranslationY(20f);
                            previewCard.animate()
                                    .alpha(1f)
                                    .translationY(0f)
                                    .setDuration(300)
                                    .start();
                        } else {
                            // Hide preview
                            previewWaypoint[0] = null;
                            previewCard.setVisibility(View.GONE);
                            btnImport.setVisibility(View.GONE);
                        }
                    } catch (Exception e) {
                        // Hide preview
                        previewWaypoint[0] = null;
                        previewCard.setVisibility(View.GONE);
                        btnImport.setVisibility(View.GONE);
                    }
                } else {
                    // Hide preview
                    previewWaypoint[0] = null;
                    previewCard.setVisibility(View.GONE);
                    btnImport.setVisibility(View.GONE);
                }
            }
        });

        // Auto-paste logic: when the EditText is focused, check clipboard for a valid
        // app link
        editImportCode.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard != null && clipboard.hasPrimaryClip()) {
                    ClipData clip = clipboard.getPrimaryClip();
                    if (clip != null && clip.getItemCount() > 0) {
                        CharSequence text = clip.getItemAt(0).getText();
                        if (text != null) {
                            String code = text.toString().trim();
                            // Check if the clipboard text is a valid waypoint link/code
                            try {
                                Waypoint wp = Waypoint.decode(this, code);
                                if (wp != null && wp.getName() != null) {
                                    editImportCode.setText(code);
                                    editImportCode.setSelection(code.length());
                                }
                            } catch (Exception ignored) {
                            }
                        }
                    }
                }
            }
        });

        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.Dialog_Rounded);
        dialog.setContentView(dialogView);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
            behavior.setDraggable(true);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }

        // Scan QR button launches our custom bottom sheet scanner
        btnScanQR.setOnClickListener(v -> {
            QrScannerBottomSheet qrScannerBottomSheet = new QrScannerBottomSheet(
                    new QrScannerBottomSheet.QrScanListener() {
                        @Override
                        public void onQrScanned(String code) {
                            editImportCode.setText(code);
                            editImportCode.setSelection(code.length());
                            // Re-show the import dialog after scanning
                            dialog.show();
                        }
                    });
            qrScannerBottomSheet.show(getSupportFragmentManager(), "qr_scanner");
            dialog.dismiss();
        });

        // Import button imports the previewed waypoint
        btnImport.setOnClickListener(v -> {
            if (previewWaypoint[0] != null) {
                // Mark the waypoint as imported
                previewWaypoint[0].setImported(true);
                waypointList.add(previewWaypoint[0]);
                adapter.updateList(waypointList);
                ToastUtils.show(this, "✅ Waypoint imported successfully!", Toast.LENGTH_SHORT);
                saveFolderToPrefs(folder);
                dialog.dismiss();
            } else {
                ToastUtils.show(this, "Please enter a valid waypoint code first", Toast.LENGTH_SHORT);
            }
        });

        if (btnCancel != null) {
            btnCancel.setBackgroundTintList(null);
        }

        dialog.show();
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
            Type type = new TypeToken<List<Folder>>() {}.getType();
            folderList = new Gson().fromJson(json, type);
        }

        boolean found = false;
        for (int i = 0; i < folderList.size(); i++) {
            if (folder.getId() != null && folder.getId().equals(folderList.get(i).getId())) {
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