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

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.FolderViewHolder>
{

    private final List<Folder> folderList;
    private final OnFolderClickListener listener;

    public FolderAdapter(List<Folder> folderList, OnFolderClickListener listener)
    {
        this.folderList = folderList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FolderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_folder, parent, false);
        return new FolderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FolderViewHolder holder, int position)
    {
        Folder folder = this.folderList.get(position);
        holder.folderNameTextView.setText(folder.getName());

        holder.itemView.setOnClickListener(v -> this.listener.onFolderClicked(folder));
        holder.editButton.setOnClickListener(v -> this.listener.onEditFolder(folder));
        holder.deleteButton.setOnClickListener(v -> this.listener.onDeleteFolder(folder));
    }

    @Override
    public int getItemCount()
    {
        return this.folderList.size();
    }

    public void updateFolder(Folder updatedFolder)
    {
        for (int i = 0; i < this.folderList.size(); i++)
        {
            if (this.folderList.get(i).getName().equals(updatedFolder.getName()))
            {
                this.folderList.set(i, updatedFolder);
                this.notifyItemChanged(i);
                break;
            }
        }
    }

    public static class FolderViewHolder extends RecyclerView.ViewHolder
    {
        TextView folderNameTextView;
        ImageButton editButton, deleteButton, shareButton;

        public FolderViewHolder(@NonNull View itemView)
        {
            super(itemView);
            this.folderNameTextView = itemView.findViewById(R.id.folderNameTextView);
            this.editButton = itemView.findViewById(R.id.editFolderButton);
            this.deleteButton = itemView.findViewById(R.id.deleteFolderButton);
        }
    }
}
