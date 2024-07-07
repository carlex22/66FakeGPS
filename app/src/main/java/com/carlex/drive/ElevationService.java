package com.carlex.drive;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ElevationService {
    private static final String API_URL = "https://api.openrouteservice.org/elevation/point?format_out=point&api_key=5b3ce3597851110001cf6248c8a113724e2a4ace9ca97d8082bf1942&geometry=";

   private static final String TAG = "ElevationService";

    
    public interface ElevationCallback {
        void onElevationReceived(double altitude);
        void onError(Exception e);
    }

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public void obterAltitude(double latitude, double longitude, ElevationCallback callback) {
        executorService.submit(() -> {
            try {
                double altitude = fetchAltitude(latitude, longitude);
                mainHandler.post(() -> callback.onElevationReceived(altitude));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    private double fetchAltitude(double latitude, double longitude) throws Exception {
        String urlStr = API_URL + longitude + "," + latitude;
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        
        
       int responseCode = conn.getResponseCode();
        Log.d(TAG, "Código de resposta HTTP: " + responseCode);
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            Log.d(TAG, "Resposta da API: " + response.toString());

            JSONObject jsonResponse = new JSONObject(response.toString());
            double altitude = jsonResponse.getJSONArray("geometry").getDouble(2);
            return altitude;
        } else {
            throw new Exception("Failed to get elevation data. HTTP response code: " + responseCode);
        }
    }
}
