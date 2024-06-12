package com.carlex.drive;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.os.Build;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;

import androidx.core.content.ContextCompat;

import java.util.Random;



import android.Manifest;                                          import android.content.Context;                                   import android.content.pm.PackageManager;                         import android.location.Criteria;                                 import android.location.Location;                                 import android.location.LocationManager;                          import android.os.Build;                                          import android.os.SystemClock;                                    import android.provider.Settings;                                 import android.util.Log;                                          import android.location.provider.ProviderProperties;                                                                                import androidx.core.content.ContextCompat;                       import androidx.core.view.InputDeviceCompat;                                                                                        import java.util.Random;                                          import java.util.concurrent.ThreadLocalRandom;

public class MockLocProvider {
/*
    private static Location gpsLocation;
    private static Location networkLocation;

    private static final String GPS_PROVIDER = LocationManager.GPS_PROVIDER;
    private static final String NETWORK_PROVIDER = LocationManager.NETWORK_PROVIDER;

    private static boolean isMockLocationsEnabled;

    private static Random random;


    public static void setGpsProvider(xLocationManager xLocationManager, double latitude, double longitude, float bearing, float speed, float accuracy, float altitude) {
        float speedInMeters = speed / 3.6f;
        gpsLocation.setLatitude(latitude);
        gpsLocation.setLongitude(longitude);
        gpsLocation.setAltitude(altitude);
        gpsLocation.setBearing(bearing);
        gpsLocation.setAccuracy(accuracy);
        gpsLocation.setSpeed(speedInMeters);
        gpsLocation.setTime(getTime());
        gpsLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            gpsLocation.setVerticalAccuracyMeters(accuracy - ((accuracy >= 5) ? (float) ThreadLocalRandom.current().nextDouble(0, 4) : 0));
            gpsLocation.setSpeedAccuracyMetersPerSecond(20.0f);
            gpsLocation.setBearingAccuracyDegrees(10.0f);
        }

        try {
            xLocationManager.setTestProviderLocation(GPS_PROVIDER, gpsLocation);
        } catch (SecurityException se) {
            Log.d("MockLocProvider", null, se);
        }
    }

    public static void setNetworkProvider(xLocationManager xLocationManager, double latitude, double longitude, float accuracy, float bearing, float altitude) {
        networkLocation.setLatitude(latitude);
        networkLocation.setLongitude(longitude);
        networkLocation.setTime(getTime());
        networkLocation.setBearing(bearing);
        networkLocation.setAltitude(altitude);
        networkLocation.setAccuracy(accuracy);
        networkLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            networkLocation.setVerticalAccuracyMeters(100.f);
            networkLocation.setSpeedAccuracyMetersPerSecond(20.f);
            networkLocation.setBearingAccuracyDegrees(10.0f);
        }
        try {
            xLocationManager.setTestProviderLocation(NETWORK_PROVIDER, networkLocation);
        } catch (SecurityException se) {
            Log.d("MockLocProvider", null, se);
        }
    }

    public static void reportLocation(xLocationManager xLocationManager, double latitude, double longitude, float accuracy, float bearing, float speed, float altitude) {
        try {
            gpsLocation.setLatitude(latitude);
            gpsLocation.setLongitude(longitude);
            gpsLocation.setTime(getTime());
            gpsLocation.setAccuracy(accuracy);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                gpsLocation.setVerticalAccuracyMeters(100.0f);
                gpsLocation.setSpeedAccuracyMetersPerSecond(20.0f);
                gpsLocation.setBearingAccuracyDegrees(10.0f);
            }
            gpsLocation.setAltitude(altitude);
            gpsLocation.setBearing(bearing);
            gpsLocation.setSpeed(speed);
            gpsLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        } catch (Exception e) {
            Log.d("MockLocProvider", null, e);
        }
    }

    private static long getTime() {
        return System.currentTimeMillis() - ((long) random.nextInt(InputDeviceCompat.SOURCE_KEYBOARD));
    }*/

}



