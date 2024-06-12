package com.carlex.drive;


import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class FusedLocationsProvider implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    private final FusedLocationProviderClient fusedLocationClient;
    private final Context context;
    private final Location location;
    private final GoogleApiClient apiClient;
    public static Context xthis;

    private static final String GPS_PROVIDER = LocationManager.GPS_PROVIDER;

    public FusedLocationsProvider(Context context) {
        this.context = context;
	xthis = context;

	Location location = new Location(GPS_PROVIDER);

        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
	this.location = location;

        this.apiClient = new GoogleApiClient.Builder(this.context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        this.apiClient.connect();

	Log.d("Fused", "criadi");
    }


    public void sToast(String txt){
	    Toast.makeText(xthis, txt, Toast.LENGTH_SHORT).show();
    }

   // public Location build(double latitude, double longitude, float accuracy, float bearing, float speed, float altitude) {
       /* float speedInMeters = speed / 3.6f;

        location.setLatitude(latitude);
        location.setLongitude(longitude);
        location.setSpeed(speedInMeters);
        location.setAccuracy(accuracy);
        location.setAltitude(altitude);
        location.setTime(System.currentTimeMillis());
        location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        location.setBearing(bearing);*/

    public Location build(Location location) {
        return this.location;
    }

    public void spoof(Location location) {
       // if (!isMockLocationsEnabled())
         //   return;

        if (apiClient.isConnected()) {
            try {
                fusedLocationClient.setMockMode(true);
                fusedLocationClient.setMockLocation(location);
            } catch (SecurityException e) {
                Log.d("Fused", "SecurityException", e);
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        try {
            fusedLocationClient.setMockMode(true);
        } catch (SecurityException e) {
		Log.d("fused","FALHA AO CONECTAR FUSED", e);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        // Handle the connection suspended case if neede
	Log.d("fused", "SUSPENSO FAKE FUSED");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // Handle the connection failed case if need
	Log.d("fused", "FALHA FAKE FUSED");
    }

    private boolean isMockLocationsEnabled() {
        try {
            return Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION) == 1;
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
	    Log.d("fused", "FALHA AO AUTORIZAR FAKE FUSED");
            return false;
        }
    }
}

