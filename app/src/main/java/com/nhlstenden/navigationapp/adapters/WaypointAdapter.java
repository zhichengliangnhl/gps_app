package com.nhlstenden.navigationapp.adapters;

import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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

        String coordinates = String.format(Locale.getDefault(),
                "Lat: %.6f, Lng: %.6f",
                waypoint.getLat(), waypoint.getLng());
        holder.coordinatesTextView.setText(coordinates);

        if (waypoint.getImageUri() != null && !waypoint.getImageUri().isEmpty()) {
            Uri uri = Uri.parse(waypoint.getImageUri());
            if ("file".equals(uri.getScheme())) {
                holder.imageView.setImageURI(uri);
            } else {
                Log.e("WAYPOINT_IMAGE", "Failed to load image from URI ");
                holder.imageView.setImageResource(R.drawable.ic_launcher_background); // fallback
            }
        } else {
            Log.e("WAYPOINT_IMAGE", "Failed, supposedly, image URI is empty");
            holder.imageView.setImageResource(R.drawable.ic_launcher_background);
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
                notifyItemChanged(i);
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
            // Notify any items after the removed position that they need to update their positions
            notifyItemRangeChanged(position, waypointList.size() - position);
        }
    }

}