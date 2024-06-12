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
import android.provider.Settings;
import android.os.SystemClock;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import android.util.Log;
import java.util.concurrent.ThreadLocalRandom;
import android.location.provider.ProviderProperties;
import com.google.android.gms.common.ConnectionResult;    
import android.widget.Toast;                           


import android.Manifest;
import android.content.Context;      
import android.content.pm.PackageManager;               import android.location.Location;                       import android.location.LocationListener;               import android.location.LocationManager;                import android.os.Bundle;                               import androidx.core.content.ContextCompat;             import com.google.android.gms.maps.model.LatLng;                                                                import android.Manifest;                                import android.content.Context;                         import android.content.pm.PackageManager;               import android.location.Criteria;                       import android.location.Location;                       import android.location.LocationManager;                import android.os.Build;                                import android.os.SystemClock;                          import android.provider.Settings;                       import android.util.Log;                                import android.location.provider.ProviderProperties;    import androidx.core.content.ContextCompat;             import androidx.core.view.InputDeviceCompat;            import java.util.Random;                                import java.util.concurrent.ThreadLocalRandom;          import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;   
import com.google.android.gms.location.FusedLocationProviderClient; 
import com.google.android.gms.location.LocationServices;


public class FakeLocationService1 extends Service {
    public double latitude, longitude, altitude;
    public float bearing;
    public double velocidade;
    public static final String TAG = "FakeLocationService";
    public static boolean isRunning = false;
    public static boolean parado = true;
    public Thread backgroundThread;
    public static boolean processado;
    //public static Toast toast;
    public FusedLocationsProvider fusedLocationsProvider;
    public static  LocationManager locationManager;
    public Context context;
    public static Location gpsLocation;
    public static Location networkLocation;
    public static FusedLocationProviderClient fusedLocation;
    private static final String CHANNEL_ID = "FakeLocationServiceChannel";
    private static final String GPS_PROVIDER = LocationManager.GPS_PROVIDER;                                                 
    private static final String NETWORK_PROVIDER = LocationManager.NETWORK_PROVIDER;                                            
    private static boolean isMockLocationsEnabled;


    
    public void onCreate() {
        super.onCreate();
        this.context = MainActivity.context;

	this.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

	// Verifica se o GPS está disponível
	boolean gpsEnabled = locationManager.isProviderEnabled(GPS_PROVIDER);


	isMockLocationsEnabled = true;

	// Verifica  permissão de localização
	if (areLocationPermissionsGranted(context)) {
		// Verifica se o GPS está disponível
		if (gpsEnabled) {
			Toast.makeText(context, "GPS disponível", Toast.LENGTH_SHORT).show();
			gpsLocation = locationManager.getLastKnownLocation(GPS_PROVIDER);		
			latitude = gpsLocation.getLatitude();    
			longitude = gpsLocation.getLongitude(); 
			altitude = gpsLocation.getAltitude(); 
			bearing = gpsLocation.getBearing();
		}
	} else {
		Toast.makeText(context, "Localizacao inicial definida", Toast.LENGTH_SHORT).show();
		latitude = -23.5879554;
		longitude = -46.63816059;
		altitude = 750.0;
		bearing = 45f;
		gpsLocation = new Location(GPS_PROVIDER);
		gpsLocation.setLatitude(latitude);
		gpsLocation.setLongitude(longitude);
		gpsLocation.setAltitude(altitude);
		gpsLocation.setBearing(bearing);
		//locationManager.setLocation(gpsLocation);
		//locationManager.setTestProviderLocation(provider, location);
	}

	gpsLocation = locationManager.getLastKnownLocation(GPS_PROVIDER);
	networkLocation = locationManager.getLastKnownLocation(NETWORK_PROVIDER);
	//fusedLocation = LocationServices.getFusedLocationProviderClient(this);


	fusedLocationsProvider = new FusedLocationsProvider(context);

	createNotificationChannel();


	if (isMockLocationsEnabled){
		Toast.makeText(context, "Permissão de localização falsa concedida", Toast.LENGTH_SHORT).show();
		startForeground(1, getNotification("66 Fake Gps Ligado").build());
		initTestProvider();
	} else {
		startForeground(1, getNotification("Falha ao iniciar 66 Fake").build());
		Toast.makeText(context, "Permissão de localização falsa não concedida", Toast.LENGTH_SHORT).show();
		return;
	}

	isRunning = true;
    }



