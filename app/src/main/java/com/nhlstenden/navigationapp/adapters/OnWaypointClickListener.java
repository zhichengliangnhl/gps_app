package com.nhlstenden.navigationapp.adapters;

import com.nhlstenden.navigationapp.models.Waypoint;

public interface OnWaypointClickListener
{
    void onEditClick(Waypoint waypoint);
    void onDeleteClick(Waypoint waypoint);
    void onNavigateClick(Waypoint waypoint);
}
