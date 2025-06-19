package com.nhlstenden.navigationapp.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nhlstenden.navigationapp.models.Folder;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class WaypointPersistenceHelper {
    private static final String PREFS_NAME = "com.nhlstenden.navigationapp.PREFS";
    private static final String FOLDERS_JSON_KEY = "folders_json";

    public static void saveFolder(Context context, Folder folder) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(FOLDERS_JSON_KEY, null);
        List<Folder> folderList = new ArrayList<>();
        if (json != null) {
            Type type = new TypeToken<List<Folder>>() {}.getType();
            folderList = new Gson().fromJson(json, type);
        }
        boolean found = false;
        for (int i = 0; i < folderList.size(); i++) {
            if (folder.getId() != null && folder.getId().equals(folderList.get(i).getId())) {
                folderList.set(i, folder);
                found = true;
                break;
            }
        }
        if (!found) {
            folderList.add(folder);
        }
        prefs.edit().putString(FOLDERS_JSON_KEY, new Gson().toJson(folderList)).apply();
    }

    public static List<Folder> loadFolders(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(FOLDERS_JSON_KEY, null);
        if (json != null) {
            Type type = new TypeToken<List<Folder>>() {}.getType();
            return new Gson().fromJson(json, type);
        }
        return new ArrayList<>();
    }
} 