    public static boolean isServiceRunning() {
        return isRunning;
    }

    
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(() -> {

	    //iniciar servico loop infinito
            while (true) {

                //Limpaar dados antigos
		MyApp.getDatabase().rotaFakeDao().deleteRotaFakeWithTimeGreaterThan(System.currentTimeMillis());

                RotaFake rotaFake1 = MyApp.getDatabase().rotaFakeDao().getRotaFakeWithMinTime(System.currentTimeMillis());

		//Verificar se o veículo está parado
		if (gpsLocation.getSpeed()>0.5 && parado == true){ if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) { NotificationManagerCompat.from(this).notify(2, getNotification("66 em Rota").build()); }              
			parado = false;                  
		} 

		if (gpsLocation.getSpeed()<=0.5 && parado == false){ if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) { NotificationManagerCompat.from(this).notify(2, getNotification("66 Estacionado").build()); }            
			parado = true;                        
		}

		//Verificar dados rotafake
		if (rotaFake1 != null) {
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

	        //iniciar spoofing   
		if (latitude < -10.0) {              
			if (isMockLocationsEnabled) { 
				spoofLocation(rotaFake1, gpsLocation, GPS_PROVIDER);   
				spoofLocation(rotaFake1, networkLocation, NETWORK_PROVIDER);
				fusedLocationsProvider.spoof(gpsLocation);
			}
		}

		//limpar pontos antigos
		MyApp.getDatabase().rotaFakeDao().deleteRotaFakeWithTimeGreaterThan(System.currentTimeMillis()); 
	    }

	//fim thread
	}).start();

	//nao reiniciar 
	return START_NOT_STICKY;
    }

	
    //criar localizacao spoofada

    public void spoofLocation(RotaFake rotaFake1, Location location, String provider) {

	    latitude = rotaFake1.getLatitude();   
	    longitude = rotaFake1.getLongitude();      
	    bearing = rotaFake1.getBearing();        
	    velocidade = rotaFake1.getVelocidade();                                                                                 
	    //Location gpsLocation = new Location(provider);
	    float noise = (float) (ThreadLocalRandom.current().nextDouble(0, 20)/10);   
	    long Timef = System.currentTimeMillis();              
	    location.setLatitude(latitude);                
	    location.setLongitude(longitude);            
	    location.setBearing(bearing+(noise/2));         
	    location.setSpeed((float) ((velocidade+(noise/3.6f))/4));
	    location.setTime(Timef);                         
	    location.setAltitude((double) ((700 + Math.random() * 50)+noise));
	    location.setAccuracy((float) (ThreadLocalRandom.current().nextDouble(0, 20)/10));                                                       
	    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
		    location.setVerticalAccuracyMeters((float) (ThreadLocalRandom.current().nextDouble(0, 20)/10));            
		    location.setSpeedAccuracyMetersPerSecond(noise/3.6f);
		    location.setBearingAccuracyDegrees(noise/2);
	    }

	    location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());

	    if (isMockLocationsEnabled){
	    	try {                                           
		    //locationManager.setLocation(location);        
		    locationManager.setTestProviderLocation(provider, location);
	    	} catch (SecurityException se) {      
		    Log.d("FakeLocationService", "falha no Mock", se);                            
	   	}	
	    }
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

    
    public static void removeProviders() {      
	     if (isMockLocationsEnabled) {       
		     try {                   
			     locationManager.removeTestProvider(GPS_PROVIDER);                                        
			     locationManager.removeTestProvider(NETWORK_PROVIDER);        
		     } catch (IllegalArgumentException | SecurityException e) {                         
			     Log.d("Fakelocatinservice", "erro remover provedores", e);           
		     }                                                             }                   
     }                                                                                   

     
     public static boolean areLocationPermissionsGranted(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
     }                                                                                                                                   
     
     public static boolean isMockLocationsEnabled(Context context) { 
	return Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION, 0) != 0;       
     }

    
    public void onDestroy() {
        super.onDestroy();
	isRunning = false;
        stopForeground(true);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(this).notify(2, getNotification("Fake Location Service Stopped").build());
        } 
	removeProviders();
    }


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
