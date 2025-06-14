package com.nhlstenden.navigationapp.adapters;

import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nhlstenden.navigationapp.R;
import com.nhlstenden.navigationapp.interfaces.OnWaypointClickListener;
import com.nhlstenden.navigationapp.models.Waypoint;

import java.util.List;
import java.util.Locale;

public class WaypointAdapter extends RecyclerView.Adapter<WaypointViewHolder> {

    private List<Waypoint> waypointList;
    private final OnWaypointClickListener listener;

    public WaypointAdapter(List<Waypoint> waypointList, OnWaypointClickListener listener) {
        this.waypointList = waypointList;
        this.listener = listener;
    }

    public void updateList(List<Waypoint> newList) {
        this.waypointList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WaypointViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_waypoint, parent, false);
        return new WaypointViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WaypointViewHolder holder, int position) {
        Waypoint waypoint = waypointList.get(position);
        holder.nameTextView.setText(waypoint.getName());
        holder.descriptionTextView.setText(waypoint.getDescription());
        holder.dateTextView.setText(waypoint.getDate());

        // Show navigation timer
        long navTime = waypoint.getNavigationTimeMillis();
        if (navTime > 0) {
            holder.timerTextView.setText("Timer: " + formatTimer(navTime));
        } else {
            holder.timerTextView.setText("Timer: --");
        }

        // Set icon and color (use default if not set)
        int iconResId = holder.itemView.getContext().getResources().getIdentifier(waypoint.getIconName(), "drawable", holder.itemView.getContext().getPackageName());
        int iconColor = waypoint.getIconColor();
        holder.imageView.setImageResource(iconResId);
        holder.imageView.setColorFilter(iconColor);

        // Set border based on completion
        boolean completed = false;
        android.content.SharedPreferences prefs = holder.itemView.getContext().getSharedPreferences("AppPrefs",
                android.content.Context.MODE_PRIVATE);
        if (waypoint.getId() != null) {
            completed = prefs.getBoolean("waypoint_completed_" + waypoint.getId(), false);
        }
        FrameLayout imageFrame = holder.itemView.findViewById(R.id.waypointImageFrame);
        ImageView crownView = holder.itemView.findViewById(R.id.waypointCrown);
        ImageView starView = holder.itemView.findViewById(R.id.waypointStar);
        if (completed) {
            imageFrame.setBackgroundResource(0);
            crownView.setVisibility(View.VISIBLE);
            starView.setVisibility(View.GONE);
        } else {
            imageFrame.setBackgroundResource(0);
            crownView.setVisibility(View.GONE);
            starView.setVisibility(View.VISIBLE);
        }

        // Hook entire card view to navigation
        holder.itemView.setOnClickListener(v -> listener.onNavigateClick(waypoint));

        // Edit & delete
        holder.editButton.setOnClickListener(v -> listener.onEditClick(waypoint));
        holder.deleteButton.setOnClickListener(v -> listener.onDeleteClick(waypoint));
        holder.shareButton.setOnClickListener(v -> listener.onShareClick(waypoint));
    }

    @Override
    public int getItemCount() {
        return waypointList.size();
    }

    public void addWaypoint(Waypoint waypoint) {
        waypointList.add(waypoint);
        notifyItemInserted(waypointList.size() - 1);
    }

    public void updateWaypoint(Waypoint updated) {
        for (int i = 0; i < waypointList.size(); i++) {
            if (waypointList.get(i).getId().equals(updated.getId())) {
                waypointList.set(i, updated);
                notifyDataSetChanged();
                break;
            }
        }
    }

    public void removeWaypoint(Waypoint waypoint) {
        int position = -1;
        // Find the position of the waypoint to remove
        for (int i = 0; i < waypointList.size(); i++) {
            if (waypointList.get(i).getId().equals(waypoint.getId())) {
                position = i;
                break;
            }
        }

        // Only remove if we found the waypoint
        if (position != -1) {
            waypointList.remove(position);
            notifyItemRemoved(position);
            // Notify any items after the removed position that they need to update their
            // positions
            notifyItemRangeChanged(position, waypointList.size() - position);
        }
    }

    private String formatTimer(long millis) {
        long seconds = millis / 1000;
        long minutes = (seconds % 3600) / 60;
        long hours = seconds / 3600;
        long secs = seconds % 60;
        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, secs);
        } else {
            return String.format("%02d:%02d", minutes, secs);
        }
    }

}