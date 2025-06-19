package com.nhlstenden.navigationapp.adapters;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nhlstenden.navigationapp.R;

public class WaypointViewHolder extends RecyclerView.ViewHolder
{
    ImageView imageView;
    TextView nameTextView, descriptionTextView, dateTextView, timerTextView;
    ImageButton editButton, deleteButton, shareButton;

    public WaypointViewHolder(@NonNull View itemView)
    {
        super(itemView);
        this.imageView = itemView.findViewById(R.id.waypointImage);
        this.nameTextView = itemView.findViewById(R.id.waypointName);
        this.descriptionTextView = itemView.findViewById(R.id.waypointDescription);
        this.dateTextView = itemView.findViewById(R.id.waypointDate);
        this.timerTextView = itemView.findViewById(R.id.waypointTimer);
        this.editButton = itemView.findViewById(R.id.btnEdit);
        this.deleteButton = itemView.findViewById(R.id.btnDelete);
        this.shareButton = itemView.findViewById(R.id.btnShare);
    }
}