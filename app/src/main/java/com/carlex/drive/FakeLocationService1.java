package com.carlex.drive;


import android.database.Cursor;

import android.net.Uri;
import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.os.RemoteException;
import android.os.RemoteException;
import android.widget.Space;

import java.time.Year;
import android.app.AppOpsManager;
import com.google.android.gms.location.LocationAvailability;
import java.io.File;
import org.json.JSONArray;
import org.json.JSONObject;
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
import java.util.List;
import java.io.IOException;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.time.Instant;
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
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.carlex.drive.fLocation;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;


//import android.preference.PreferenceManager;
//import com.carlex.drive.SystemPreferencesHandler;
import java.math.BigDecimal;
import java.math.RoundingMode;



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
import android.content.ContentValues;

public class FakeLocationService1 extends Service {

    
    
    public static double latitude =  -23.5879554,  longitude = -46.6381605, altitude = 750.0;
    public static float bearing= 5f;
    public static double velocidade = 0f;
    public static Location gpsLocation;
    public static Location networkLocation;
    public static boolean isRunning = false;
    public static boolean parado = true;
    private Queue<Double> speedHistory = new LinkedList<>();
    private Queue<fLocation> locationHistory = new LinkedList<>();
    private Queue<Float> bearingHistory = new LinkedList<>();
    private Queue<Double> altitudeHistory = new LinkedList<>();
    private Queue<Long> timeHistory = new LinkedList<>();
    private Queue<Long> timeHistory1 = new LinkedList<>();
 
    
    public static boolean isMockLocationsEnabled;
    private  Runnable runnable;
    private  SensorProcessor sensorProcessor;
    
    private  Thread backgroundThread;
    public  LocationManager locationManager;
    private   FusedLocationProviderClient fusedLocation;
    
    private  Context context;
    
    public  Location ffLocation;
    public  SaveSensorJson saveDataSensor;
    
    
    private  ElevationService elevationService;
    private  Handler handler;
    private  boolean locSave = false;
    
    private static final int REQUEST_LOCATION_PERMISSION = 1;

    public  FusedLocationsProvider fusedLocationsProvider;
  
    private static final String TAG = "FLS";
    private static final String CHANNEL_ID = "FakeLocationServiceChannel";
    private static final String GPS_PROVIDER = LocationManager.GPS_PROVIDER;
    private static final String NETWORK_PROVIDER = LocationManager.NETWORK_PROVIDER;
    private static final String FUSED_PROVIDER = LocationManager.FUSED_PROVIDER;
    private  DataLocation dataLoc;
    
    private static final String PREF_NAME = "FakeLoc";
    
   public SharedPreferences prefs;
    
    
    public static final String ACTION_SEND_DATA = "com.carlex.drive.ACTION_SEND_DATA";
    public static final String PREFERENCES_FILE = "FakeLoc";
    public static final String KEY_DATA = "chave";

    
    
    
    private void saveLocationPreferences(Context context, double latitude, double longitude, float bearing, float speed, double altitude) {
        SharedPreferences prefs = getSharedPreferences (PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat("latitude", (float)(latitude));
        editor.putFloat("longitude", (float)(longitude));
        editor.putFloat("bearing", (bearing));
        editor.putFloat("speed", (speed));
        editor.putFloat("altitude", (float)(altitude));
        editor.apply();
    }
    
    private  boolean checkPermissions() {
        int writePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return writePermission == PackageManager.PERMISSION_GRANTED;
    }
   
    
   public static boolean bSE;
   public static JsonFileHandler saveSensor;
   private static final String KEY_LOCATION = "location.json";
   private static final String KEY_SENSOR = "sensor.json";
   private static final String DIRECTORY_PATH = "/storage/emulated/0/carlex/";

  public static Intent serviceIntent;
    
    
   public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        Locale.setDefault(Locale.US);
        isRunning= false;
        PreferenceManager.getDefaultSharedPreferences(this).edit().apply();
        dataLoc = new DataLocation();
        Log.i(TAG, "service created");
    }
    
    public boolean dataLocation() {
        return true;
    }
    
