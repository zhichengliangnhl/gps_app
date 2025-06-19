package com.nhlstenden.navigationapp.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.graphics.TypefaceCompatApi28Impl;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nhlstenden.navigationapp.BaseActivity;
import com.nhlstenden.navigationapp.R;
import com.nhlstenden.navigationapp.dialogs.IconSelectionDialog;
import com.nhlstenden.navigationapp.helpers.AchievementManager;
import com.nhlstenden.navigationapp.helpers.ToastUtils;
import com.nhlstenden.navigationapp.models.Waypoint;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class CreateWaypointActivity extends BaseActivity
{
    private MapView mapPreview;
    private GoogleMap previewMap;

    private static final int MAX_DESCRIPTION_LENGTH = 500;
    private double lat = 0.0;
    private double lng = 0.0;

    private EditText etName, etDescription;
    private Button btnSaveWaypoint, btnCancel;
    private ImageView imagePreview;
    private View imageClickOverlay;
    private String mode, id;
    private String selectedIconName = "icon1";
    private int selectedIconColor = Color.BLACK;
    private String originalDate;
    private boolean isEditMode = false;
    private Waypoint existingWaypoint = null;

    private final ActivityResultLauncher<Intent> mapLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result ->
            {
                if (result.getResultCode() == RESULT_OK && result.getData() != null)
                {
                    Intent data = result.getData();
                    if (data.hasExtra("lat") && data.hasExtra("lng"))
                    {
                        this.lat = data.getDoubleExtra("lat", 0.0);
                        this.lng = data.getDoubleExtra("lng", 0.0);

                        ToastUtils.show(this,
                                String.format("Location selected: %.6f, %.6f", this.lat, this.lng),
                                Toast.LENGTH_SHORT);

                        this.updateMapPreview(this.lat, this.lng);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_waypoint);

        TextView headerTitle = findViewById(R.id.headerTitle);
        this.etName = findViewById(R.id.etName);
        this.etDescription = findViewById(R.id.etDescription);
        this.btnSaveWaypoint = findViewById(R.id.btnSaveWaypoint);
        this.btnCancel = findViewById(R.id.btnCancel);
        this.mapPreview = findViewById(R.id.mapPreview);
        this.imagePreview = findViewById(R.id.imagePreview);
        this.imageClickOverlay = findViewById(R.id.imageClickOverlay);

        this.imagePreview.setImageResource(getResources().getIdentifier(this.selectedIconName, "drawable", getPackageName()));
        this.imagePreview.setColorFilter(this.selectedIconColor);

        this.mode = getIntent().getStringExtra("mode");
        this.isEditMode = "edit".equals(this.mode);
        this.id = getIntent().getStringExtra("id");

        boolean isImported;

        if (headerTitle != null)
        {
            headerTitle.setText("edit".equals(this.mode) ? "Edit Treasure" : "Create Treasure");
        }
        setupSettingsPanel();

        if (savedInstanceState != null)
        {
            isImported = false;
            this.lat = savedInstanceState.getDouble("lat", 0.0);
            this.lng = savedInstanceState.getDouble("lng", 0.0);
            this.mode = savedInstanceState.getString("mode");
            this.id = savedInstanceState.getString("id");
            this.selectedIconName = savedInstanceState.getString("iconName");
            this.selectedIconColor = savedInstanceState.getInt("iconColor", Color.BLACK);
            this.imagePreview.setImageResource(getResources().getIdentifier(this.selectedIconName, "drawable", getPackageName()));
            this.imagePreview.setColorFilter(this.selectedIconColor);
        }
        else if ("edit".equals(this.mode))
        {
            this.existingWaypoint = getIntent().getParcelableExtra("WAYPOINT");
            if (this.existingWaypoint != null)
            {
                this.etName.setText(this.existingWaypoint.getName());
                this.etDescription.setText(this.existingWaypoint.getDescription());
                this.lat = this.existingWaypoint.getLat();
                this.lng = this.existingWaypoint.getLng();
                this.originalDate = this.existingWaypoint.getDate();
                isImported = this.existingWaypoint.isImported();
                this.selectedIconName = this.existingWaypoint.getIconName();
                this.selectedIconColor = this.existingWaypoint.getIconColor();
                this.imagePreview.setImageResource(getResources().getIdentifier(this.selectedIconName, "drawable", getPackageName()));
                this.imagePreview.setColorFilter(this.selectedIconColor);
            }
            else
            {
                isImported = false;
            }
        }
        else
        {
            isImported = false;
            if (getIntent().hasExtra("lat") && getIntent().hasExtra("lng"))
            {
                this.lat = getIntent().getDoubleExtra("lat", 0.0);
                this.lng = getIntent().getDoubleExtra("lng", 0.0);
            }
        }

        this.mapPreview.onCreate(savedInstanceState);
        this.mapPreview.getMapAsync(googleMap ->
        {
            this.previewMap = googleMap;
            this.previewMap.getUiSettings().setAllGesturesEnabled(false);
            this.updateMapPreview(this.lat, this.lng);
        });

        View ivCompass = findViewById(R.id.ivCompass);

        if (isImported)
        {
            this.mapPreview.setAlpha(0.6f);
            this.mapPreview.setVisibility(View.GONE);
            findViewById(R.id.mapClickOverlay).setVisibility(View.GONE);
            ivCompass.setVisibility(View.VISIBLE);
        }
        else
        {
            this.mapPreview.setVisibility(View.VISIBLE);
            findViewById(R.id.mapClickOverlay).setVisibility(View.VISIBLE);
            ivCompass.setVisibility(View.GONE);
            findViewById(R.id.mapClickOverlay).setOnClickListener(v ->
            {
                Intent mapIntent = new Intent(CreateWaypointActivity.this, MapActivity.class);
                if (this.lat != 0.0 && this.lng != 0.0)
                {
                    mapIntent.putExtra("lat", this.lat);
                    mapIntent.putExtra("lng", this.lng);
                }
                this.mapLauncher.launch(mapIntent);
            });
        }

        this.imageClickOverlay.setOnClickListener(v ->
        {
            IconSelectionDialog dialog = new IconSelectionDialog(this, this.selectedIconName, this.selectedIconColor, (iconName, color) ->
            {
                this.selectedIconName = iconName;
                this.selectedIconColor = color;
                this.imagePreview.setImageResource(getResources().getIdentifier(this.selectedIconName, "drawable", getPackageName()));
                this.imagePreview.setColorFilter(this.selectedIconColor);
            });
            dialog.show();
        });

        this.btnSaveWaypoint.setOnClickListener(v ->
        {
            if (!this.validateInput())
            {
                return;
            }

            String name = this.etName.getText().toString().trim();
            String description = this.etDescription.getText().toString().trim();
            if (description.length() > MAX_DESCRIPTION_LENGTH)
            {
                description = description.substring(0, MAX_DESCRIPTION_LENGTH);
            }

            String waypointId;
            String waypointDate;
            if (this.isEditMode && this.existingWaypoint != null)
            {
                waypointId = this.existingWaypoint.getId();
                waypointDate = this.existingWaypoint.getDate();
            }
            else
            {
                waypointId = UUID.randomUUID().toString();
                waypointDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                AchievementManager.updateFirstStepsProgress(this);
            }

            Waypoint resultWaypoint = new Waypoint(
                    waypointId,
                    name,
                    description,
                    this.selectedIconName,
                    this.selectedIconColor,
                    this.lat,
                    this.lng
            );
            resultWaypoint.setDate(waypointDate);
            resultWaypoint.setImported(isImported);

            Intent resultIntent = new Intent();
            Log.d("WAYPOINT_RESULT", "Waypoint: " + resultWaypoint);
            resultIntent.putExtra("WAYPOINT", resultWaypoint);
            resultIntent.putExtra("mode", this.mode);
            setResult(RESULT_OK, resultIntent);
            finish();
        });

        this.btnCancel.setOnClickListener(v ->
        {
            setResult(RESULT_CANCELED);
            finish();
        });

        View bottomNav = findViewById(R.id.bottom_nav_container);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) ->
        {
            Insets systemInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            if (bottomNav != null)
            {
                bottomNav.setPadding(
                        bottomNav.getPaddingLeft(),
                        bottomNav.getPaddingTop(),
                        bottomNav.getPaddingRight(),
                        systemInsets.bottom
                );
            }
            return insets;
        });
    }

    private boolean validateInput()
    {
        if (TextUtils.isEmpty(this.etName.getText().toString().trim()))
        {
            ToastUtils.show(this, "Please enter a name", Toast.LENGTH_SHORT);
            return false;
        }

        if (this.lat == 0.0 && this.lng == 0.0)
        {
            ToastUtils.show(this, "Please select a location on the map", Toast.LENGTH_SHORT);
            return false;
        }

        String description = this.etDescription.getText().toString().trim();
        if (description.length() > MAX_DESCRIPTION_LENGTH)
        {
            ToastUtils.show(this, "Description too long (max 500 characters)", Toast.LENGTH_SHORT);
            return false;
        }

        return true;
    }

    private void updateMapPreview(double lat, double lng)
    {
        if (this.previewMap == null) return;

        this.previewMap.clear();
        if (lat != 0.0 && lng != 0.0)
        {
            LatLng location = new LatLng(lat, lng);
            this.previewMap.addMarker(new MarkerOptions().position(location).title("Selected Location"));
            this.previewMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        this.mapPreview.onResume();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        this.mapPreview.onPause();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        this.mapPreview.onDestroy();
    }

    @Override
    public void onLowMemory()
    {
        super.onLowMemory();
        this.mapPreview.onLowMemory();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        this.mapPreview.onSaveInstanceState(outState);
        outState.putDouble("lat", this.lat);
        outState.putDouble("lng", this.lng);
        outState.putString("mode", this.mode);
        outState.putString("id", this.id);
        outState.putString("iconName", this.selectedIconName);
        outState.putInt("iconColor", this.selectedIconColor);
    }
}
