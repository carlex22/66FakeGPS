package com.carlex.drive;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import com.google.android.gms.maps.model.LatLng;

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
import android.widget.Toast;


public class xLocationManager {

    private static xLocationManager instance;
    private static LocationManager androidLocationManager;
    private Location currentLocation;
    private Context context;
    private static Context xthis;

    private static Location gpsLocation;      
    private static Location networkLocation;         
    private static final String GPS_PROVIDER = LocationManager.GPS_PROVIDER;   
    private static final String NETWORK_PROVIDER = LocationManager.NETWORK_PROVIDER; 
    private static boolean isMockLocationsEnabled;         
    private static Random random;

    private xLocationManager(Context context) {
        this.context = context;
	xthis = context;
        androidLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        setupLocationListener();
    }

    public static synchronized xLocationManager getInstance(Context context) {
        if (instance == null) {
            instance = new xLocationManager(context.getApplicationContext());
        }
        return instance;
    }

    private void setupLocationListener() {
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                currentLocation = location;
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            @Override
            public void onProviderEnabled(String provider) {}

            @Override
            public void onProviderDisabled(String provider) {}
        };

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            androidLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10, 0, locationListener);

            currentLocation = androidLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }

    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public LatLng getLatLngFromLocation() {
        if (currentLocation != null) {
            return new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        }
        return null;
    }

    public float getSpeed() {
        if (currentLocation != null) {
            return currentLocation.getSpeed();
        }
        return 0f;
    }

    public double getAltitude() {
        if (currentLocation != null) {
            return currentLocation.getAltitude();
        }
        return 0.0;
    }

    public float getBearing() {
        if (currentLocation != null) {
            return currentLocation.getBearing();
        }
        return 0f;
    }


    public double getLatitude() {
        if (currentLocation != null) {
            return currentLocation.getLatitude();
        }
        return 0.0; // Ou outro valor padrão adequado
    }

    public double getLongitude() {
        if (currentLocation != null) {
            return currentLocation.getLongitude();
        }
        return 0.0; // Ou outro valor padrão adequado
    }

    public Location getLastKnownLocation() {
	    return currentLocation;
    }



	public void requestLocationUpdates(String provider, long minTime, float minDistance, LocationListener listener) {
		if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
			androidLocationManager.requestLocationUpdates(provider, minTime, minDistance, listener);
		}
	}	

	public static void initTestProvider(Context context) {
	xthis = context;
        gpsLocation = new Location(GPS_PROVIDER);
        networkLocation = new Location(NETWORK_PROVIDER);
        isMockLocationsEnabled = areLocationPermissionsGranted(context) && isMockLocationsEnabled(context);
        random = new Random();
	//isMockLocationsEnabled=true;
        if (isMockLocationsEnabled) {
	    removeProviders();

	    androidLocationManager.addTestProvider(GPS_PROVIDER,false, false, false, false, true, true, true, ProviderProperties.POWER_USAGE_LOW, ProviderProperties.ACCURACY_FINE);

	    androidLocationManager.setTestProviderEnabled(GPS_PROVIDER, true);
	    androidLocationManager.addTestProvider(NETWORK_PROVIDER,false, false, false, false, true, true, true, ProviderProperties.POWER_USAGE_LOW, ProviderProperties.ACCURACY_COARSE);

	    androidLocationManager.setTestProviderEnabled(NETWORK_PROVIDER, true);
            }
        }
		


    public static void setGpsProvider(Location gpsLocation) {
        /*gpsLocation.setLatitude(latitude);
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
        }*/


        try {
            androidLocationManager.setTestProviderLocation(GPS_PROVIDER, gpsLocation);
        } catch (SecurityException se) {
            Log.d("MockLocProvider", null, se);
        }
     }

    public static void setNetworkProvider(Location networkLocation) {
        /*networkLocation.setLatitude(latitude);
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
        },*/
	    
	Log.d("MockLocProvider", networkLocation.toString());

        try {
            androidLocationManager.setTestProviderLocation(NETWORK_PROVIDER, networkLocation);
        } catch (SecurityException se) {
            Log.d("MockLocProvider", null, se);
        }
    }



    public static void reportLocation(Location gpsLocation) {
        /*try {
            /*gpsLocation.setLatitude(latitude);
            gpsLocation.setLongitude(longitude);
            gpsLocation.setTime(getTime());
            gpsLocation.setAccuracy(accuracy);
	    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                gpsLocation.setVerticalAccuracyMeters(100.0f);
                gpsLocation.setSpeedAccuracy
		MetersPerSecond(20.0f);
                gpsLocation.setBearingAccuracyDegrees(10.0f);
            }
            gpsLocation.setAltitude(altitude);
            gpsLocation.setBearing(bearing);
            gpsLocation.setSpeed(speed);
            gpsLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        } catch (Exception e) {
	    Toast.makeText(xthis, "FALHA AO REPORT FAKE", Toast.LENGTH_SHORT).show();
            Log.d("MockLocProvider", null, e);
        }*/
    	}

    	private static long getTime() {
        return System.currentTimeMillis() - ((long) random.nextInt(InputDeviceCompat.SOURCE_KEYBOARD));
    	}

    	public static void removeProviders() {
        if (isMockLocationsEnabled) {
            try {
                androidLocationManager.removeTestProvider(GPS_PROVIDER);
                androidLocationManager.removeTestProvider(NETWORK_PROVIDER);
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


