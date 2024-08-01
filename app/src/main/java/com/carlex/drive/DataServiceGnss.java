package com.carlex.drive;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.SharedPreferences;
import android.app.Service;
import android.content.Context;
import java.io.IOException;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import android.os.IBinder;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.Manifest;
import android.os.Build;

import java.util.Map; // Importar a classe Map


// DataService.java
public class DataServiceGnss extends Service {
    public static final String ACTION_SEND_DATA = "com.carlex.drive.ACTION_SEND_DATA";
    public static final String PREFERENCES_FILE = "FakeSensorGnss";
    public static final String KEY_DATA = "Ax";
    public Context context;
    private static final String TAG = "FSDG";
    private static final String CHANNEL_ID = "FakeSensorGnssServiceChannel";

    
    private SharedPreferences sharedPreferences;
    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;

    @Override
    public void onCreate() {
        super.onCreate();

        // Configurar SharedPreferences
        sharedPreferences = getSharedPreferences(PREFERENCES_FILE, MODE_PRIVATE);
        // Definir listener para mudanças em SharedPreferences
        preferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                sendAllPreferences();
            }
        };

        sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener);

        // Tornar o serviço em foreground
        context = getApplicationContext();
        createNotificationChannel();
        startForeground(1, getNotification("Fake Sensor Gnss Ligado").build());
    
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sendAllPreferences();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    
    private void sendAllPreferences() {
        Map<String, ?> allPrefs = sharedPreferences.getAll();
        Intent broadcastIntent = new Intent(ACTION_SEND_DATA);

        for (Map.Entry<String, ?> entry : allPrefs.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof String) {
                broadcastIntent.putExtra(entry.getKey(), (String) value);
            } else if (value instanceof Integer) {
                broadcastIntent.putExtra(entry.getKey(), (Integer) value);
            } else if (value instanceof Boolean) {
                broadcastIntent.putExtra(entry.getKey(), (Boolean) value);
            } else if (value instanceof Float) {
                broadcastIntent.putExtra(entry.getKey(), (Float) value);
            } else if (value instanceof Long) {
                broadcastIntent.putExtra(entry.getKey(), (Long) value);
            }
        }
        sendBroadcast(broadcastIntent);
    }


    private  void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Fake sensor Gnss Service Channel";
            String description = "Channel for Fake gnss Location Service";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private  NotificationCompat.Builder getNotification(String message) {
        return new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("Fake sensor Gnss Service")
                .setContentText(message)
                .setSmallIcon(R.drawable.ico)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
    }
}
