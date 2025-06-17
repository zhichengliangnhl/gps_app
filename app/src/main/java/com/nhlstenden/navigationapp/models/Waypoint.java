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

public class Waypoint implements Parcelable
{
    private String id;
    private String name;
    private String description;
    private String iconName;
    private int iconColor;
    private double lat;
    private double lng;
    private String date;
    private long navigationTimeMillis = 0L;
    private boolean isImported = false;

    public Waypoint(String id, String name, String description, String iconName, int iconColor, double lat, double lng)
    {
        this.id = id;
        this.name = name;
        this.description = description;
        this.iconName = iconName;
        this.iconColor = iconColor;
        this.lat = lat;
        this.lng = lng;
        // Set current date in yyyy-MM-dd format
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        this.date = sdf.format(new Date());
    }

    protected Waypoint(Parcel in)
    {
        id = in.readString();
        name = in.readString();
        description = in.readString();
        iconName = in.readString();
        iconColor = in.readInt();
        lat = in.readDouble();
        lng = in.readDouble();
        date = in.readString();
        navigationTimeMillis = in.readLong();
        isImported = in.readByte() != 0;
    }

    public static final Creator<Waypoint> CREATOR = new Creator<Waypoint>()
    {
        @Override
        public Waypoint createFromParcel(Parcel in)
        {
            return new Waypoint(in);
        }

        @Override
        public Waypoint[] newArray(int size)
        {
            return new Waypoint[size];
        }
    };

    public String getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public String getIconName()
    {
        return iconName;
    }

    public int getIconColor()
    {
        return iconColor;
    }

    public double getLat()
    {
        return lat;
    }

    public double getLng()
    {
        return lng;
    }

    public String getDate()
    {
        return date;
    }

    public long getNavigationTimeMillis()
    {
        return navigationTimeMillis;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public void setIconName(String iconName)
    {
        this.iconName = iconName;
    }

    public void setIconColor(int iconColor)
    {
        this.iconColor = iconColor;
    }

    public void setLat(double lat)
    {
        this.lat = lat;
    }

    public void setLng(double lng)
    {
        this.lng = lng;
    }

    public void setDate(String date)
    {
        this.date = date;
    }

    public void setNavigationTimeMillis(long navigationTimeMillis)
    {
        this.navigationTimeMillis = navigationTimeMillis;
    }

    public boolean isImported()
    {
        return isImported;
    }

    public void setImported(boolean imported)
    {
        isImported = imported;
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
        dest.writeString(description);
        dest.writeString(iconName);
        dest.writeInt(iconColor);
        dest.writeDouble(lat);
        dest.writeDouble(lng);
        dest.writeString(date);
        dest.writeLong(navigationTimeMillis);
        dest.writeByte((byte) (isImported ? 1 : 0));
    }

    public String encode()
    {
        try
        {
            JSONObject json = new JSONObject();
            json.put("id", id);
            json.put("name", name);
            json.put("description", description);
            json.put("iconName", iconName);
            json.put("iconColor", iconColor);
            json.put("lat", lat);
            json.put("lng", lng);
            json.put("date", date);
            json.put("navigationTimeMillis", navigationTimeMillis);

            return Base64.encodeToString(json.toString().getBytes(), Base64.NO_WRAP);
        } catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public static Waypoint decode(Context context, String encoded)
    {
        try
        {
            String jsonStr = new String(Base64.decode(encoded, Base64.NO_WRAP));
            JSONObject json = new JSONObject(jsonStr);

            String id = json.getString("id");
            String name = json.getString("name");
            String description = json.getString("description");
            String iconName = json.getString("iconName");
            int iconColor = json.optInt("iconColor", Color.BLACK);
            double lat = json.getDouble("lat");
            double lng = json.getDouble("lng");
            String date = json.optString("date", null);
            long navigationTimeMillis = json.optLong("navigationTimeMillis", 0L);

            Waypoint wp = new Waypoint(id, name, description, iconName, iconColor, lat, lng);
            if (date != null) wp.setDate(date);
            wp.setImported(true);
            return wp;
        } catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
}
