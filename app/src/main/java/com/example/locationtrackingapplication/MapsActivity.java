package com.example.locationtrackingapplication;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.locationtrackingapplication.databinding.ActivityMapsBinding;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.PrimitiveIterator;
import java.util.Set;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private LocationListener locationListener;
    private LocationManager locationManager;
    private final long MIN_DIST = 1;
    private LatLng latLng;
    private ArrayList<String> latitude, longitude;
//    private double currentLongitude=0, currentLatitude=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        latitude = new ArrayList<>();
        longitude = new ArrayList<>();
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
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                try {
//                    LatLng tempLatLng = null;
//                    if (latLng != null)
//                        tempLatLng = latLng;
                    latLng = new LatLng(location.getLatitude(), location.getLongitude());
//                    if (tempLatLng == latLng) {
                        mMap.addMarker(new MarkerOptions().position(latLng).title("My position"));
//                        latitude.add(location.getLatitude() + "");
//                        longitude.add/(location.getLongitude() + "");
//                        saveLongitudeArray(getApplicationContext(), longitude);
//                        saveLatitudeArray(getApplicationContext(), latitude);
//                    }/**/
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 13));

                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
                            .zoom(17)                   // Sets the zoom
                            .bearing(90)                // Sets the orientation of the camera to east
                            .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                            .build();                   // Creates a CameraPosition from the builder
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

//                    mMap.addMarker(new MarkerOptions().position(latLng).title("My position"));


//                    new CountDownTimer(30000, 1000) {
//
//                        public void onTick(long millisUntilFinished) {
//
////                            mTextField.setText("seconds remaining: " + millisUntilFinished / 1000);
//                            //here you can have your logic to latitudeSet text to edittext
//                        }
//
//                        public void onFinish() {
//
////
//                        }
//
//                    }.start();


                } catch (SecurityException e) {
                    e.printStackTrace();
                }

            }


            @Override
            public void onProviderEnabled(@NonNull String provider) {

            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {

            }
        };
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //   ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;

        }
        long MIN_TIME = 60000;
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DIST, locationListener);

    }

    public static boolean saveLongitudeArray(Context context, ArrayList<String> longitudeArrayList) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor mEdit1 = sp.edit();
        /* sKey is an array */
        mEdit1.putInt("LOStatus_size", longitudeArrayList.size());

        for (int i = 0; i < longitudeArrayList.size(); i++) {
            mEdit1.remove("LOStatus_" + i);
            mEdit1.putString("LOStatus_" + i, longitudeArrayList.get(i));
        }

        return mEdit1.commit();
    }

    public static boolean saveLatitudeArray(Context context, ArrayList<String> latitudeArrayList) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor mEdit1 = sp.edit();
        /* sKey is an array */
        mEdit1.putInt("LAStatus_size", latitudeArrayList.size());

        for (int i = 0; i < latitudeArrayList.size(); i++) {
            mEdit1.remove("LAStatus_" + i);
            mEdit1.putString("LAStatus_" + i, latitudeArrayList.get(i));
        }

        return mEdit1.commit();
    }
}