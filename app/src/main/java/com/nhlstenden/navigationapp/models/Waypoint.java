package com.nhlstenden.navigationapp.models;

import static com.nhlstenden.navigationapp.activities.WaypointActivity.decodeBase64ToImageFile;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Base64;
import android.util.Log;

import com.nhlstenden.navigationapp.R;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
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
    private int iconResId;
    private int iconColor;
    private double lat;
    private double lng;
    private String date;

    public Waypoint(String id, String name, String description, int iconResId, int iconColor, double lat, double lng) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.iconResId = iconResId;
        this.iconColor = iconColor;
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
        iconResId = in.readInt();
        iconColor = in.readInt();
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

    public int getIconResId() {
        return iconResId;
    }

    public int getIconColor() {
        return iconColor;
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

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }

    public void setIconColor(int iconColor) {
        this.iconColor = iconColor;
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
        dest.writeInt(iconResId);
        dest.writeInt(iconColor);
        dest.writeDouble(lat);
        dest.writeDouble(lng);
        dest.writeString(date);
    }

    public String encode() {
        try {
            JSONObject json = new JSONObject();
            json.put("id", id);
            json.put("name", name);
            json.put("description", description);
            json.put("iconResId", iconResId);
            json.put("iconColor", iconColor);
            json.put("lat", lat);
            json.put("lng", lng);
            json.put("date", date);

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
            int iconResId = json.optInt("iconResId", R.drawable.icon1);
            int iconColor = json.optInt("iconColor", Color.BLACK);
            double lat = json.getDouble("lat");
            double lng = json.getDouble("lng");
            String date = json.optString("date", null);

            Waypoint wp = new Waypoint(id, name, description, iconResId, iconColor, lat, lng);
            if (date != null) wp.setDate(date);
            return wp;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String encodeImageFromPath(String imagePath) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(imagePath, options);

            // Determine scaling factor
            int maxDim = 300;
            int scale = 1;
            while (options.outWidth / scale > maxDim || options.outHeight / scale > maxDim) {
                scale *= 2;
            }

            options.inSampleSize = scale;
            options.inJustDecodeBounds = false;
            Bitmap resizedBitmap = BitmapFactory.decodeFile(imagePath, options);

            if (resizedBitmap == null) return null;

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream);
            resizedBitmap.recycle();

            byte[] imageBytes = outputStream.toByteArray();
            return Base64.encodeToString(imageBytes, Base64.NO_WRAP);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

