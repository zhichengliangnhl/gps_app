package com.nhlstenden.navigationapp.adapters;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nhlstenden.navigationapp.R;

public class FolderViewHolder extends RecyclerView.ViewHolder {
    public TextView folderNameTextView;
    public ImageButton shareButton;

    public FolderViewHolder(@NonNull View itemView) {
        super(itemView);
        folderNameTextView = itemView.findViewById(R.id.folderNameTextView);
    }
}