/*package com.carlex.drive;



import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.location.provider.ProviderProperties; 

import androidx.core.content.ContextCompat;
import androidx.core.view.InputDeviceCompat;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class MockLocProvider {

    private static Location gpsLocation;
    private static Location networkLocation;
    private static LocationManager locationManager;

    private static final String GPS_PROVIDER = LocationManager.GPS_PROVIDER;
    private static final String NETWORK_PROVIDER = LocationManager.NETWORK_PROVIDER;

    private static boolean isMockLocationsEnabled;

    private static Random random;

    public static void initTestProvider(Context context) {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        gpsLocation = new Location(GPS_PROVIDER);
        networkLocation = new Location(NETWORK_PROVIDER);
        isMockLocationsEnabled = areLocationPermissionsGranted(context) && isMockLocationsEnabled(context);
        random = new Random();

        if (isMockLocationsEnabled) {
            try {
                removeProviders();



		locationManager.addTestProvider(GPS_PROVIDER,false, false, false, false, true, true, true,ProviderProperties.POWER_USAGE_LOW,ProviderProperties.ACCURACY_FINE); 
			// Adjust as needed


                locationManager.setTestProviderEnabled(GPS_PROVIDER, true);


		locationManager.addTestProvider(NETWORK_PROVIDER,false, false, false, false, true, true, true, ProviderProperties.POWER_USAGE_LOW, ProviderProperties.ACCURACY_COARSE);                           
								
                locationManager.setTestProviderEnabled(NETWORK_PROVIDER, true);

            } catch (IllegalArgumentException | SecurityException e) {
                Log.d("MockLocProvider", null, e);
            }
            return;
        }
    }

    public static void setGpsProvider(double latitude, double longitude, float bearing, float speed, float accuracy, float altitude) {
        float speedInMeters = speed / 3.6f;
        gpsLocation.setLatitude(latitude);
        gpsLocation.setLongitude(longitude);
        gpsLocation.setAltitude(altitude);
        gpsLocation.setBearing(bearing);
        gpsLocation.setAccuracy(accuracy);
        gpsLocation.setSpeed(speedInMeters);
        gpsLocation.setTime(getTime());
        gpsLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());

        Log.d("MockLocProvider", "bearing: " + bearing + " speed: " + speed + " accuracy: " + accuracy + " altitude: " + altitude);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            gpsLocation.setVerticalAccuracyMeters(accuracy - ((accuracy >= 5) ? (float) ThreadLocalRandom.current().nextDouble(0, 4) : 0));
            gpsLocation.setSpeedAccuracyMetersPerSecond(20.0f);
            gpsLocation.setBearingAccuracyDegrees(10.0f);
        }

        try {
            locationManager.setTestProviderLocation(GPS_PROVIDER, gpsLocation);
        } catch (SecurityException se) {
            Log.d("MockLocProvider", null, se);
        }
    }

    public static void setNetworkProvider(double latitude, double longitude, float accuracy, float bearing, float altitude) {
        networkLocation.setLatitude(latitude);
        networkLocation.setLongitude(longitude);
        networkLocation.setTime(getTime());
        networkLocation.setBearing(bearing);
        networkLocation.setAltitude(altitude);
        networkLocation.setAccuracy(accuracy);
        networkLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            networkLocation.setVerticalAccuracyMeters(100.f);
            networkLocation.setSpeedAccuracyMetersPerSecond(20.f);
            networkLocation.setBearingAccuracyDegrees(10.0f);
        }
        try {
            locationManager.setTestProviderLocation(NETWORK_PROVIDER, networkLocation);
        } catch (SecurityException se) {
            Log.d("MockLocProvider", null, se);
        }
    }

    public static void reportLocation(double latitude, double longitude, float accuracy, float bearing, float speed, float altitude) {
        try {
            gpsLocation.setLatitude(latitude);
            gpsLocation.setLongitude(longitude);
            gpsLocation.setTime(getTime());
            gpsLocation.setAccuracy(accuracy);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                gpsLocation.setVerticalAccuracyMeters(100.0f);
                gpsLocation.setSpeedAccuracyMetersPerSecond(20.0f);
                gpsLocation.setBearingAccuracyDegrees(10.0f);
            }
            gpsLocation.setAltitude(altitude);
            gpsLocation.setBearing(bearing);
            gpsLocation.setSpeed(speed);
            gpsLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        } catch (Exception e) {
            Log.d("MockLocProvider", null, e);
        }
    }

    private static long getTime() {
        return System.currentTimeMillis() - ((long) random.nextInt(InputDeviceCompat.SOURCE_KEYBOARD));
    }

    public static void removeProviders() {
        if (isMockLocationsEnabled) {
            try {
                locationManager.removeTestProvider(GPS_PROVIDER);
                locationManager.removeTestProvider(NETWORK_PROVIDER);
            } catch (IllegalArgumentException | SecurityException e) {
                Log.d("MockLocProvider", null, e);
            }
        }
    }

    public static boolean areLocationPermissionsGranted(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isMockLocationsEnabled(Context context) {
        return Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION, 0) != 0;
    }
}
*/
