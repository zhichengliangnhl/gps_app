package com.nhlstenden.navigationapp.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class Folder implements Parcelable {
    private String name;
    private List<Waypoint> waypoints;

    public Folder(String name) {
        this.name = name;
        this.waypoints = new ArrayList<>();
    }

    protected Folder(Parcel in) {
        name = in.readString();
        waypoints = in.createTypedArrayList(Waypoint.CREATOR);
    }

    public static final Creator<Folder> CREATOR = new Creator<Folder>() {
        @Override
        public Folder createFromParcel(Parcel in) {
            return new Folder(in);
        }

        @Override
        public Folder[] newArray(int size) {
            return new Folder[size];
        }
    };

    public String getName() {
        return name;
    }

    public List<Waypoint> getWaypoints() {
        return waypoints;
    }

    public void addWaypoint(Waypoint waypoint) {
        waypoints.add(waypoint);
    }

    public void removeWaypoint(Waypoint waypoint) {
        waypoints.remove(waypoint);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeTypedList(waypoints);
    }
    public void deleteWaypoint(Waypoint waypoint) {
        this.waypoints.remove(waypoint);
    }
}