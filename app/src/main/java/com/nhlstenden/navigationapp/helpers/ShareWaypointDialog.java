package com.nhlstenden.navigationapp.helpers;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.nhlstenden.navigationapp.R;
import com.nhlstenden.navigationapp.models.Waypoint;
import com.nhlstenden.navigationapp.helpers.ToastUtils;
import com.nhlstenden.navigationapp.utils.QRCodeUtils;

public class ShareWaypointDialog {
    public static void show(Context context, Waypoint waypoint) {
        String encoded = waypoint.encode();
        if (encoded == null) {
            ToastUtils.show(context, "Failed to encode waypoint", Toast.LENGTH_SHORT);
            return;
        }
        Bitmap qrBitmap = QRCodeUtils.generateQRCode(encoded);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_share_waypoint, null);
        ImageView qrCodeImage = dialogView.findViewById(R.id.qrCodeImage);
        EditText importLink = dialogView.findViewById(R.id.importLink);
        Button btnCopy = dialogView.findViewById(R.id.btnCopy);
        Button btnShare = dialogView.findViewById(R.id.btnShare);
        if (qrBitmap != null && qrCodeImage != null) {
            qrCodeImage.setImageBitmap(qrBitmap);
        }
        importLink.setText(encoded);
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
        btnCopy.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
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
            ToastUtils.show(context, "Link copied to clipboard", Toast.LENGTH_SHORT);
        });
        btnShare.setOnClickListener(v -> {
            v.postDelayed(() -> {
                try {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_TEXT, encoded);
                    context.startActivity(Intent.createChooser(shareIntent, "Share Waypoint"));
                    dialog.dismiss();
                } catch (Exception e) {
                    Log.e("ShareWaypoint", "Failed to share waypoint: " + e.getMessage());
                    ToastUtils.show(context, "Failed to share waypoint", Toast.LENGTH_SHORT);
                }
            }, 100);
        });
        dialog.show();
    }
} 