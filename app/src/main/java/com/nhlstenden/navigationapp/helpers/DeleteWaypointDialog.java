package com.nhlstenden.navigationapp.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
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

public class DeleteWaypointDialog {
    public interface OnDeleteListener {
        void onDelete(Waypoint waypoint);
    }

    public static void show(Context context, Waypoint waypoint, Folder folder, List<Waypoint> waypointList, OnDeleteListener listener) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_delete_waypoint, null);
        ImageView previewWaypointIcon = dialogView.findViewById(R.id.previewWaypointIcon);
        ImageView previewWaypointCrown = dialogView.findViewById(R.id.previewWaypointCrown);
        ImageView previewWaypointImport = dialogView.findViewById(R.id.previewWaypointImport);
        TextView previewWaypointName = dialogView.findViewById(R.id.previewWaypointName);
        TextView previewWaypointDescription = dialogView.findViewById(R.id.previewWaypointDescription);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnDelete = dialogView.findViewById(R.id.btnDelete);

        previewWaypointName.setText(waypoint.getName());
        previewWaypointDescription.setText(waypoint.getDescription());
        int iconResId = context.getResources().getIdentifier(waypoint.getIconName(), "drawable", context.getPackageName());
        if (iconResId != 0) {
            previewWaypointIcon.setImageResource(iconResId);
            previewWaypointIcon.setColorFilter(waypoint.getIconColor());
        }
        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        boolean completed = false;
        if (waypoint.getId() != null) {
            completed = prefs.getBoolean("waypoint_completed_" + waypoint.getId(), false);
        }
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
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnDelete.setOnClickListener(v -> {
            folder.getWaypoints().remove(waypoint);
            waypointList.remove(waypoint);
            ToastUtils.show(context, "Waypoint deleted", Toast.LENGTH_SHORT);
            dialog.dismiss();
            if (listener != null) listener.onDelete(waypoint);
        });
        btnCancel.setBackgroundTintList(null);
        btnDelete.setBackgroundTintList(null);
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setDimAmount(0.6f);
        }
        if (bottomSheet != null) {
            bottomSheet.setBackgroundResource(android.R.color.transparent);
        }
    }
} 