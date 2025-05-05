package com.nhlstenden.navigationapp.adapters;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nhlstenden.navigationapp.R;

public class WaypointViewHolder extends RecyclerView.ViewHolder {
    public ImageView imageView;
    public TextView nameTextView, descriptionTextView;
    public ImageButton editButton, deleteButton;
    public Button navigateButton;

    public WaypointViewHolder(@NonNull View itemView) {
        super(itemView);
        imageView = itemView.findViewById(R.id.waypointImage);
        nameTextView = itemView.findViewById(R.id.waypointName);
        descriptionTextView = itemView.findViewById(R.id.waypointDescription);
        editButton = itemView.findViewById(R.id.btnEdit);
        deleteButton = itemView.findViewById(R.id.btnDelete);
        navigateButton = itemView.findViewById(R.id.btnNavigate);
    }
}
