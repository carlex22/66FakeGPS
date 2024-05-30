package com.carlex.drive;

import android.Manifest;
import android.app.Notification;
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
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import android.util.Log;
import java.util.concurrent.ThreadLocalRandom;

public class FakeLocationService1 extends Service {

    public double latitude, longitude, altitude;
    public float bearing;
    public double velocidade;

    public static final String TAG = "FakeLocationService";
    public static boolean isRunning = false;
    public static boolean parado = true;
    public Thread backgroundThread;
    public static boolean processado;
    public FusedLocationsProvider fusedLocationsProvider;
    public xLocationManager locationManager;
    public Context context;

    private static final String CHANNEL_ID = "FakeLocationServiceChannel";

    @Override
    public void onCreate() {
        super.onCreate();
        context = MainActivity.mainApp;
        locationManager = xLocationManager.getInstance(MainActivity.mainApp);

        createNotificationChannel();
        startForeground(1, getNotification("Fake Location Service Started").build());

	isRunning = true;

        // Obter a última posição conhecida

        try {
            Log.i(TAG, "Sleeping for 5000 ms");
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // Log.e(TAG, "Sleep interrupted", e);
        }

        latitude = locationManager.getLatitude();
        longitude = locationManager.getLongitude();
        altitude = locationManager.getAltitude();
        bearing = locationManager.getBearing();

        fusedLocationsProvider = new FusedLocationsProvider(this);
        xLocationManager.initTestProvider(this);
        locationManager = xLocationManager.getInstance(this);

        Log.i(TAG, "Service created with initial GPS data:" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", altitude=" + altitude +
                ", bearing=" + bearing);
    }


