package com.nhlstenden.navigationapp.helpers;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.ClipData;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.nhlstenden.navigationapp.R;
import com.nhlstenden.navigationapp.models.Folder;
import com.nhlstenden.navigationapp.models.Waypoint;
import com.nhlstenden.navigationapp.helpers.ToastUtils;

import java.util.List;

public class ImportWaypointDialog {
    public interface OnImportListener {
        void onImport(Waypoint waypoint);
    }

    public static void show(Context context, OnImportListener listener) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_import_waypoint, null);
        EditText editImportCode = dialogView.findViewById(R.id.editImportCode);
        Button btnScanQR = dialogView.findViewById(R.id.btnScanQR);
        Button btnImport = dialogView.findViewById(R.id.btnImport);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        View previewCard = dialogView.findViewById(R.id.previewCard);
        ImageView previewWaypointIcon = dialogView.findViewById(R.id.previewWaypointIcon);
        ImageView previewImportBadge = dialogView.findViewById(R.id.previewImportBadge);
        TextView previewWaypointName = dialogView.findViewById(R.id.previewWaypointName);
        TextView previewWaypointDescription = dialogView.findViewById(R.id.previewWaypointDescription);
        TextView previewWaypointDate = dialogView.findViewById(R.id.previewWaypointDate);
        final Waypoint[] previewWaypoint = { null };
        editImportCode.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(android.text.Editable s) {
                String code = s.toString().trim();
                if (code.length() > 10) {
                    try {
                        Waypoint wp = Waypoint.decode(context, code);
                        if (wp != null && wp.getName() != null) {
                            previewWaypoint[0] = wp;
                            previewWaypointName.setText(wp.getName());
                            previewWaypointDescription.setText(wp.getDescription());
                            previewWaypointDate.setText(wp.getDate());
                            int iconResId = context.getResources().getIdentifier(wp.getIconName(), "drawable", context.getPackageName());
                            if (iconResId != 0) {
                                previewWaypointIcon.setImageResource(iconResId);
                                previewWaypointIcon.setColorFilter(wp.getIconColor());
                            }
                            previewCard.setVisibility(View.VISIBLE);
                            btnImport.setVisibility(View.VISIBLE);
                            previewCard.setAlpha(0f);
                            previewCard.setTranslationY(20f);
                            previewCard.animate().alpha(1f).translationY(0f).setDuration(300).start();
                        } else {
                            previewWaypoint[0] = null;
                            previewCard.setVisibility(View.GONE);
                            btnImport.setVisibility(View.GONE);
                        }
                    } catch (Exception e) {
                        previewWaypoint[0] = null;
                        previewCard.setVisibility(View.GONE);
                        btnImport.setVisibility(View.GONE);
                    }
                } else {
                    previewWaypoint[0] = null;
                    previewCard.setVisibility(View.GONE);
                    btnImport.setVisibility(View.GONE);
                }
            }
        });
        editImportCode.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard != null && clipboard.hasPrimaryClip()) {
                    ClipData clip = clipboard.getPrimaryClip();
                    if (clip != null && clip.getItemCount() > 0) {
                        CharSequence text = clip.getItemAt(0).getText();
                        if (text != null) {
                            String code = text.toString().trim();
                            try {
                                Waypoint wp = Waypoint.decode(context, code);
                                if (wp != null && wp.getName() != null) {
                                    editImportCode.setText(code);
                                    editImportCode.setSelection(code.length());
                                }
                            } catch (Exception ignored) {}
                        }
                    }
                }
            }
        });
        BottomSheetDialog dialog = new BottomSheetDialog(context, R.style.Dialog_Rounded);
        dialog.setContentView(dialogView);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
            behavior.setDraggable(true);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
        btnScanQR.setOnClickListener(v -> {
            ToastUtils.show(context, "QR scan not implemented in helper", Toast.LENGTH_SHORT);
            dialog.dismiss();
        });
        btnImport.setOnClickListener(v -> {
            if (previewWaypoint[0] != null) {
                previewWaypoint[0].setImported(true);
                if (listener != null) listener.onImport(previewWaypoint[0]);
                dialog.dismiss();
            } else {
                ToastUtils.show(context, "Please enter a valid waypoint code first", Toast.LENGTH_SHORT);
            }
        });
        if (btnCancel != null) {
            btnCancel.setBackgroundTintList(null);
        }
        dialog.show();
    }
} 