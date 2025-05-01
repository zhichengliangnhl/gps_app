package com.nhlstenden.navigationapp.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Folder implements Serializable {
    private String name;
    private List<Waypoint> waypoints;

    public Folder(String name) {
        this.name = name;
        this.waypoints = new ArrayList<>();
    }

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
}
