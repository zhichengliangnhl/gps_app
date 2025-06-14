package com.nhlstenden.navigationapp.adapters;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nhlstenden.navigationapp.R;

public class WaypointViewHolder extends RecyclerView.ViewHolder {
    ImageView imageView;
    TextView nameTextView, descriptionTextView, dateTextView, timerTextView;
    ImageButton editButton, deleteButton, shareButton;

    public WaypointViewHolder(@NonNull View itemView) {
        super(itemView);
        imageView = itemView.findViewById(R.id.waypointImage);
        nameTextView = itemView.findViewById(R.id.waypointName);
        descriptionTextView = itemView.findViewById(R.id.waypointDescription);
        dateTextView = itemView.findViewById(R.id.waypointDate);
        timerTextView = itemView.findViewById(R.id.waypointTimer);
        editButton = itemView.findViewById(R.id.btnEdit);
        deleteButton = itemView.findViewById(R.id.btnDelete);
        shareButton = itemView.findViewById(R.id.btnShare);
    }
}