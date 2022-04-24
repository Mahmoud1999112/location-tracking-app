package com.example.locationtrackingapplication;

import android.Manifest;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.snackbar.Snackbar;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static com.example.locationtrackingapplication.Common.KEY_REQUESTING_LOCATION_UPDATES;

public class BackgroundLocationActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    static BackgroundLocationActivity instance;
    LocationRequest locationRequest;
    FusedLocationProviderClient fusedLocationProviderClient;
    TextView txt_location;
    Button requestLocation, removeLocation, locationBtn;
    ArrayList<String> finalAnswerList;
    MyBackgroundService mService = null;
    ListView l;
    ArrayAdapter<String> arr;
    ArrayList<String> addressesList;
    ArrayList<String> checkIfMoving;
    ArrayList<Double> finalLongitude, finalLatitude;
    boolean mBound = false;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MyBackgroundService.LocalBinder binder = (MyBackgroundService.LocalBinder) service;
            mService = binder.getServices();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };

    public static BackgroundLocationActivity getInstance() {
        return instance;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_background_location);
        instance = this;
        txt_location = findViewById(R.id.txt_location);
        locationBtn = findViewById(R.id.location);
        finalAnswerList = new ArrayList<>();
        addressesList = new ArrayList<>();
        checkIfMoving = new ArrayList<>();
        finalLongitude = new ArrayList<>();
        finalLatitude = new ArrayList<>();
        l = findViewById(R.id.list);
        arr
                = new ArrayAdapter<String>
                (getApplicationContext(), R.layout.support_simple_spinner_dropdown_item,
                        finalAnswerList);
        l.setAdapter(arr);
        Intent intent = new Intent(this, MapsActivity.class);
