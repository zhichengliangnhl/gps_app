package com.nhlstenden.navigationapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nhlstenden.navigationapp.R;
import com.nhlstenden.navigationapp.interfaces.OnFolderClickListener;
import com.nhlstenden.navigationapp.models.Folder;

import java.util.List;

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.FolderViewHolder> {

    private final List<Folder> folderList;
    private final OnFolderClickListener listener;

    public FolderAdapter(List<Folder> folderList, OnFolderClickListener listener) {
        this.folderList = folderList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FolderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_folder, parent, false);
        return new FolderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FolderViewHolder holder, int position) {
        Folder folder = folderList.get(position);
        holder.folderNameTextView.setText(folder.getName());

        holder.itemView.setOnClickListener(v -> listener.onFolderClicked(folder));
        holder.editButton.setOnClickListener(v -> listener.onEditFolder(folder));
        holder.deleteButton.setOnClickListener(v -> listener.onDeleteFolder(folder));
    }

    @Override
    public int getItemCount() {
        return folderList.size();
    }

    public void updateFolder(Folder updatedFolder) {
        for (int i = 0; i < folderList.size(); i++) {
            if (folderList.get(i).getName().equals(updatedFolder.getName())) {
                folderList.set(i, updatedFolder);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public static class FolderViewHolder extends RecyclerView.ViewHolder {
        TextView folderNameTextView;
        ImageButton editButton, deleteButton, shareButton;

        public FolderViewHolder(@NonNull View itemView) {
            super(itemView);
            folderNameTextView = itemView.findViewById(R.id.folderNameTextView);
            editButton = itemView.findViewById(R.id.editFolderButton);
            deleteButton = itemView.findViewById(R.id.deleteFolderButton);
        }
    }
}