    public static boolean isServiceRunning() {
        return isRunning;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(() -> {
            while (true) {
               //Log.i(TAG, "Deleting old records from database");
                
		//MyApp.getDatabase().rotaFakeDao().deleteRotaFakeWithTimeGreaterThan(System.currentTimeMillis());

                RotaFake rotaFake1 = MyApp.getDatabase().rotaFakeDao().getRotaFakeWithMinTime(System.currentTimeMillis());
                if (rotaFake1 != null) {
			/*if (rotaFake1.getVelocidade()>=1.0){                                                              if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {                                                                           NotificationManagerCompat.from(this).notify(2, getNotification("Fake Location Em Rota").build());                                   }                                                                 parado = false;                                           }*/
                    latitude = rotaFake1.getLatitude();
                    longitude = rotaFake1.getLongitude();
                    //altitude = 750;
                    bearing = rotaFake1.getBearing();
                    velocidade = rotaFake1.getVelocidade();

                    Log.i(TAG, "Fetched RotaFake from database: " +
                            "latitude=" + latitude +
                            ", longitude=" + longitude +
                            ", altitude=" + altitude +
                            ", bearing=" + bearing +
                            ", velocidade=" + velocidade);

                    Location gpsLocation = new Location(LocationManager.GPS_PROVIDER);
		    float noise = (float) (ThreadLocalRandom.current().nextDouble(0, 20)/10);
                    long Timef = System.currentTimeMillis();
                    gpsLocation.setLatitude(latitude);
                    gpsLocation.setLongitude(longitude);
                    gpsLocation.setBearing(bearing+(noise/2));
                    gpsLocation.setSpeed((float) ((velocidade+noise)/4));
                    gpsLocation.setTime(Timef);
                    gpsLocation.setAltitude((double) ((700 + Math.random() * 50)+noise));
		    gpsLocation.setAccuracy((float) (ThreadLocalRandom.current().nextDouble(0, 20)/10));

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        gpsLocation.setVerticalAccuracyMeters((float) (ThreadLocalRandom.current().nextDouble(0, 20)/10));
                        gpsLocation.setSpeedAccuracyMetersPerSecond(noise);
                        gpsLocation.setBearingAccuracyDegrees(noise/2);
                    }

                    gpsLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());

                    Log.i(TAG, "Spoofing GPS location: " +
                            "latitude=" + gpsLocation.getLatitude() +
                            ", longitude=" + gpsLocation.getLongitude() +
                            ", bearing=" + gpsLocation.getBearing() +
                            ", speed=" + gpsLocation.getSpeed() +
                            ", altitude=" + gpsLocation.getAltitude());

                    if (latitude < -10.0) {
                        locationManager.setGpsProvider(gpsLocation);
			//gpsLocation = setProviderForLocation(gpsLocation, "Fused");
			fusedLocationsProvider.spoof(gpsLocation);
		        //gpsLocation = setProviderForLocation(gpsLocation, "Network"); 
			locationManager.setNetworkProvider(gpsLocation);
                    }

                    long tempo = rotaFake1.getTempo();
                    long diferencaTempo = tempo - System.currentTimeMillis();

                    if (diferencaTempo > 0) {
                        try {
                            Log.i(TAG, "Sleeping for " + diferencaTempo + " ms");
                            Thread.sleep(diferencaTempo);
                        } catch (InterruptedException e) {
                            Log.e(TAG, "Sleep interrupted", e);
                        }
                    }
                } else {

                    if (latitude < -10.0){             
			    /*if (rotaFake1.getVelocidade()<1.0){
				    if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
				    NotificationManagerCompat.from(this).notify(2, getNotification("Fake Location Estacionado").build()); 
				    }
				    parado = true;
			    }*/
			    RotaFake rotaFakeEntry = new RotaFake(latitude, longitude, bearing, velocidade, (long) (System.currentTimeMillis() + 1000));
			    MyApp.getDatabase().rotaFakeDao().insert(rotaFakeEntry);
		    } 
		    try {                   
			    Log.i(TAG, "Sleeping for 500 ms");      
			    Thread.sleep(500);                  
		    } catch (InterruptedException e) {          
			    Log.e(TAG, "Sleep interrupted", e);      
		    }                                                    
		}                                          
		
		MyApp.getDatabase().rotaFakeDao().deleteRotaFakeWithTimeGreaterThan(System.currentTimeMillis()); 

            }
        }).start();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
	isRunning = false;
        stopForeground(true);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(this).notify(2, getNotification("Fake Location Service Stopped").build());
        } else {
            // Notificar o usuário sobre a necessidade de permissões
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private NotificationCompat.Builder getNotification(String message) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Fake Location Service")
                .setContentText(message)
                .setSmallIcon(R.drawable.ico)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
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
}




/*package com.carlex.drive;

import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.SystemClock;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import java.util.concurrent.ThreadLocalRandom;

public class FakeLocationService1 extends Service {

    public double latitude, longitude, altitude;
    public float bearing;
    public double velocidade;

    public static final String TAG = "FakeLocationService";
    public boolean isRunning;
    public Thread backgroundThread;
    public static boolean processado;
    public FusedLocationsProvider fusedLocationsProvider;
    public xLocationManager locationManager;
    public Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = MainActivity.mainApp;
        locationManager = xLocationManager.getInstance(MainActivity.mainApp);

    	// Obter a última posição conhecida
    	

	try {                     
		Log.i(TAG, "Sleeping for 5000 ms");

                        Thread.sleep(5000);
                    } catch (InterruptedException e) {        
			    //Log.e(TAG, "Sleep interrupted", e);
                    }

	/*
    	if (lastKnownLocation != null) {
        	latitude = lastKnownLocation.getLatitude();
        	longitude = lastKnownLocation.getLongitude();
        	altitude = lastKnownLocation.getAltitude();
        	bearing = lastKnownLocation.getBearing();

    	} else {
        	latitude = -23.5505;
        	longitude = -46.6333;
        	altitude = 0.0; 
        	bearing = (float) 0.0; 
    	}*/

