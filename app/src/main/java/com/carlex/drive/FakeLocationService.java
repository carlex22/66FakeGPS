package com.carlex.drive;

import android.widget.Toast;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import com.google.android.gms.maps.model.LatLng;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import android.location.Location;
import android.location.LocationManager;



public class FakeLocationService extends Service {
    private static final String TAG = "FakeLocationService";
    private boolean isRunning;
    private Thread backgroundThread;
    public static boolean processado;              
    public FusedLocationsProvider fusedLocationsProvider;
    public  xLocationManager locationManager;
    public  Context context;
    public static List<Location> gpsLocations = new ArrayList<>();
    public static List<Location> networkLocations = new ArrayList<>();


    public double latitude, longitude, altitude;
    public float bearing;
    public double velocidade;


    @Override
    public void onCreate() {
        super.onCreate();
        this.context = getApplicationContext();

	fusedLocationsProvider = new FusedLocationsProvider(this);
        xLocationManager.initTestProvider(this);
        locationManager = xLocationManager.getInstance(this);
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	// Acessando rotaFake diretamente de MainActivity
    	List<Object[]> rotaFake = MainActivity.rotaFake;

    	if (rotaFake == null) {
        	sendNotification("Fake não iniciado");
        	stopSelf();
    	} else {
        	if (rotaFake.size() < 4) {
            		sendNotification("Fake estacionado");   
        	} else {
            		sendNotification("Fake em processamento");
        	}
		List<Object[]> finalRotaFake = rotaFake; 
		// Necessary for lambda expression    
		backgroundThread = new Thread(() -> {         
			simulateLocations(finalRotaFake);    
			sendNotification("Fake procesada");
			stopSelf();
		});                                           
		isRunning = true;        
		backgroundThread.start();
    	}
	
    	return START_NOT_STICKY;
    }


    @Override
    public void onDestroy() {
        isRunning = false;
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private void sendNotification(String message) {
    	String channelId = "fake_location_channel";
    	String channelName = "Fake Location Service";
    	NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ico)
            .setContentTitle("Fake Location Service")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT);
    	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        	NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
        	NotificationManager notificationManager = getSystemService(NotificationManager.class);
        	notificationManager.createNotificationChannel(channel);
    }
    	NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    	notificationManager.notify(1, notificationBuilder.build());
    }

    private void simulateLocations(List<Object[]> rotaFake) {
        long cumulativeTime = System.currentTimeMillis() + 5000;

	List<Object[]> rotaFakeCopy = new ArrayList<>(rotaFake);

    	for (Object[] dadosSegmento : rotaFakeCopy) {
            int indiceSegmento = (int) dadosSegmento[0];
            LatLng pontoAtual = (LatLng) dadosSegmento[1];
            float bearing = (float) dadosSegmento[2];
            double velocidade = (double) dadosSegmento[3];
	    double tempoo = (double) dadosSegmento[4];
            long tempo = (long) tempoo;
	    long tTime = System.currentTimeMillis();
            Location gpsLocation = new Location(LocationManager.GPS_PROVIDER);
	    long Time = System.currentTimeMillis();
            gpsLocation.setLatitude(pontoAtual.latitude);
            gpsLocation.setLongitude(pontoAtual.longitude);
            gpsLocation.setBearing(bearing);
            gpsLocation.setSpeed((float) velocidade);
            gpsLocation.setTime(tTime);
            gpsLocation.setAltitude(700 + Math.random() * 50);
            gpsLocation.setAccuracy(1.0f);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                gpsLocation.setVerticalAccuracyMeters((float) ThreadLocalRandom.current().nextDouble(0, 4));
                gpsLocation.setSpeedAccuracyMetersPerSecond(20.0f);
                gpsLocation.setBearingAccuracyDegrees(10.0f);
            }

            gpsLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());


	    fusedLocationsProvider.spoof(gpsLocation);
	    locationManager.setGpsProvider(gpsLocation);

	    //Log.d("FakeLocationService", "Fake location set: " + location.toString());

	    try {                                 
		    Thread.sleep(tempo);           
	    } catch (InterruptedException e) {        
		    e.printStackTrace();         
	    }

          

        }
        processado = true; 
}



}

