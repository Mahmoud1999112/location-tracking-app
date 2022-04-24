package com.example.locationtrackingapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {
    private double longitude, latitude;
    static HistoryActivity instance;
    private ArrayList<String> latitudeStringList, longitudeStringList, finalAnswerList;
    private ArrayList<Double> latitudeList, longitudeList;
    private ArrayList<ArrayList<String>> finalLongitudeList, finalLatitudeList;
    private ListView l;

    public static HistoryActivity getInstance() {
        return instance;
    }

    public void receiveNewLocation(double longitude, double latitude) {
        HistoryActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                txt_location.setText(value);
// here we got the location values

                Geocoder geocoder;
                List<Address> addresses;
                ArrayList<String> addressesList;
                addressesList = new ArrayList<>();
                geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                try {
                    addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                    String knownName = addresses.get(0).getFeatureName();
                    addressesList.add(knownName);

                } catch (IOException e) {
                    e.printStackTrace();
                }


//        String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()

//            String city = addresses.get(0).getLocality();
//            String state = addresses.get(0).getAdminArea();
//            String country = addresses.get(0).getCountryName();
//            String postalCode = addresses.get(0).getPostalCode();
                for (int i = 0; i < addressesList.size(); i++) {
                    try {
                        if (addressesList.get(i).equals(addressesList.get(i + 1)))
                            finalAnswerList.add(i, "You moved around the same area: " + addressesList.get(i));
                        else
                            finalAnswerList.add(i, "You traveled from: " + addressesList.get(i) + " to: " + addressesList.get(i + 1));
                    } catch (IndexOutOfBoundsException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        latitudeStringList = new ArrayList<String>();
        longitudeStringList = new ArrayList<String>();
        latitudeList = new ArrayList<Double>();
        longitudeList = new ArrayList<Double>();
        finalLatitudeList = new ArrayList<>();
        finalLongitudeList = new ArrayList<>();
        l = (ListView) findViewById(R.id.list);
        finalAnswerList = new ArrayList<>();
        loadLatitudeArray(getApplicationContext(), latitudeStringList);
        loadLongitudeArray(getApplicationContext(), longitudeStringList);
        for (String latitudeString : latitudeStringList
        ) {
            latitudeList.add(Double.parseDouble(latitudeString));
        }
        for (String longitudeString : longitudeStringList
        ) {
            longitudeList.add(Double.parseDouble(longitudeString));
        }
        for (int i = 0; i < latitudeList.size(); i++) {
            ArrayList<String> tempList = new ArrayList<>();
            try {
                tempList.set(i, latitudeList.get(i) + "");
                tempList.set(i + 1, latitudeList.get(i + 1) + "");
                finalLatitudeList.set(i, tempList);
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }

        }
        for (int i = 0; i < longitudeList.size(); i++) {
            ArrayList<String> tempList = new ArrayList<>();
            try {
                tempList.set(i, longitudeList.get(i) + "");
                tempList.set(i + 1, longitudeList.get(i + 1) + "");
                finalLongitudeList.set(i, tempList);
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }

        }


        ArrayAdapter<String> arr;
        arr
                = new ArrayAdapter<String>
                (this, R.layout.support_simple_spinner_dropdown_item,
                        finalAnswerList);
        l.setAdapter(arr);
        l.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getApplicationContext(), MapHistoryActivity.class);
                try {
                    saveLongitudeArray(getApplicationContext(), finalLongitudeList.get(position));
                    saveLatitudeArray(getApplicationContext(), finalLatitudeList.get(position));
                    startActivity(i);
                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    public static void loadLatitudeArray(Context mContext, ArrayList<String> latitudeList) {
        SharedPreferences mSharedPreference1 = PreferenceManager.getDefaultSharedPreferences(mContext);
        latitudeList.clear();
        int size = mSharedPreference1.getInt("LAStatus_size", 0);

        for (int i = 0; i < size; i++) {
            latitudeList.add(mSharedPreference1.getString("LAStatus_" + i, null));
        }

    }

    public static void loadLongitudeArray(Context mContext, ArrayList<String> longitudeList) {
        SharedPreferences mSharedPreference1 = PreferenceManager.getDefaultSharedPreferences(mContext);
        longitudeList.clear();
        int size = mSharedPreference1.getInt("LOStatus_size", 0);

        for (int i = 0; i < size; i++) {
            longitudeList.add(mSharedPreference1.getString("LOStatus_" + i, null));
        }

    }

    public static boolean saveLongitudeArray(Context context, ArrayList<String> longitudeArrayList) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor mEdit1 = sp.edit();
        /* sKey is an array */
        mEdit1.putInt("HistoryLoStatus_size", longitudeArrayList.size());

        for (int i = 0; i < longitudeArrayList.size(); i++) {
            mEdit1.remove("HistoryLoStatus_" + i);
            mEdit1.putString("HistoryLoStatus_" + i, longitudeArrayList.get(i));
        }

        return mEdit1.commit();
    }

    public static boolean saveLatitudeArray(Context context, ArrayList<String> latitudeArrayList) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor mEdit1 = sp.edit();
        /* sKey is an array */
        mEdit1.putInt("HistoryLaStatus_size", latitudeArrayList.size());

        for (int i = 0; i < latitudeArrayList.size(); i++) {
            mEdit1.remove("HistoryLaStatus_" + i);
            mEdit1.putString("HistoryLaStatus_" + i, latitudeArrayList.get(i));
        }

        return mEdit1.commit();
    }

    }
