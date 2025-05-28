package com.nhlstenden.navigationapp.interfaces;

import com.nhlstenden.navigationapp.models.Folder;

public interface OnFolderClickListener {
    void onFolderClicked(Folder folder);
    void onEditFolder(Folder folder);
    void onDeleteFolder(Folder folder);
    void onShareFolder(Folder folder);
}
