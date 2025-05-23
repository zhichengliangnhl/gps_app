package com.nhlstenden.navigationapp.models;

import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Base64;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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

    // Add this in Waypoint.java


    public String encode() {
        try {
            JSONObject json = new JSONObject();
            json.put("id", id);
            json.put("name", name);
            json.put("description", description);
            json.put("imageUri", imageUri != null ? imageUri : "");
            json.put("lat", lat);
            json.put("lng", lng);
            json.put("date", date);

            // Encode actual image if it's a file URI
            if (imageUri != null && imageUri.startsWith("file://")) {
                String path = Uri.parse(imageUri).getPath();
                String base64 = encodeImageFromPath(path);
                if (base64 != null) {
                    json.put("imageBase64", base64);
                }
            }

            return Base64.encodeToString(json.toString().getBytes(), Base64.NO_WRAP);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Waypoint decode(Context context, String encoded) {
        try {
            String jsonStr = new String(Base64.decode(encoded, Base64.NO_WRAP));
            JSONObject json = new JSONObject(jsonStr);

            String id = json.getString("id");
            String name = json.getString("name");
            String description = json.getString("description");
            String imageUri = json.optString("imageUri", "");
            double lat = json.getDouble("lat");
            double lng = json.getDouble("lng");
            String date = json.optString("date", null);

            // Check if there's an embedded image
            if (json.has("imageBase64")) {
                String imageBase64 = json.getString("imageBase64");
                String savedPath = decodeBase64ToImageFile(context, imageBase64);
                if (savedPath != null) {
                    imageUri = "file://" + savedPath;
                }
            }

            Waypoint wp = new Waypoint(id, name, description, imageUri, lat, lng);
            if (date != null) wp.setDate(date);
            return wp;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String encodeImageFromPath(String imagePath) {
        try {
            File file = new File(imagePath);
            if (!file.exists()) return null;

            FileInputStream fis = new FileInputStream(file);
            byte[] bytes = new byte[(int) file.length()];
            fis.read(bytes);
            fis.close();

            return Base64.encodeToString(bytes, Base64.NO_WRAP);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String decodeBase64ToImageFile(Context context, String base64Data) {
        try {
            byte[] imageBytes = Base64.decode(base64Data, Base64.NO_WRAP);
            File file = new File(context.getFilesDir(), "shared_img_" + System.currentTimeMillis() + ".jpg");

            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(imageBytes);
                fos.flush();
            }

            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

