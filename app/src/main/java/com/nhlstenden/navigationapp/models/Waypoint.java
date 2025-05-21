package com.nhlstenden.navigationapp.models;

import android.os.Parcel;
import android.os.Parcelable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Waypoint implements Parcelable {
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
        // Set current date in yyyy-MM-dd format
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        this.date = sdf.format(new Date());
    }

    protected Waypoint(Parcel in) {
        id = in.readString();
        name = in.readString();
        description = in.readString();
        imageUri = in.readString();
        lat = in.readDouble();
        lng = in.readDouble();
        date = in.readString();
    }

    public static final Creator<Waypoint> CREATOR = new Creator<Waypoint>() {
        @Override
        public Waypoint createFromParcel(Parcel in) {
            return new Waypoint(in);
        }

        @Override
        public Waypoint[] newArray(int size) {
            return new Waypoint[size];
        }
    };

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeString(imageUri);
        dest.writeDouble(lat);
        dest.writeDouble(lng);
        dest.writeString(date);
    }
}
