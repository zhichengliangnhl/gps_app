package com.nhlstenden.navigationapp.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Folder implements Parcelable
{
    private String id;
    private String name;
    private List<Waypoint> waypoints;

    public Folder(String name)
    {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.waypoints = new ArrayList<>();
    }

    public Folder(String id, String name)
    {
        this.id = id;
        this.name = name;
        this.waypoints = new ArrayList<>();
    }

    protected Folder(Parcel in)
    {
        id = in.readString();
        name = in.readString();
        waypoints = in.createTypedArrayList(Waypoint.CREATOR);
    }

    public static final Creator<Folder> CREATOR = new Creator<Folder>()
    {
        @Override
        public Folder createFromParcel(Parcel in)
        {
            return new Folder(in);
        }

        @Override
        public Folder[] newArray(int size)
        {
            return new Folder[size];
        }
    };

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public List<Waypoint> getWaypoints()
    {
        return waypoints;
    }

    public void addWaypoint(Waypoint waypoint)
    {
        waypoints.add(waypoint);
    }

    public void removeWaypoint(Waypoint waypoint)
    {
        waypoints.remove(waypoint);
    }

    public void deleteWaypoint(Waypoint waypoint)
    {
        this.waypoints.remove(waypoint);
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeTypedList(waypoints);
    }
}
