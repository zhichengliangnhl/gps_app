package com.nhlstenden.navigationapp.adapters;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nhlstenden.navigationapp.R;
import com.nhlstenden.navigationapp.models.Waypoint;

import java.util.List;

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

        if (waypoint.getImageUri() != null && !waypoint.getImageUri().isEmpty()) {
            holder.imageView.setImageURI(Uri.parse(waypoint.getImageUri()));
        } else {
            holder.imageView.setImageResource(R.drawable.ic_launcher_background);
        }

        holder.editButton.setOnClickListener(v -> listener.onEditClick(waypoint));
        holder.deleteButton.setOnClickListener(v -> listener.onDeleteClick(waypoint));
        holder.navigateButton.setOnClickListener(v -> listener.onNavigateClick(waypoint));
    }

    @Override
    public int getItemCount() {
        return waypointList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView nameTextView, descriptionTextView, dateTextView;
        ImageButton editButton, deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.waypointImage);
            nameTextView = itemView.findViewById(R.id.waypointName);
            descriptionTextView = itemView.findViewById(R.id.waypointDescription);
            dateTextView = itemView.findViewById(R.id.waypointDate);
            editButton = itemView.findViewById(R.id.btnEdit);
            deleteButton = itemView.findViewById(R.id.btnDelete);
        }
    }
}