    public  boolean isMockLocationsEnabled(RotaFake rotaFake){
        boolean ismock = true; //isMockLocations();
        if (isMockLocationsEnabled != ismock){
            if (!isMockLocationsEnabled){
                  boolean init = initTestProvider();
                  gpsLocation = new Location(GPS_PROVIDER);
                  networkLocation = new Location(NETWORK_PROVIDER);
                  fusedLocationsProvider = new FusedLocationsProvider(context, networkLocation);
                  boolean st = setDefaultLocation(rotaFake);
            } else {
                  removeProviders();
            } 
            isMockLocationsEnabled = ismock;
        }
        return isMockLocationsEnabled;
    }
        
    
    
    public  boolean isMockLocations() {
        boolean isMockLocationEnabled;
        try {
            AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);

            if (appOps == null) {
                Log.e(TAG, "AppOpsManager is null");
                return false;
            }

            int mockLocationResult = appOps.checkOpNoThrow(AppOpsManager.OPSTR_MOCK_LOCATION, Process.myUid(), context.getPackageName());
            isMockLocationEnabled = mockLocationResult == AppOpsManager.MODE_ALLOWED;
          
            return isMockLocationEnabled;
        } catch (Exception e) {
            Log.e(TAG, "Error checking mock locations: ", e);
            return false;
        }
    }
    
    

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (isRunning) {
           Log.e(TAG, "Fake 66 service is runnig");
            return START_STICKY;
        }
        Log.i(TAG, "Fake 66 service start");

        isRunning = true;
        context = getApplicationContext();
        startForeground(1, getNotification("Fake Gps Ligado").build());
    
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            gpsLocation = locationManager.getLastKnownLocation(GPS_PROVIDER);
            networkLocation = locationManager.getLastKnownLocation(NETWORK_PROVIDER);
        }
        
        
        ffLocation = new Location(FUSED_PROVIDER);
        boolean gpsEnabled = locationManager.isProviderEnabled(GPS_PROVIDER);
        
        
        if (gpsEnabled) {
            Location gpsLocation = locationManager.getLastKnownLocation(GPS_PROVIDER);
            if (gpsLocation != null) {
                latitude = gpsLocation.getLatitude();
                longitude = gpsLocation.getLongitude();
                altitude = gpsLocation.getAltitude();
                bearing = gpsLocation.getBearing();
                RotaFake rotaFakeEntry = new RotaFake( latitude, longitude, bearing, 0.0, System.currentTimeMillis());
                boolean starmock = isMockLocationsEnabled(rotaFakeEntry);
            } else {
                boolean starmock = isMockLocationsEnabled(loadLastLoc());
            }
        }
    
        if (!SpaceManService.isRunning()){
            String tles = readRawTextFile(R.raw.gps);
            Intent spaceIntent = new Intent(context, SpaceManService.class);
            spaceIntent.putExtra("location", gpsLocation);
            spaceIntent.putExtra("tles", tles);
            startService(spaceIntent);
        }        
      
        locSave = dataLocation();
        
        elevationService = new ElevationService();
        
        if (backgroundThread == null || !backgroundThread.isAlive()) {
            boolean rr = startBackgroundTask();
        }
        
        return START_STICKY;
    }
    
    public Intent spaceIntent;

    public static void setIsRunnig(Boolean set){
        isRunning = set;
    }
    
    public static boolean isServiceRunning() {
        return isRunning;
    }

    public long s1=0L;
    
    public  boolean startBackgroundTask() {
        backgroundThread = new Thread(() -> {
            while (true) {
                Long ss = 0l;
                RotaFake rotaFake1 = MyApp.getDatabase().rotaFakeDao().getRotaFakeWithMinTime(System.currentTimeMillis());
                if (rotaFake1==null)
                    rotaFake1 = new RotaFake( latitude, longitude, bearing, 0.0, (long) (System.currentTimeMillis() + 100));
                
                boolean starmock = isMockLocationsEnabled(rotaFake1);
                MyApp.getDatabase().rotaFakeDao().deleteRotaFakeWithTimeGreaterThan(System.currentTimeMillis());
                ss = handleVehicleState(rotaFake1);
              /*  if (ss+s1 > 30) {*/
                    spoofLocationAndUpdate(rotaFake1);
                    //s1 = 0L;
                //} else s1 += ss;
                
                Log.d(TAG, ss+ "----------end mock cycle Time:"+ss+"-----------" );
            }
        });
        backgroundThread.start();
        return true;
    }

    public static Long uTempo = 0L;
    
    private  long handleVehicleState(RotaFake rotaFake1) {
        if (rotaFake1 != null) {
            //Verificar se o veículo está parado
            if (gpsLocation != null){
                if (gpsLocation.getSpeed()> 1 && parado == true){ //if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) { NotificationManagerCompat.from(context).notify(2, getNotification("66 em Rota").build()); }              
                    parado = false;       
                    notifyState("Em Rota");    
                    Log.i(TAG, "Fake 66 Rota created");
                } 
                if (gpsLocation.getSpeed()<= 1 && parado == false){ //if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) { NotificationManagerCompat.from(context).notify(2, getNotification("66 Estacionado").build()); }            
                    parado = true;    
                    notifyState("Estacionado");  
                    Log.i(TAG, "Fake 66 Rota stop");
                }
            }
		    //esperar tempo para proxima atualizacao
            long tempo = rotaFake1.getTempo();
            long diferencaTempo = tempo - System.currentTimeMillis();
		    if (diferencaTempo > 0) {
			    try {Thread.sleep(diferencaTempo);} 
			    catch (InterruptedException e) {}
                return diferencaTempo;
            }
        } else {
		    float tnoise = (float) (ThreadLocalRandom.current().nextDouble(90, 100));
		    velocidade = 0; // if (latitude < -10.0){ 
		    //RotaFake rotaFakeEntry = new RotaFake( latitude, longitude, bearing, 0.0, (long) (System.currentTimeMillis() + tnoise));
		    //MyApp.getDatabase().rotaFakeDao().insert(rotaFakeEntry);
			//rotaFake1 = MyApp.getDatabase().rotaFakeDao().getRotaFakeWithMinTime(System.currentTimeMillis());
		    try { Thread.sleep((long) tnoise); }   
		    catch (InterruptedException e) {}   
            return (long) tnoise ; 
		}                
        return 0l;          
    }

    private void spoofLocationAndUpdate(RotaFake rotaFake1) {
          gpsLocation = spoofLocation(rotaFake1, gpsLocation, GPS_PROVIDER);
          networkLocation = spoofLocation(rotaFake1, networkLocation, NETWORK_PROVIDER);
          ffLocation = spoofLocation(rotaFake1, ffLocation, FUSED_PROVIDER);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        
        if (SpaceManService.isRunning())
        if (spaceIntent!=null)
            stopService(spaceIntent);
        backgroundThread.interrupt();
        Log.i(TAG, "Fake 66 service S");
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private  void notifyState(String message) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(context).notify(2, getNotification(message).build());
        }
    }

    private  void sleepRandomTime() {
        float tnoise = (float) (ThreadLocalRandom.current().nextDouble(100, 150));
        try {
            Thread.sleep((long) tnoise);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    public  void getaltitude() {
        elevationService.obterAltitude(latitude, longitude, new ElevationService.ElevationCallback() {
                    public void onElevationReceived(double altitude) {   
                        FakeLocationService1.setAltitude(altitude);
                        Log.d(TAG, "A altitude da coordenada (" + latitude + ", " + longitude + ") é: " + altitude + " metros");
                    }
                    public void onError(Exception e) {
                        Log.e(TAG, "Erro ao obter a altitude", e);
                    }
                }); 
    }
    
    
    private static void setAltitude(double alt){
        altitude = alt;
    }

    
    /*
    private  boolean waitForNextUpdate(RotaFake rotaFake1) {
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
              //if (latitude < -.0){ 
                velocidade = 0.0;
			    RotaFake rotaFakeEntry = new RotaFake( latitude, longitude, bearing, 0.0, (long) (System.currentTimeMillis() + 159));
                MyApp.getDatabase().rotaFakeDao().insert(rotaFakeEntry);
			    rotaFake1 = MyApp.getDatabase().rotaFakeDao().getRotaFakeWithMinTime(System.currentTimeMillis());
             // }
        }
        return true;
    }*/

    private boolean setDefaultLocation(RotaFake rotaFakeEntry) {
       try{
            gpsLocation = spoofLocation(rotaFakeEntry, gpsLocation, GPS_PROVIDER);
            networkLocation = spoofLocation(rotaFakeEntry, gpsLocation, GPS_PROVIDER);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error requesting permissions: ", e);
            return false;
        }
    }   
    


    public  RotaFake loadLastLoc() {
        prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        
        latitude =  (double)prefs.getFloat("latitude", -23.5879554f);
        longitude = (double)prefs.getFloat( "longitude", -46.63816059f);
        bearing = prefs.getFloat("bearing", 45f);
        velocidade = 0.0;
        altitude = (double) prefs.getFloat("altitude", 750f);

        RotaFake rotaFakeEntry = new RotaFake( latitude, longitude, bearing, 0.0, System.currentTimeMillis());
        return rotaFakeEntry; 
    }
    
    
    private  double adicionarRuido(double valorOriginal, double nivelRuido) {
        Random random = new Random();
        double ruido = (random.nextDouble() * 2 - 1) * nivelRuido;
        return valorOriginal + ruido;
    }
    
    public float calcularMediaPonderadaAngular(Queue<Float> angulos) {
        // Calcular componentes x e y
        double x = 0.0;
        double y = 0.0;
        int n = 0;//angulos.size();
        int i=1;
        for (float value : angulos) {
            double anguloRad = Math.toRadians((double)value);
            double peso =  i ;  // O peso é o índice invertido
            x += peso * Math.cos(anguloRad);
            y += peso * Math.sin(anguloRad);
            i++;
        }

        // Calcular a média ponderada do ângulo
        double mediaAngularRad = Math.atan2(y, x);

        // Converter de volta para graus
        double mediaAngularGraus = Math.toDegrees(mediaAngularRad);

        return (float) mediaAngularGraus;
    }
    
    
    private double calculateWeightedAverage(Queue<Double> history) {
       if (history.size()>0){
            double total = 0;
            double weightSum = 0;
            int index =  1;//history.size();
            for (double value : history) {
                total += value * index;
                weightSum += index;
                index++;
            }
            double to = total / weightSum;
            return to;
        }
        else return (0.000);
    }

    
    private static final double LAT_LNG_NOISE_LEVEL = 0.000001; // Ajuste o nível de ruído conforme necessário
    private static final double BEARING_NOISE_LEVEL = 0.1; // Ajuste o nível de ruído conforme necessário
    private static final double VELOCIDADE_NOISE_LEVEL = 0.1; // Ajuste o nível de ruído conforme necessário

    private  long lastSaveTime = 0;
    
   public  boolean checkSave() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSaveTime < 5000) {
            return false;
        }
        lastSaveTime = currentTime;
        return true;
    }
    
    public  double formatarDecimais(double numero, int dec) {
        
        if (isNull(numero)) numero = 0.0;
        if (isInfinite(numero)) numero = 0.0;
        if (isNaN(numero)) numero = 0.0;
        
        
        BigDecimal bd = new BigDecimal(Double.toString(numero));
        bd = bd.setScale(dec, RoundingMode.HALF_UP); // Arredonda para 4 casas decimais
        return bd.doubleValue();
    }
    
    
    public static boolean isNull(Double value) {
        return value == null;
    }

    public static boolean isInfinite(Double value) {
        return value != null && Double.isInfinite(value);
    }

    public static boolean isNaN(Double value) {
        return value != null && Double.isNaN(value);
    }
    

    private  Location spoofLocation(RotaFake rotaFake1, Location location, String provider) {
       
        if (location == null) location = new Location(provider);
        if (rotaFake1 == null) rotaFake1 = new RotaFake( latitude, longitude, bearing, 0.0, System.currentTimeMillis());
 
        
        latitude = adicionarRuido(rotaFake1.getLatitude(), LAT_LNG_NOISE_LEVEL);
        longitude = adicionarRuido(rotaFake1.getLongitude(), LAT_LNG_NOISE_LEVEL);
        bearing = ((float) adicionarRuido(rotaFake1.getBearing(), BEARING_NOISE_LEVEL));
        velocidade = Math.abs(adicionarRuido(rotaFake1.getVelocidade(), VELOCIDADE_NOISE_LEVEL))/2;
      
        if (checkSave() && velocidade > 1) getaltitude();
        else altitude = adicionarRuido(altitude,0.5);
        
        
        long Timef = System.currentTimeMillis() + (long) ThreadLocalRandom.current().nextInt(5, 10);
           
        fLocation flocation = new fLocation( latitude, longitude,bearing, (float) velocidade, altitude, Timef);
        
        if (altitude >0) altitude=altitude;
        else altitude = 750;
        
        
        if (provider == GPS_PROVIDER){
            
            if (locationHistory.size() >= 3) {
                        locationHistory.poll();
                } 
            
            locationHistory.add(flocation);
            
            
        }
            
            if (locationHistory.size() >= 3){
                
                if (velocidade>1)
                    velocidade = Math.abs(adicionarRuido(flocation.getDeltaSpeedXY((fLocation) locationHistory.toArray()[1]),VELOCIDADE_NOISE_LEVEL));
            
                long ttime = Math.abs(flocation.getDeltaTime((fLocation) locationHistory.toArray()[1]));
                timeHistory.add(ttime);
                speedHistory.add(velocidade);
                bearingHistory.add(bearing);
                altitudeHistory.add(altitude);
                
                long totalSum = 0;
                for (double time : timeHistory) {
                    totalSum += time;
                }
                
                // Enquanto a soma dos registros + novo tempo for maior que 1000, remova o registro mais antigo
                if (timeHistory.size()>2){
                    while (totalSum > 2000) {
                        double removedTime = timeHistory.poll(); // Remove o registro mais antigo (início da fila)
                        speedHistory.poll();
                        bearingHistory.poll();
                        altitudeHistory.poll();
                        if (removedTime > 0) {
                            totalSum -= removedTime;
                        }
                    }
                }
                
                altitude = calculateWeightedAverage(altitudeHistory);
                velocidade = calculateWeightedAverage(speedHistory);
                bearing = calcularMediaPonderadaAngular(bearingHistory);
            }
        
        
        
        
        location.setLatitude(formatarDecimais(latitude,6));
        location.setLongitude(formatarDecimais(longitude,6));
        location.setBearing((float)formatarDecimais((double)bearing,2));
        location.setSpeed((float)formatarDecimais(velocidade/3l,2));
        location.setTime(Timef);
        location.setAltitude(formatarDecimais(altitude,2));
        location.setAccuracy(ThreadLocalRandom.current().nextInt(1, 3));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            location.setVerticalAccuracyMeters(Math.abs((float) (ThreadLocalRandom.current().nextInt(1, 3))));
            location.setSpeedAccuracyMetersPerSecond(Math.abs((float) formatarDecimais(ThreadLocalRandom.current().nextInt(1, 3)/3.6,2)));
            location.setBearingAccuracyDegrees(Math.abs(ThreadLocalRandom.current().nextInt(1, 3)));
        }
        location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos()+ThreadLocalRandom.current().nextInt(1, 50));
           
            if (isMockLocationsEnabled){
                try {
                    Bundle extras = new Bundle();                          
                    extras.putInt("satellites", 
                    SpaceManService.getSatelliteCount());
                    extras.putDouble("maxCn0", 
                    formatarDecimais((double)SpaceManService.getMaxCn0(),2)); 
                    extras.putDouble("meanCn0", 
                    formatarDecimais((double)SpaceManService.getMeanCn0(),2));      
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
            } 
            
            if (provider == GPS_PROVIDER) {
                       Log.i(TAG, "set mock location:"+location.toString());
               
            
                saveLocationPreferences(context, latitude, longitude, bearing, (float)velocidade, altitude);
         
            
                if (locSave){
                    if (locationHistory.size() >= 3) dataLoc.updateData(context, locationHistory);
                } else locSave = dataLocation();
            
            }
            return location;
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

    private  NotificationCompat.Builder getNotification(String message) {
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

   
    
   public  boolean  initTestProvider() {
	   //b if (isMockLocationsEnabled || isRunning) {
            boolean remo =  removeProviders();
            locationManager.addTestProvider(GPS_PROVIDER,false, false, false, false, true, true, true, ProviderProperties.POWER_USAGE_LOW, ProviderProperties.ACCURACY_FINE);
            locationManager.setTestProviderEnabled(GPS_PROVIDER, true);
            locationManager.addTestProvider(NETWORK_PROVIDER,false, false, false, false, true, true, true, ProviderProperties.POWER_USAGE_LOW, ProviderProperties.ACCURACY_COARSE);
            locationManager.setTestProviderEnabled(NETWORK_PROVIDER, true);
	   // }
        return remo;
    }

    
    
    public  boolean areLocationPermissionsGranted(Context context) {
          return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    

    public  boolean removeProviders() {
	   //  if (isMockLocationsEnabled) {
		     try {
			     locationManager.removeTestProvider(GPS_PROVIDER);
			     locationManager.removeTestProvider(NETWORK_PROVIDER);
		     } catch (IllegalArgumentException | SecurityException e) {
			    Log.d("Fakelocatinservice", "erro remover provedores", e);
                return false;
		    }                                                    
        return true;
     }
    
    
    
}
