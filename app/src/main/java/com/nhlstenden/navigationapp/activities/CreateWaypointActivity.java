package com.nhlstenden.navigationapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.nhlstenden.navigationapp.R;

import com.nhlstenden.navigationapp.models.Waypoint;

public class CreateWaypointActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText nameEditText, descriptionEditText;
    private Button selectPhotoButton, saveWaypointButton;
    private ImageView selectedPhotoImageView;
    private Uri selectedPhotoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_waypoint);

        nameEditText = findViewById(R.id.nameEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        selectPhotoButton = findViewById(R.id.selectPhotoButton);
        saveWaypointButton = findViewById(R.id.saveWaypointButton);
        selectedPhotoImageView = findViewById(R.id.selectedPhotoImageView);

        selectPhotoButton.setOnClickListener(v -> openGallery());

        saveWaypointButton.setOnClickListener(v -> saveWaypoint());
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedPhotoUri = data.getData();
            selectedPhotoImageView.setImageURI(selectedPhotoUri);
        }
    }

    private void saveWaypoint() {
        String name = nameEditText.getText().toString();
        String description = descriptionEditText.getText().toString();

        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT).show();
            return;
        }

        String photoUriString = selectedPhotoUri != null ? selectedPhotoUri.toString() : null;

        Waypoint waypoint = new Waypoint(name, description, photoUriString, 0.0, 0.0); // location not yet set
        Intent resultIntent = new Intent();
        resultIntent.putExtra("WAYPOINT", waypoint);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }
}
