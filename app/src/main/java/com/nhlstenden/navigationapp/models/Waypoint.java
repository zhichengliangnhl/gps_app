package models;

import java.io.Serializable;

public class Waypoint implements Serializable {
    private String name;
    private String description;
    private String photoUri;
    private double latitude;
    private double longitude;

    public Waypoint(String name, String description, String photoUri, double latitude, double longitude) {
        this.name = name;
        this.description = description;
        this.photoUri = photoUri;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getPhotoUri() { return photoUri; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
}
