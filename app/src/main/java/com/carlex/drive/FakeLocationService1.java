package com.carlex.drive;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.os.RemoteException;
import android.os.RemoteException;
import android.widget.Space;
import com.google.android.gms.location.LocationAvailability;
import java.io.File;
import android.location.Location;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.SharedPreferences;
import java.util.Locale;
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

    public static double latitude =  -100.0, longitude = -10.0, altitude = 750.0;
    public static float bearing= 5f;
    public static double velocidade = 0f;
    public static Location gpsLocation;
    public static Location networkLocation;
    public static boolean isRunning = false;
    public static boolean parado = true;
   private static Queue<Float> speedHistory = new LinkedList<>();
    private static Queue<Float> bearingHistory = new LinkedList<>();
 
    public static boolean isMockLocationsEnabled;
    private static Runnable runnable;
    private static SensorProcessor sensorProcessor;
    
    private static Thread backgroundThread;
    public static LocationManager locationManager;
    private static  FusedLocationProviderClient fusedLocation;
    public static SpaceMan spaceMan;
    private static Context context;
 private static final String PREFS_NAME = "LocationPreferences";

    public static Location ffLocation;
    
    private static ElevationService elevationService;
    private static Handler handler;
    
    private static SystemPreferencesHandler systemPreferencesHandler;
    
    private static final int REQUEST_LOCATION_PERMISSION = 1;

    public static FusedLocationsProvider fusedLocationsProvider;
  
    private static final String TAG = "FLS";
    private static final String CHANNEL_ID = "FakeLocationServiceChannel";
    private static final String GPS_PROVIDER = LocationManager.GPS_PROVIDER;
    private static final String NETWORK_PROVIDER = LocationManager.NETWORK_PROVIDER;
    private static final String FUSED_PROVIDER = LocationManager.FUSED_PROVIDER;

   
    
    
    private static boolean saveLocationPreferences(Context context, double latitude, double longitude, float bearing, float speed, double altitude) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("latitude", String.valueOf(latitude));
        editor.putString("longitude", String.valueOf(longitude));
        editor.putString("bearing", String.valueOf(bearing));
        editor.putString("speed", String.valueOf(speed));
        editor.putString("altitude", String.valueOf(altitude));
        editor.apply();
        return true;
    }
    
    
   public void onCreate() {
        super.onCreate();
        createNotificationChannel();
       Locale.setDefault(Locale.US);
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
        
        sensorProcessor = new SensorProcessor();
        
  
        initTestProvider();
        
        elevationService = new ElevationService();
        
    
       // spaceMan = new SpaceMan(context, tles, new Location(GPS_PROVIDER));
    
        if (isMockLocationsEnabled) {
            Toast.makeText(context, "Permissão de localização falsa concedida", Toast.LENGTH_SHORT).show();
            if (backgroundThread == null || !backgroundThread.isAlive()) {
                
                boolean rr = startBackgroundTask();
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

    public static boolean startBackgroundTask() {
        backgroundThread = new Thread(() -> {
            while (true) {
                RotaFake rotaFake1 = MyApp.getDatabase().rotaFakeDao().getRotaFakeWithMinTime(System.currentTimeMillis());
                boolean pp = postlocation(rotaFake1);
                boolean ss = handleVehicleState(rotaFake1);    
                boolean mmo = spoofLocationAndUpdate(rotaFake1);
                if(gpsLocation != null) SpaceManService.settLoc(gpsLocation);
                MyApp.getDatabase().rotaFakeDao().deleteRotaFakeWithTimeGreaterThan(System.currentTimeMillis());
            }
        });
        backgroundThread.start();
        return true;
    }

    public static Long uTempo = 0L;
    
    private static boolean handleVehicleState(RotaFake rotaFake1) {
        if (rotaFake1 != null) {
            //Verificar se o veículo está parado
            if (gpsLocation == null){
               return false;
             }
       
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
                if (gpsLocation!=null) {
                        SensorProcessor.setAltitude(gpsLocation.getAltitude());
                        SensorProcessor.setLatitude(rotaFake1.getLatitude());
                        SensorProcessor.setLongitude(rotaFake1.getLongitude());
                        SensorProcessor.setBearing(rotaFake1.getBearing());
                        SensorProcessor.setTempo(System.currentTimeMillis()-uTempo+diferencaTempo); 
                        boolean runs = SensorProcessor.starTask(); 
                        Log.i(TAG, "proceed sensor:"+runs);
                }
			    try {Thread.sleep(diferencaTempo);} 
			    catch (InterruptedException e) {}
                uTempo = System.currentTimeMillis();
            
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
        return true;          
    }

    private static boolean  spoofLocationAndUpdate(RotaFake rotaFake1) {
        if (latitude < -1.0) {
              gpsLocation = spoofLocation(rotaFake1, gpsLocation, GPS_PROVIDER);
              networkLocation = spoofLocation(rotaFake1, networkLocation, NETWORK_PROVIDER);
              ffLocation = spoofLocation(rotaFake1, ffLocation, FUSED_PROVIDER);
             // updateSpaceMan(gpsLocation);
        }
        return true;
    }
    
    private static boolean postlocation(RotaFake rotaFake1){
        if (rotaFake1!=null){
            if(gpsLocation != null) {
                    boolean sp = SpaceManService.setLastLoc(gpsLocation);
                    boolean sav = SpaceManService.saveLocationToPreferences(
                    gpsLocation, 
                    rotaFake1.getLatitude(),
                    rotaFake1.getLongitude(),
                    rotaFake1.getBearing(),
                    rotaFake1.getVelocidade(),
                    750.0,
                    System.currentTimeMillis());
                                /*  SensorProcessorService.setculo(
                    rotaFake1.getLatitude(),
                    rotaFake1.getLongitude(),
                    rotaFake1.getBearing(),
                    rotaFake1.getVelocidade(),
                    750.0,
                    System.currentTimeMillis());*/
                }
            }
        return true;
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
        handler.removeCallbacks(runnable);
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
    
    public static double getaltitude() {
        elevationService.obterAltitude(latitude, longitude, new ElevationService.ElevationCallback() {
                    public void onElevationReceived(double altitude) {   
                        FakeLocationService1.setAltitude(altitude);
                        Log.d(TAG, "A altitude da coordenada (" + latitude + ", " + longitude + ") é: " + altitude + " metros");
                    }
                    public void onError(Exception e) {
                        Log.e(TAG, "Erro ao obter a altitude", e);
                    }
                }); 
        return altitude;
    }
    
    
    private static void setAltitude(double alt){
        altitude = alt;
    }

    private static  boolean waitForNextUpdate(RotaFake rotaFake1) {
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
        return true;
    }

    private static void setDefaultLocation() {
        Toast.makeText(context, "Localização inicial definida", Toast.LENGTH_SHORT).show();
        latitude = -23.5879554;
        longitude = -46.63816059;
        altitude = 750.0;
        bearing = 45f;
    }
    
    
    private static double adicionarRuido(double valorOriginal, double nivelRuido) {
        Random random = new Random();
        double ruido = (random.nextDouble() * 2 - 1) * nivelRuido;
        return valorOriginal + ruido;
    }
    
    
    private static final double LAT_LNG_NOISE_LEVEL = 0.000001; // Ajuste o nível de ruído conforme necessário
    private static final double BEARING_NOISE_LEVEL = 1.0; // Ajuste o nível de ruído conforme necessário
    private static final double VELOCIDADE_NOISE_LEVEL = 0.1; // Ajuste o nível de ruído conforme necessário

    private static long lastSaveTime = 0;
    
   public static boolean checkSave() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSaveTime < 5000) {
            return false;
        }
        lastSaveTime = currentTime;
        return true;
    }
    
    
    

    private static Location spoofLocation(RotaFake rotaFake1, Location location, String provider) {
       
        if (isRunning){
           if (location==null){
              return null;
           } 
            
          
            
        
        float noise3 = (float) (ThreadLocalRandom.current().nextDouble(0, 1) );
   
        if (rotaFake1 != null) {
            latitude = adicionarRuido(rotaFake1.getLatitude(), LAT_LNG_NOISE_LEVEL);
            longitude = adicionarRuido(rotaFake1.getLongitude(), LAT_LNG_NOISE_LEVEL);
            bearing = ((float) adicionarRuido(rotaFake1.getBearing(), BEARING_NOISE_LEVEL));
            velocidade = Math.abs(adicionarRuido(rotaFake1.getVelocidade(), VELOCIDADE_NOISE_LEVEL));
            if (checkSave() && velocidade > 1){
                altitude = getaltitude();
            } else {
                altitude = adicionarRuido(altitude,1);
            }
                
        }
            
        
            
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        
        long Timef = (long) adicionarRuido((double)System.currentTimeMillis(),100);
           
        location.setBearing(bearing );
        location.setSpeed((float) (velocidade/3.6f) );
        location.setTime(Timef);
        location.setAltitude(altitude);
        location.setAccuracy(((int)noise3/5)+1);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            location.setVerticalAccuracyMeters(Math.abs((float) (noise3+0.1)));
            location.setSpeedAccuracyMetersPerSecond(Math.abs((float) (noise3+0.1)/2));
            location.setBearingAccuracyDegrees(Math.abs(ThreadLocalRandom.current().nextInt(1, 3) ));
        }
        location.setElapsedRealtimeNanos((long) adicionarRuido((double) SystemClock.elapsedRealtimeNanos(),100));
            try {
                Bundle extras = new Bundle();                          
                extras.putInt("satellites", 
                SpaceManService.getSatelliteCount());
                extras.putFloat("maxCn0", 
                (SpaceManService.getMaxCn0())); 
                extras.putFloat("meanCn0", 
                (SpaceManService.getMeanCn0()));      
                location.setExtras(extras);
                //Log.d(TAG, "max" + SpaceManService.getMaxCn0());
                 if (provider == FUSED_PROVIDER){
                    fusedLocationsProvider.spoof(location);
                 } else {
                    locationManager.setTestProviderLocation(provider, location);
                 }
            } catch (SecurityException se) {
                Log.d(TAG, "Falha no Mock", se);
                return null;
            }
            
            if (provider == GPS_PROVIDER) {
                saveLocationPreferences(context, latitude, longitude, bearing, (float)velocidade, altitude);
                
            }
           
            
            
            return location;
        }
        return null;
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
