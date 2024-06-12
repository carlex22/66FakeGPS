package com.carlex.drive;

import android.content.Context;
import android.location.GnssStatus;
import android.location.LocationManager;
import android.os.Build;
import android.util.Log;
import androidx.annotation.RequiresApi;

public class GNSSManager {
    private static final String TAG = "GNSSManager";
    private LocationManager locationManager;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public GNSSManager(Context context) {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        GnssStatus.Callback gnssStatusCallback = new GnssStatus.Callback() {
            @Override
            public void onStarted() {
                //Log.d(TAG, "GNSS status started");
            }

            @Override
            public void onStopped() {
                //Log.d(TAG, "GNSS status stopped");
            }

            @Override
            public void onSatelliteStatusChanged(GnssStatus status) {
            //    Log.d(TAG, "GNSS status updated");
                GNSSStatusHolder.setGnssStatus(status);
            }
        };

        if (locationManager != null) {
            locationManager.registerGnssStatusCallback(gnssStatusCallback);
        }
    }
}