/*//////
	latitude = locationManager.getAltitude();       
	longitude = locationManager.getAltitude();        
	altitude = locationManager.getAltitude();     
	bearing = locationManager.getBearing();





        fusedLocationsProvider = new FusedLocationsProvider(this);
        xLocationManager.initTestProvider(this);
        locationManager = xLocationManager.getInstance(this);

        Log.i(TAG, "Service created with initial GPS data:" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", altitude=" + altitude +
                ", bearing=" + bearing);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(() -> {
            while (true) {
                Log.i(TAG, "Deleting old records from database");
          //      MyApp.getDatabase().rotaFakeDao().deleteRotaFakeWithTimeGreaterThan(System.currentTimeMillis());

                RotaFake rotaFake1 = MyApp.getDatabase().rotaFakeDao().getRotaFakeWithMinTime(System.currentTimeMillis());
                if (rotaFake1 != null) {
                    latitude = rotaFake1.getLatitude();
                    longitude = rotaFake1.getLongitude();
                    altitude = 750;
                    bearing = rotaFake1.getBearing();
                    velocidade = rotaFake1.getVelocidade();

                    Log.i(TAG, "Fetched RotaFake from database: " +
                            "latitude=" + latitude +
                            ", longitude=" + longitude +
                            ", altitude=" + altitude +
                            ", bearing=" + bearing +
                            ", velocidade=" + velocidade);

                    Location gpsLocation = new Location(LocationManager.GPS_PROVIDER);
                    long Timef = System.currentTimeMillis();
                    gpsLocation.setLatitude(latitude);
                    gpsLocation.setLongitude(longitude);
                    gpsLocation.setBearing(bearing);
                    gpsLocation.setSpeed((float) velocidade);
                    gpsLocation.setTime(Timef);
                    gpsLocation.setAltitude(700 + Math.random() * 50);
                    gpsLocation.setAccuracy(1.0f);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        gpsLocation.setVerticalAccuracyMeters((float) ThreadLocalRandom.current().nextDouble(0, 4));
                        gpsLocation.setSpeedAccuracyMetersPerSecond(20.0f);
                        gpsLocation.setBearingAccuracyDegrees(10.0f);
                    }

                    gpsLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());

                    Log.i(TAG, "Spoofing GPS location: " +
                            "latitude=" + gpsLocation.getLatitude() +
                            ", longitude=" + gpsLocation.getLongitude() +
                            ", bearing=" + gpsLocation.getBearing() +
                            ", speed=" + gpsLocation.getSpeed() +
                            ", altitude=" + gpsLocation.getAltitude());

		    if (latitude < -10.0){
                    	fusedLocationsProvider.spoof(gpsLocation);
                    	locationManager.setGpsProvider(gpsLocation);
		    }

                    long tempo = rotaFake1.getTempo();
                    long diferencaTempo = tempo - System.currentTimeMillis();

                    if (diferencaTempo > 0) {
                        try {
                            Log.i(TAG, "Sleeping for " + diferencaTempo + " ms");
                            Thread.sleep(diferencaTempo);
                        } catch (InterruptedException e) {
                          Log.e(TAG, "Sleep interrupted", e);
                        }
                    }
                } else {

		    if (latitude < -10.0){
                    RotaFake rotaFakeEntry = new RotaFake(latitude, longitude, bearing, velocidade, (long) (System.currentTimeMillis() + 1000));
                    MyApp.getDatabase().rotaFakeDao().insert(rotaFakeEntry);}

                    Log.i(TAG, "Inserted new RotaFake into database: " +
                            "latitude=" + latitude +
                            ", longitude=" + longitude +
                            ", bearing=" + bearing +
                            ", velocidade=" + velocidade);

                    try {
                        Log.i(TAG, "Sleeping for 500 ms");
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Log.e(TAG, "Sleep interrupted", e);
                    }
                }

		MyApp.getDatabase().rotaFakeDao().deleteRotaFakeWithTimeGreaterThan(System.currentTimeMillis());

            }
        }).start();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        isRunning = false;
        Log.i(TAG, "Service destroyed");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
*/
