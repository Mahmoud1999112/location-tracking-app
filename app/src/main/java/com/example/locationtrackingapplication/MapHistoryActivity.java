package com.example.locationtrackingapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.fragment.app.FragmentActivity;

import com.example.locationtrackingapplication.databinding.ActivityMapHistoryBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MapHistoryActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapHistoryBinding binding;
    private LatLng latLng, latLng2;
    private double fLongitude, sLongitude, fLatitude, sLatitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
//        loadLatitudeArray(getApplicationContext(), latitudeString);
//        loadLongitudeArray(getApplicationContext(), longitudeString);
        SharedPreferences prefs = getSharedPreferences("MY_PREFS_NAME", MODE_PRIVATE);
        fLongitude = Double.parseDouble(prefs.getString("FLongitude", ""));
        fLatitude = Double.parseDouble(prefs.getString("FLatitude", ""));
        sLongitude = Double.parseDouble(prefs.getString("SLongitude", ""));
        sLatitude = Double.parseDouble(prefs.getString("SLatitude", ""));
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera

        latLng = new LatLng(sLatitude, sLongitude);
        latLng2 = new LatLng(fLatitude, fLongitude);
        mMap.addMarker(new MarkerOptions().position(latLng).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(sLatitude, sLongitude), 13));

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(sLatitude, sLongitude))      // Sets the center of the map to location user
                .zoom(17)                   // Sets the zoom
                .bearing(90)                // Sets the orientation of the camera to east
                .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    public static void loadLatitudeArray(Context mContext, ArrayList<String> latitudeList) {
        SharedPreferences mSharedPreference1 = PreferenceManager.getDefaultSharedPreferences(mContext);
        latitudeList.clear();
        int size = mSharedPreference1.getInt("HistoryLoStatus_size", 0);

        for (int i = 0; i < size; i++) {
            latitudeList.add(mSharedPreference1.getString("HistoryLoStatus_" + i, null));
        }

    }

    public static void loadLongitudeArray(Context mContext, ArrayList<String> longitudeList) {
        SharedPreferences mSharedPreference1 = PreferenceManager.getDefaultSharedPreferences(mContext);
        longitudeList.clear();
        int size = mSharedPreference1.getInt("HistoryLaStatus_size", 0);

        for (int i = 0; i < size; i++) {
            longitudeList.add(mSharedPreference1.getString("HistoryLaStatus_" + i, null));
        }

    }
}
