package com.example.locationtrackingapplication;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MyBackgroundService extends Service {
    private static final String CHANNEL_ID = "my_channel";
    private static final String EXTRA_STARTED_FROM_NOTIFICATION = "com.example.locationtrackingapplication" + ".started_from_notification";
    private final IBinder mBinder = new LocalBinder();
    private static final long UPDATED_INTERVAL_IN_MIL = 10000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MIL = UPDATED_INTERVAL_IN_MIL / 2;
    private static final int NOTI_ID = 1223;
    private boolean mChangingConfiguration = false;
    private NotificationManager mNotificationManager;
    private LocationRequest locationRequest;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private Handler mServiceHandler;
    private Location mLocation;

    public MyBackgroundService() {

    }

    @Override
    public void onCreate() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                onNewLocation(locationResult.getLastLocation());
            }
        };
        createLocationRequest();
        getLastLocation();
        HandlerThread handlerThread = new HandlerThread("EDMTDev");
        handlerThread.start();
        ;
        mServiceHandler = new Handler(handlerThread.getLooper());
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT);
            mNotificationManager.createNotificationChannel(mChannel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean startedFomNotification = intent.getBooleanExtra(EXTRA_STARTED_FROM_NOTIFICATION, false);
        if (startedFomNotification) {
            removeLocationUpdates();
            stopSelf();
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mChangingConfiguration = true;
    }

    public void removeLocationUpdates() {
        try {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
            Common.setRequestingLocationUpdates(this, false);
            stopSelf();
        } catch (SecurityException e) {
            Common.setRequestingLocationUpdates(this, true);
            Log.e("EDMT_DEV", "lost location permission, could not removed updates" + e);
        }
    }

    private void getLastLocation() {
        try {

            fusedLocationProviderClient.getLastLocation()
                    .addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if (task.isSuccessful() && task.getResult() != null)
                                mLocation = task.getResult();
                            else
                                Log.e("EDMT_DEV", "Failed to get location");
                        }
                    });
        } catch (SecurityException e) {
            Log.e("EDMT_DEV", "Lost location permission" + e);
        }
    }

    private void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(30000);
        locationRequest.setFastestInterval(15000);
        locationRequest.setSmallestDisplacement(5);

//        locationRequest.setNumUpdates(1);

        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void onNewLocation(Location lastLocation) {
        mLocation = lastLocation;
        EventBus.getDefault().postSticky(new SendLocationToActivity(mLocation));
        if (serviceIsRunningInForeGround(this))
            mNotificationManager.notify(NOTI_ID, getNotification());
    }

    private Notification getNotification() {
        Intent intent = new Intent(this, MyBackgroundService.class);
        Geocoder geocoder;
        List<Address> addresses;

        geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        String text = Common.getLocationText(mLocation);


        try {
            addresses = geocoder.getFromLocation(mLocation.getLatitude(), mLocation.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            String knownName = addresses.get(0).getFeatureName();
//                String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            String city = addresses.get(0).getLocality();
            text = "You're currently moving around: " + city;

        } catch (IOException e) {
            e.printStackTrace();
        }

        intent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION, true);
        PendingIntent servicePendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent activityPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, BackgroundLocationActivity.class), 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .addAction(R.drawable.ic_baseline_launch_24, "Launch", activityPendingIntent)
                .addAction(R.drawable.ic_baseline_cancel_24, "Remove", servicePendingIntent)
                .setContentText(text)
                .setContentTitle(Common.getLocationTitle(this))
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker(text)
                .setWhen(System.currentTimeMillis());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID);
        }
        return builder.build();


    }

    private boolean serviceIsRunningInForeGround(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
            if (getClass().getName().equals(service.service.getClassName()))
                if (service.foreground)
                    return true;
        return false;
    }

    public void requestLocationUpdates() {
        Common.setRequestingLocationUpdates(this, true);
        startService(new Intent(getApplicationContext(), MyBackgroundService.class));
        try {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        } catch (SecurityException e) {
            Log.e("EDMT_DEV", "lost location permission could no request it" + e);
        }
    }

    public class LocalBinder extends Binder {
        MyBackgroundService getServices() {
            return MyBackgroundService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        stopForeground(true);
        mChangingConfiguration = false;
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        stopForeground(true);
        mChangingConfiguration = false;
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (!mChangingConfiguration && Common.requestingLocationUpdates(this))
            startForeground(NOTI_ID, getNotification());
        return true;
    }

    @Override
    public void onDestroy() {
        mServiceHandler.removeCallbacks(null);
        super.onDestroy();
    }
}
