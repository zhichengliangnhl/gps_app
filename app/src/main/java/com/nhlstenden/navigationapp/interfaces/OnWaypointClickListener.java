package com.nhlstenden.navigationapp.interfaces;

import com.nhlstenden.navigationapp.models.Waypoint;

public interface OnWaypointClickListener
{
    void onEditClick(Waypoint waypoint);
    void onDeleteClick(Waypoint waypoint);
    void onNavigateClick(Waypoint waypoint);
}
