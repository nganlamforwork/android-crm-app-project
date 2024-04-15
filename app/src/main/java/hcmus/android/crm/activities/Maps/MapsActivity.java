package hcmus.android.crm.activities.Maps;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import hcmus.android.crm.R;
import hcmus.android.crm.activities.DrawerBaseActivity;
import hcmus.android.crm.databinding.ActivityMapsBinding;

public class MapsActivity extends DrawerBaseActivity implements OnMapReadyCallback {
    private ActivityMapsBinding binding;
    private GoogleMap myMap;
    private Marker marker;
    private double latitude, longitude;
    private String name, address;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Location");

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.appBar.toolbar;
        setSupportActionBar(toolbar);

        // Enable the back button in the action bar or toolbar
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        // Retrieve latitude and longitude from intent extras
        Intent intent = getIntent();
        if (intent != null) {
            latitude = Double.parseDouble(intent.getStringExtra("latitude"));
            longitude = Double.parseDouble(intent.getStringExtra("longitude"));
            name = intent.getStringExtra("name");
            address = intent.getStringExtra("address");
        }

        Log.d("LAT", String.valueOf(latitude));
        Log.d("LONG", String.valueOf(longitude));

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        myMap = googleMap;

        // Create a LatLng object for the marker position
        LatLng location = new LatLng(latitude, longitude);

        // Create and add a marker at the specified location with title and snippet
        marker = myMap.addMarker(new MarkerOptions()
                .position(location)
                .title(name)
                .snippet(address));
        // Move the camera to the location and set the zoom level
        myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
    }


    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }

}
