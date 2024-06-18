package com.carlex.drive;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;
import android.location.provider.ProviderProperties;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class FakeLocationService1 extends Service {

    public static double latitude, longitude, altitude;
    public static float bearing;
    public static double velocidade;
    public static Location gpsLocation;
    public static Location networkLocation;
    public static boolean isRunning = false;
    public static boolean parado = true;
    public static boolean isMockLocationsEnabled;
    
    private Thread backgroundThread;
    public static LocationManager locationManager;
    private FusedLocationProviderClient fusedLocation;
    public static SpaceMan spaceMan;
    private Context context;
  
    public FusedLocationsProvider fusedLocationsProvider;
  
    private static final String TAG = "FLS";
    private static final String CHANNEL_ID = "FakeLocationServiceChannel";
    private static final String GPS_PROVIDER = LocationManager.GPS_PROVIDER;
    private static final String NETWORK_PROVIDER = LocationManager.NETWORK_PROVIDER;
    private static final String FUSED_PROVIDER = LocationManager.FUSED_PROVIDER;

   private static final Object lock = new Object();

    
   static void setLatitude(double newLatitude) {
        synchronized (lock) {
            latitude = newLatitude;
        }
    }

    // Getter e Setter sincronizados para longitude
    public static synchronized double getLongitude() {
        return longitude;
    }

    public static synchronized double getLatitude() {                       
         return latitude;
    }

    public static void setLongitude(double newLongitude) {
        synchronized (lock) {
            longitude = newLongitude;
        }
    }

    // Getter e Setter sincronizados para velocidade
    public static synchronized double getSpeed() {
        return velocidade;
    }

    public static void setSpeed(double newVelocidade) {
        synchronized (lock) {
            velocidade = newVelocidade;
        }
    }

    // Getter e Setter sincronizados para altitude
    public static synchronized double getAltitude() {
        return altitude;
    }

    public static void setAltitude(double newAltitude) {
        synchronized (lock) {
            altitude = newAltitude;
        }
    }

    // Getter e Setter sincronizados para bearing
    public static synchronized float getBearing() {
        return bearing;
    }

    public static void setBearing(float newBearing) {
        synchronized (lock) {
            bearing = newBearing;
	    }    
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

       if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            gpsLocation = locationManager.getLastKnownLocation(GPS_PROVIDER);
            networkLocation = locationManager.getLastKnownLocation(NETWORK_PROVIDER);
        }
        
        boolean gpsEnabled = locationManager.isProviderEnabled(GPS_PROVIDER);
        //isMockLocationsEnabled = true;

        if (areLocationPermissionsGranted(context)) {
            if (gpsEnabled) {
                Toast.makeText(context, "GPS disponível", Toast.LENGTH_SHORT).show();
                Location gpsLocation = locationManager.getLastKnownLocation(GPS_PROVIDER);
                if (gpsLocation != null) {
                    latitude = gpsLocation.getLatitude();
                    longitude = gpsLocation.getLongitude();
                    altitude = gpsLocation.getAltitude();
                    bearing = gpsLocation.getBearing();
                }
                isMockLocationsEnabled = true;
            }
        } else {
            setDefaultLocation();
            isMockLocationsEnabled = true;
        }
        

        //fusedLocation = LocationServices.getFusedLocationProviderClient(this);
        //fusedLocationsProvider = new FusedLocationsProvider(context);
      
        createNotificationChannel();

        if (isMockLocationsEnabled) {
            Toast.makeText(context, "Permissão de localização falsa concedida", Toast.LENGTH_SHORT).show();
            startForeground(1, getNotification("66 Fake Gps Ligado").build());
            initTestProvider();
        } else {
            startForeground(1, getNotification("Falha ao iniciar 66 Fake").build());
            Toast.makeText(context, "Permissão de localização falsa não concedida", Toast.LENGTH_SHORT).show();
            return;
        }

        String tles = readRawTextFile(R.raw.gps);
        spaceMan = new SpaceMan(context, tles, new Location(GPS_PROVIDER));
    }
    
    
   public static boolean isServiceRunning() {
        return isRunning;
    }

    

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        backgroundThread = new Thread(() -> {
            while (true) {
                RotaFake rotaFake1 = MyApp.getDatabase().rotaFakeDao().getRotaFakeWithMinTime(System.currentTimeMillis());
                spoofLocationAndUpdate(rotaFake1);
                handleVehicleState(rotaFake1);
                MyApp.getDatabase().rotaFakeDao().deleteRotaFakeWithTimeGreaterThan(System.currentTimeMillis());
            }
        });
        backgroundThread.start();
        return START_NOT_STICKY;
    }

    private void handleVehicleState(RotaFake rotaFake1) {
        if (getGpsSpeed() > 0.5 && parado) {
            notifyState("66 em Rota");
            parado = false;
        } else if (getGpsSpeed() <= 0.5 && !parado) {
            notifyState("66 Estacionado");
            parado = true;
        }
        if (rotaFake1 == null) {
            sleepRandomTime();
        } else {
            waitForNextUpdate(rotaFake1);
        }
    }

    private void spoofLocationAndUpdate(RotaFake rotaFake1) {
        
        if (latitude < -10.0 && isMockLocationsEnabled) {
            spoofLocation(rotaFake1, new Location(GPS_PROVIDER), GPS_PROVIDER);
            spoofLocation(rotaFake1, new Location(NETWORK_PROVIDER), NETWORK_PROVIDER);
            if (gpsLocation!=null){
              //  fusedLocationsProvider.spoof(gpsLocation);
                updateSpaceMan(gpsLocation);
                }
        }
        MyApp.getDatabase().rotaFakeDao().deleteRotaFakeWithTimeGreaterThan(System.currentTimeMillis());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(this).notify(2, getNotification("Fake Location Service Stopped").build());
        }
        removeProviders();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void notifyState(String message) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(this).notify(2, getNotification(message).build());
        }
    }

    private void sleepRandomTime() {
        float tnoise = (float) (ThreadLocalRandom.current().nextDouble(100, 150));
        try {
            Thread.sleep((long) tnoise);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void waitForNextUpdate(RotaFake rotaFake1) {
        long tempo = rotaFake1.getTempo();
        long diferencaTempo = tempo - System.currentTimeMillis();
        if (diferencaTempo > 0) {
            try {
                Thread.sleep(diferencaTempo);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void setDefaultLocation() {
        Toast.makeText(context, "Localização inicial definida", Toast.LENGTH_SHORT).show();
        latitude = -23.5879554;
        longitude = -46.63816059;
        altitude = 750.0;
        bearing = 45f;
    }

    public void spoofLocation(RotaFake rotaFake1, Location location, String provider) {
        if (location==null){
            location  = new Location(provider);
        } 
    
        if (rotaFake1!=null){
            latitude = rotaFake1.getLatitude();
            longitude = rotaFake1.getLongitude();
            bearing = rotaFake1.getBearing();
            velocidade = rotaFake1.getVelocidade();
        }

        location.setLatitude(latitude);
        location.setLongitude(longitude);
        float noise = (float) (ThreadLocalRandom.current().nextDouble(0, 20) / 10);
        long Timef = System.currentTimeMillis();
        location.setBearing(bearing + (noise / 2));
        location.setSpeed((float) ((velocidade + (noise / 3.6f)) / 4));
        location.setTime(Timef);
        location.setAltitude((double) ((700 + Math.random() * 50) + noise));
        location.setAccuracy((float) (ThreadLocalRandom.current().nextDouble(0, 20) / 10));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            location.setVerticalAccuracyMeters((float) (ThreadLocalRandom.current().nextDouble(0, 20) / 10));
            location.setSpeedAccuracyMetersPerSecond(noise / 3.6f);
            location.setBearingAccuracyDegrees(noise / 2);
        }
        location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());

        if (isMockLocationsEnabled) {
            try {
                    locationManager.setTestProviderLocation(provider, location);
            } catch (SecurityException se) {
                Log.d(TAG, "Falha no Mock", se);
            }
        }
    }

    private static void updateSpaceMan(Location location) {
        SpaceMan.setGroundStationPosition(location);
        //SpaceMan.calculatePositions();
        Log.d(TAG, "Max C/N0: " + SpaceMan.getMaxCn0());
        Log.d(TAG, "Mean C/N0: " + SpaceMan.getMeanCn0());
        Log.d(TAG, "Satellite Count: " + SpaceMan.getSatelliteCount());
    }

    private float getGpsSpeed() {
        return new Location(GPS_PROVIDER).getSpeed();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Fake Location Service Channel";
            String description = "Channel for Fake Location Service";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private NotificationCompat.Builder getNotification(String message) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Fake Location Service")
                .setContentText(message)
                .setSmallIcon(R.drawable.ico)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
    }

    private String readRawTextFile(int resId) {
        InputStream inputStream = getResources().openRawResource(resId);
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    
   public static void initTestProvider() {
	//isMockLocationsEnabled = areLocationP0ermissionsGranted(this) && isMockLocationsEnabled(this.context);

	if (isMockLocationsEnabled) {
	    removeProviders();
	    locationManager.addTestProvider(GPS_PROVIDER,false, false, false, false, true, true, true, ProviderProperties.POWER_USAGE_LOW, ProviderProperties.ACCURACY_FINE);
	    locationManager.setTestProviderEnabled(GPS_PROVIDER, true);
	    locationManager.addTestProvider(NETWORK_PROVIDER,false, false, false, false, true, true, true, ProviderProperties.POWER_USAGE_LOW, ProviderProperties.ACCURACY_COARSE);
	    locationManager.setTestProviderEnabled(NETWORK_PROVIDER, true);
	}
    }

    
    
    public static boolean areLocationPermissionsGranted(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    

    public static void removeProviders() {
	     if (isMockLocationsEnabled) {
		     try {
			     locationManager.removeTestProvider(GPS_PROVIDER);
			     locationManager.removeTestProvider(NETWORK_PROVIDER);
		     } catch (IllegalArgumentException | SecurityException e) {
			     Log.d("Fakelocatinservice", "erro remover provedores", e);
		     }                                                             }
     }
    
    

}
