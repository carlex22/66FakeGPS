package com.carlex.drive;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.os.RemoteException;
import android.os.RemoteException;
import java.io.File;
import android.location.Location;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.SharedPreferences;

import android.os.Binder;
import android.os.IBinder;
import android.os.Looper;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.SystemClock;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Process;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.location.provider.ProviderProperties;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;
import android.content.SharedPreferences;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ThreadLocalRandom;

//import android.preference.PreferenceManager;
//import com.carlex.drive.SystemPreferencesHandler;



import java.lang.reflect.Field;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

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
import android.os.Handler;
import kotlin.contracts.Returns;
public class FakeLocationService1 extends Service {

    public static double latitude =  -100.0, longitude = -10.0, altitude = 10.0;
    public static float bearing= 5f;
    public static double velocidade = 0f;
    public static Location gpsLocation;
    public static Location networkLocation;
    public static boolean isRunning = false;
    public static boolean parado = true;
   private static Queue<Float> speedHistory = new LinkedList<>();
    private static Queue<Float> bearingHistory = new LinkedList<>();
 
    public static boolean isMockLocationsEnabled;
    
    private static Thread backgroundThread;
    public static LocationManager locationManager;
    private static  FusedLocationProviderClient fusedLocation;
    public static SpaceMan spaceMan;
    private static Context context;
 private static final String PREFS_NAME = "LocationPreferences";

    public static Location ffLocation;
    
    private static SystemPreferencesHandler systemPreferencesHandler;
    
    private static final int REQUEST_LOCATION_PERMISSION = 1;

    public static FusedLocationsProvider fusedLocationsProvider;
  
