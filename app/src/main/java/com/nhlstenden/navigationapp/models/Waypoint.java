package com.nhlstenden.navigationapp.models;

import java.io.Serializable;

public class Waypoint implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String name;
    private String description;
    private String imageUri;
    private double lat;
    private double lng;
    private String date;

    public Waypoint(String id, String name, String description, String imageUri, double lat, double lng) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageUri = imageUri;
        this.lat = lat;
        this.lng = lng;
        this.date = "2025-04-24"; // Default date
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUri() {
        return imageUri;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public String getDate() {
        return date;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
