package com.nhlstenden.navigationapp.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;
import android.widget.EditText;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nhlstenden.navigationapp.BaseActivity;
import com.nhlstenden.navigationapp.R;
import com.nhlstenden.navigationapp.helpers.ToastUtils;

import java.io.IOException;
import java.util.List;

public class MapActivity extends BaseActivity implements OnMapReadyCallback
{
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng selectedLocation;
    private Button btnSaveWaypoint;

    private final ActivityResultLauncher<Intent> createWaypointLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result ->
            {
                if (result.getResultCode() == RESULT_OK)
                {
                    setResult(RESULT_OK, result.getData());
                    finish();
                }
                else if (result.getResultCode() == RESULT_CANCELED)
                {
                    ToastUtils.show(this, "Waypoint creation cancelled", Toast.LENGTH_SHORT);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        TextView headerTitle = findViewById(R.id.headerTitle);
        if (headerTitle != null)
        {
            headerTitle.setText("Loot land");
        }
        this.setupSettingsPanel();

        SupportMapFragment mapFragment = new SupportMapFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.map_container, mapFragment)
                .commit();
        mapFragment.getMapAsync(this);

        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        EditText searchEditText = findViewById(R.id.searchEditText);
        searchEditText.setOnEditorActionListener((v, actionId, event) ->
        {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN))
            {
                String locationName = searchEditText.getText().toString().trim();
                if (!locationName.isEmpty())
                {
                    this.searchLocation(locationName);
                }
                return true;
            }
            return false;
        });

        this.btnSaveWaypoint = findViewById(R.id.btnSaveWaypoint);
        this.btnSaveWaypoint.setEnabled(false);
        this.btnSaveWaypoint.setOnClickListener(v -> this.saveWaypoint());

        Button btnBackWaypoint = findViewById(R.id.btnBackWaypoint);
        btnBackWaypoint.setOnClickListener(v ->
        {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("cancel_reason", "User pressed back on map");
            setResult(RESULT_CANCELED, resultIntent);
            finish();
        });

        Intent intent = getIntent();
        if (intent.hasExtra("lat") && intent.hasExtra("lng"))
        {
            double lat = intent.getDoubleExtra("lat", 0.0);
            double lng = intent.getDoubleExtra("lng", 0.0);
            if (lat != 0.0 && lng != 0.0)
            {
                this.selectedLocation = new LatLng(lat, lng);
                this.btnSaveWaypoint.setEnabled(true);
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        this.mMap = googleMap;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
        {
            this.enableMyLocation();
        }
        else
        {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }

        this.mMap.setOnMapClickListener(latLng ->
        {
            this.selectedLocation = latLng;
            this.mMap.clear();
            this.mMap.addMarker(new MarkerOptions().position(latLng).title("Selected Location"));
            this.btnSaveWaypoint.setEnabled(true);
        });

        if (this.selectedLocation != null)
        {
            this.mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(this.selectedLocation, 15));
            this.mMap.addMarker(new MarkerOptions().position(this.selectedLocation).title("Selected Location"));
        }
    }

    private void enableMyLocation()
    {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
        {
            this.mMap.setMyLocationEnabled(true);
            this.fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location ->
                    {
                        if (location != null)
                        {
                            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            this.mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                        }
                    });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                this.enableMyLocation();
            }
            else
            {
                ToastUtils.show(this, "Location permission is required for this feature",
                        Toast.LENGTH_LONG);
            }
        }
    }

    private void saveWaypoint()
    {
        if (this.selectedLocation != null)
        {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("lat", this.selectedLocation.latitude);
            resultIntent.putExtra("lng", this.selectedLocation.longitude);
            setResult(RESULT_OK, resultIntent);
            finish();
        }
    }

    private void searchLocation(String locationName)
    {
        Geocoder geocoder = new Geocoder(this);
        try
        {
            List<Address> addressList = geocoder.getFromLocationName(locationName, 1);
            if (addressList != null && !addressList.isEmpty())
            {
                Address address = addressList.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                this.selectedLocation = latLng;
                this.mMap.clear();
                this.mMap.addMarker(new MarkerOptions().position(latLng).title(locationName));
                this.mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                this.btnSaveWaypoint.setEnabled(true);
            }
            else
            {
                ToastUtils.show(this, "Address not found", Toast.LENGTH_SHORT);
            }
        }
        catch (IOException e)
        {
            Log.e("MapActivity", "Geocoder failed", e);
            ToastUtils.show(this, "Geocoding failed: " + e.getMessage(), Toast.LENGTH_LONG);
        }
    }
}