    private static final String TAG = "FLS";
    private static final String CHANNEL_ID = "FakeLocationServiceChannel";
    private static final String GPS_PROVIDER = LocationManager.GPS_PROVIDER;
    private static final String NETWORK_PROVIDER = LocationManager.NETWORK_PROVIDER;
    private static final String FUSED_PROVIDER = LocationManager.FUSED_PROVIDER;

   
    
    
    private static void saveLocationPreferences(Context context, double latitude, double longitude, float bearing, float speed, double altitude) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("latitude", String.valueOf(latitude));
        editor.putString("longitude", String.valueOf(longitude));
        editor.putString("bearing", String.valueOf(bearing));
        editor.putString("speed", String.valueOf(speed));
        editor.putString("altitude", String.valueOf(altitude));
        editor.apply();
    }
    
    
   public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        isRunning= false;
        Log.i(TAG, "service created");
        //startForeground(148, getNotification("Fake Gps Ligado").build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (isRunning) {
           Log.e(TAG, "Fake 66 service is runnig");
            return START_STICKY;
        }
        
        
        Log.i(TAG, "Fake 66 service start");

        isRunning = true;
        
        if (intent != null && intent.getExtras() != null) {
            String packageName = intent.getExtras().getString("appContext");
            if (packageName != null) {
                try {
                    context = createPackageContext(packageName, 0);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    
        if (context == null) {
            context = getApplicationContext();
        }
    
        startForeground(1, getNotification("Fake Gps Ligado").build());
    
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            gpsLocation = locationManager.getLastKnownLocation(GPS_PROVIDER);
            networkLocation = locationManager.getLastKnownLocation(NETWORK_PROVIDER);
        }
    
        if (ContextCompat.checkSelfPermission(context, "android.permission.ACCESS_MOCK_LOCATION") == PackageManager.PERMISSION_GRANTED) {
            isMockLocationsEnabled = true;
        }
    
        if (networkLocation == null) {
            networkLocation = gpsLocation;
        }
    
        ffLocation = new Location(FUSED_PROVIDER);
        boolean gpsEnabled = locationManager.isProviderEnabled(GPS_PROVIDER);
    
        if (gpsEnabled) {
           // Toast.makeText(context, "GPS disponível", Toast.LENGTH_SHORT).show();
            Location gpsLocation = locationManager.getLastKnownLocation(GPS_PROVIDER);
            if (gpsLocation != null) {
                latitude = gpsLocation.getLatitude();
                longitude = gpsLocation.getLongitude();
                altitude = gpsLocation.getAltitude();
                bearing = gpsLocation.getBearing();
            } else {
                setDefaultLocation();
            }
        } else {
            setDefaultLocation();
        }
            
            
       // fusedLocation = LocationServices.getFusedLocationProviderClient(context);
       
        if (gpsLocation==null){
                gpsLocation = new Location(GPS_PROVIDER);
                }
            
        fusedLocationsProvider = new FusedLocationsProvider(context, gpsLocation);
    
        createNotificationChannel();
        
    
        String tles = readRawTextFile(R.raw.gps);
    
        Intent serviceIntent = new Intent(context, SpaceManService.class);
        serviceIntent.putExtra("location", gpsLocation);
        serviceIntent.putExtra("tles", tles);
        serviceIntent.putExtra("callingPackage", "com.carlex.drive.FakeLocationService1");
    
        startService(serviceIntent);
        
        Intent serviceIntentSe = new Intent(this, SensorProcessorService.class);
        startService(serviceIntentSe);
  
    
       // spaceMan = new SpaceMan(context, tles, new Location(GPS_PROVIDER));
    
        if (isMockLocationsEnabled) {
            Toast.makeText(context, "Permissão de localização falsa concedida", Toast.LENGTH_SHORT).show();
            if (backgroundThread == null || !backgroundThread.isAlive()) {
                initTestProvider();
                startBackgroundTask();
            }
        } else {
            Toast.makeText(context, "Permissão de localização falsa não concedida", Toast.LENGTH_SHORT).show();
        }
    
            
        
        return START_STICKY;
    }

    public static void setIsRunnig(Boolean set){
        isRunning = set;
    }
    
    public static boolean isServiceRunning() {
        return isRunning;
    }

    private static void startBackgroundTask() {
        backgroundThread = new Thread(() -> {
            while (true) {
                RotaFake rotaFake1 = MyApp.getDatabase().rotaFakeDao().getRotaFakeWithMinTime(System.currentTimeMillis());
                handleVehicleState(rotaFake1);
                spoofLocationAndUpdate(rotaFake1);
                MyApp.getDatabase().rotaFakeDao().deleteRotaFakeWithTimeGreaterThan(System.currentTimeMillis());
            }
        });
        backgroundThread.start();
    }

    private static void handleVehicleState(RotaFake rotaFake1) {
        if (rotaFake1 != null) {
            //Verificar se o veículo está parado
		    if (gpsLocation.getSpeed()>0.5 && parado == true){ if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) { NotificationManagerCompat.from(context).notify(2, getNotification("66 em Rota").build()); }              
			    parado = false;           
                Log.i(TAG, "Fake 66 Rota created");
		    } 
		    if (gpsLocation.getSpeed()<=0.5 && parado == false){ if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) { NotificationManagerCompat.from(context).notify(2, getNotification("66 Estacionado").build()); }            
			    parado = true;    
               Log.i(TAG, "Fake 66 Rota stop");
		    }
		    //esperar tempo para proxima atualizacao
            long tempo = rotaFake1.getTempo();
            long diferencaTempo = tempo - System.currentTimeMillis();
		    if (diferencaTempo > 0) {
			    try {Thread.sleep(diferencaTempo);} 
			    catch (InterruptedException e) {}
            }
        } else {
		    float tnoise = (float) (ThreadLocalRandom.current().nextDouble(100, 150));
		    if (latitude < -10.0){ 
			    RotaFake rotaFakeEntry = new RotaFake( latitude, longitude, bearing, 0.0, (long) (System.currentTimeMillis() + 159));
		        MyApp.getDatabase().rotaFakeDao().insert(rotaFakeEntry);
			    rotaFake1 = MyApp.getDatabase().rotaFakeDao().getRotaFakeWithMinTime(System.currentTimeMillis());
		}
		    try { Thread.sleep((long) tnoise); }   
		    catch (InterruptedException e) {}        
		}                          
    }

    private static  void spoofLocationAndUpdate(RotaFake rotaFake1) {
        if (latitude < -1.0) {
              spoofLocation(rotaFake1, gpsLocation, GPS_PROVIDER);
              spoofLocation(rotaFake1, networkLocation, NETWORK_PROVIDER);
              spoofLocation(rotaFake1, ffLocation, FUSED_PROVIDER);
             // updateSpaceMan(gpsLocation);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(context).notify(2, getNotification("Fake Location Service Stopped").build());
        }
        try {
            MyApp.getDatabase().rotaFakeDao().deleteAllExceptFirstFour();
        } catch (Exception e) {
            e.printStackTrace();
        }
        removeProviders();
        if (backgroundThread != null) {
            backgroundThread.interrupt();
        }
        Log.i(TAG, "Fake 66 service S");
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private static void notifyState(String message) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(context).notify(2, getNotification(message).build());
        }
    }

    private static void sleepRandomTime() {
        float tnoise = (float) (ThreadLocalRandom.current().nextDouble(100, 150));
        try {
            Thread.sleep((long) tnoise);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void waitForNextUpdate(RotaFake rotaFake1) {
        long tempo = rotaFake1.getTempo();
        long diferencaTempo = tempo - System.currentTimeMillis();
        if (diferencaTempo > 0) {
            try {
                Thread.sleep(diferencaTempo);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } else {
             float tnoise = (float) (ThreadLocalRandom.current().nextDouble(100, 150));
              if (latitude < -.0){ 
                velocidade = 0.0;
			    RotaFake rotaFakeEntry = new RotaFake( latitude, longitude, bearing, 0.0, (long) (System.currentTimeMillis() + 159));
                MyApp.getDatabase().rotaFakeDao().insert(rotaFakeEntry);
			    rotaFake1 = MyApp.getDatabase().rotaFakeDao().getRotaFakeWithMinTime(System.currentTimeMillis());
              }
        }
        
    }

    private static void setDefaultLocation() {
        Toast.makeText(context, "Localização inicial definida", Toast.LENGTH_SHORT).show();
        latitude = -23.5879554;
        longitude = -46.63816059;
        altitude = 750.0;
        bearing = 45f;
    }
    
    
    private static float processSensorData(Queue<Float> history, float newValue) {
        // Add new values to the history queue
        if (history.size() >= 10) {
            history.poll(); // Remove the oldest entry if the queue is full
        }
        history.add(newValue);

        // Calculate the average of the historical values
        float smoothedValue = 0;
        for (float value : history) {
            smoothedValue += value;
        }
        smoothedValue /= history.size();
        smoothedValue = round(smoothedValue, 3); // Arredondar para 3 casas decimais

        // Calculate the threshold
        float threshold = smoothedValue * 1.5f;

        // Ignore outliers
        if (Math.abs(newValue - smoothedValue) > threshold) {
            return smoothedValue; // Return the smoothed value if the new value is an outlier
        }

        return round(newValue, 3); // Return the new value if it is not an outlier, arredondado para 3 casas decimais
    }
    
    
    private static float round(float value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.floatValue();
    }

    private static void spoofLocation(RotaFake rotaFake1, Location location, String provider) {
        if (isRunning){
           if (location==null){
              return;
           } 
            
            
        float smoothedBearing = 0f;
        float smoothedSpeed= 0f;
        float smoothedAltitude= 0f;
        float noise3 = (float) (ThreadLocalRandom.current().nextDouble(0, 15) );
   
    
        if (rotaFake1!=null){
            latitude = rotaFake1.getLatitude();
            longitude = rotaFake1.getLongitude();
            bearing = rotaFake1.getBearing();
            velocidade = rotaFake1.getVelocidade();
            smoothedAltitude = 750+noise3;
            smoothedBearing = processSensorData(bearingHistory, bearing);
            smoothedSpeed = processSensorData(speedHistory, (float) velocidade);
            saveLocationPreferences(context, latitude, longitude, smoothedBearing, smoothedSpeed, smoothedAltitude);
        }
            
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        float noise = (float) (ThreadLocalRandom.current().nextDouble(0, 20) / 100);
        float noise1 = (float) (ThreadLocalRandom.current().nextDouble(0, 20) / 10000);
        float noise2 = (float) (ThreadLocalRandom.current().nextDouble(0, 20) / 10000);

        long Timef = System.currentTimeMillis();
        location.setBearing(round(smoothedBearing + (noise / 5),3));
        location.setSpeed(round ((smoothedSpeed+noise) / 5f,3));
        location.setTime(Timef+((long)(noise*17)));
        location.setAltitude((double)round((smoothedAltitude),3));
        location.setAccuracy(((int)noise3/5)+1);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            location.setVerticalAccuracyMeters(0.5f);
            location.setSpeedAccuracyMetersPerSecond(0.5f);
            location.setBearingAccuracyDegrees((ThreadLocalRandom.current().nextInt(1, 2) ));
        }
        location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos()+((long)noise3));
            try {
                Bundle extras = new Bundle();                          
                extras.putInt("satellites", 
                SpaceManService.getSatelliteCount());
                extras.putFloat("maxCn0", 
                round(SpaceManService.getMaxCn0(),3)); 
                extras.putFloat("meanCn0", 
                round(SpaceManService.getMeanCn0(),3));      
                location.setExtras(extras);
                //Log.d(TAG, "max" + SpaceManService.getMaxCn0());
                 if (provider == FUSED_PROVIDER){
                    fusedLocationsProvider.spoof(location);
                 } else {
                    locationManager.setTestProviderLocation(provider, location);
                 }
            } catch (SecurityException se) {
                Log.d(TAG, "Falha no Mock", se);
            }
            
            
        }
    }

    private static void updateSpaceMan(Location location) {
        SpaceMan.setGroundStationPosition(location);
        //Log.d(TAG, "Max C/N0: " + SpaceMan.getMaxCn0());
        //Log.d(TAG, "Mean C/N0: " + SpaceMan.getMeanCn0());
        //Log.d(TAG, "Satellite Count: " + SpaceMan.getSatelliteCount());
    }

    public static  float getGpsSpeed() {
        return new Location(GPS_PROVIDER).getSpeed();
    }

    private  void createNotificationChannel() {
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

    private static NotificationCompat.Builder getNotification(String message) {
        return new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("Fake Location Service")
                .setContentText(message)
                .setSmallIcon(R.drawable.ico)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
    }

    private  String readRawTextFile(int resId) {
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
	    if (isMockLocationsEnabled || isRunning) {
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
		    }                                                    
         }
     }
    

}