//        Intent intent1 = new Intent(this, HistoryActivity.class);
        requestLocation = findViewById(R.id.request_location_updates_button);
        removeLocation = findViewById(R.id.remove_location_updates_button);
        l.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getApplicationContext(), MapHistoryActivity.class);
                try {
                    SharedPreferences.Editor editor = getSharedPreferences("MY_PREFS_NAME", MODE_PRIVATE).edit();
                    editor.putString("FLongitude", finalLongitude.get(position) + "");
                    editor.putString("FLatitude", finalLatitude.get(position) + "");
                    editor.putString("SLongitude", finalLongitude.get(position + 1) + "");
                    editor.putString("SLatitude", finalLatitude.get(position + 1) + "");
                    editor.commit();
                    startActivity(i);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
        locationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(intent);
            }
        });
        if (!checkPermissions()) {
            requestPermissions();
        }
        Dexter.withActivity(this).withPermissions(Arrays.asList(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION)).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {

                requestLocation.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mService.requestLocationUpdates();

                    }
                });
                removeLocation.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mService.removeLocationUpdates();
                    }
                });
                setButtonState(Common.requestingLocationUpdates(BackgroundLocationActivity.this));

                bindService(new Intent(BackgroundLocationActivity.this, MyBackgroundService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {

            }
        }).check();
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

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i("", "Displaying permission rationale to provide additional context.");
            Snackbar.make(
                    findViewById(R.id.remove_location_updates_button),
                    "permission",
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction("ok", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(BackgroundLocationActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    })
                    .show();
        } else {
            Log.i("TAG", "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(BackgroundLocationActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }


    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    protected void onStart() {
        super.onStart();
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);
        EventBus.getDefault().register(this);

    }

    @Override
    protected void onStop() {
        if (mBound) {
            unbindService(mServiceConnection);
            mBound = false;
        }
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
        EventBus.getDefault().unregister(this);
        super.onStop();
    }
//39.223451
//    39.223691

    private void updateLocation() {
//        buildLocationRequest();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, getPendingIntent());
    }

    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(this, MyLocationService.class);
        intent.setAction(MyLocationService.ACTION_PROCESS_UPDATE);
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

//    private void buildLocationRequest() {
//        locationRequest = new LocationRequest();
//        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//        locationRequest.setInterval(5000);
//        locationRequest.setFastestInterval(5000);
//        locationRequest.setSmallestDisplacement(1f);
//    }

    public void updateTextView(String value, double latitude, double longitude) {
        BackgroundLocationActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Geocoder geocoder;
                List<Address> addresses;

                geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                try {
                    addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                    String knownName = addresses.get(0).getFeatureName();
                    String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                    String city = addresses.get(0).getLocality();
                    txt_location.setText("You're currently in : " + address);
                    checkIfMoving.add(address);

                    for (int i = 0; i < checkIfMoving.size(); i++) {
                        if (checkIfMoving.get(i).equals(checkIfMoving.get(i + 1))) {
                            addressesList.add(knownName);
                            int counter = 0;
                            for (int j = 0; j < addressesList.size(); j++) {
                                try {
                                    if (addressesList.get(j).equals(addressesList.get(j + 1))) {
                                        if (counter == 0) {
                                            finalAnswerList.add("You're moving around the same area :)");

                                        }
                                        finalLongitude.add(longitude);
                                        finalLatitude.add(latitude);
                                        counter++;
                                    } else {
                                        finalAnswerList.add("From: " + addressesList.get(j) + " to: " + addressesList.get(j + 1));
                                        finalLatitude.add(latitude);
                                        finalLongitude.add(longitude);
                                    }
                                    String lastLocation = addressesList.get(j + 1);
                                    addressesList.clear();
                                    addressesList.add(lastLocation);
                                    for (int k = 0; k < finalAnswerList.size(); k++) {
                                        try {
                                            if (finalAnswerList.get(k).equals(finalAnswerList.get(k + 1)))
                                                finalAnswerList.remove(k + 1);
                                        } catch (IndexOutOfBoundsException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    arr.notifyDataSetChanged();
                                    arr
                                            = new ArrayAdapter<String>
                                            (getApplicationContext(), R.layout.support_simple_spinner_dropdown_item,
                                                    finalAnswerList);
                                    l.setAdapter(arr);
                                } catch (IndexOutOfBoundsException e) {
                                    finalLatitude.add(latitude);
                                    finalLongitude.add(longitude);
                                    e.printStackTrace();
                                }
                            }

                        }
                    }
//            String state = addresses.get(0).getAdminArea();
//            String country = addresses.get(0).getCountryName();
//            String postalCode = addresses.get(0).getPostalCode();


                } catch (IOException e) {
                    e.printStackTrace();
                }


//                for (int i = 0; i <= addressesList.size(); i++) {
//                    try {
//
//                        if (addressesList.get(i).equals(addressesList.get(i + 1)))
//                            finalAnswerList.add(i, "You moved around the same area: " + addressesList.get(i));
//                        else
//                            finalAnswerList.add(i, "You traveled from: " + addressesList.get(i) + " to: " + addressesList.get(i + 1));
//                    } catch (IndexOutOfBoundsException e) {
//                        e.printStackTrace();
//                    }
//                }


            }
        });
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(KEY_REQUESTING_LOCATION_UPDATES)) {
            setButtonState(sharedPreferences.getBoolean(Common.KEY_REQUESTING_LOCATION_UPDATES, false));
        }
    }

    private void setButtonState(boolean isRequestEnable) {
        if (isRequestEnable) {

            requestLocation.setEnabled(false);
            removeLocation.setEnabled(true);
        } else {
            requestLocation.setEnabled(true);
            removeLocation.setEnabled(false);
        }


    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onListenLocation(SendLocationToActivity event) {
        if (event != null) {
            String data = new StringBuilder()
                    .append(event.getLocation().getLatitude())
                    .append("/")
                    .append(event.getLocation().getLongitude())
                    .toString();
            Geocoder geocoder;
            List<Address> addresses;

            geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

            try {
                addresses = geocoder.getFromLocation(event.getLocation().getLatitude(), event.getLocation().getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                String knownName = addresses.get(0).getFeatureName();
//                String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                String city = addresses.get(0).getLocality();
                addressesList.add(knownName);
            } catch (IOException e) {
                e.printStackTrace();
            }

            BackgroundLocationActivity.getInstance().updateTextView(data, event.getLocation().getLatitude(), event.getLocation().getLongitude());
            Toast.makeText(mService, data, Toast.LENGTH_SHORT).show();

        }
    }
}