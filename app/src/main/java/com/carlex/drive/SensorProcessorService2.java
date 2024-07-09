package com.carlex.drive;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.topjohnwu.superuser.io.SuFile;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.time.Instant;
import java.util.LinkedList;

public class SensorProcessorService2 extends Service {

    private static final String TAG = "SensorProcessorService";
    private static final String DIRECTORY_PATH = "/storage/emulated/0/carlex/";
    private static final String INPUT_FILE = "locations.json";
    private static final String OUTPUT_FILE = "sensor.json";
    private static final long INTERVAL = 100; // 0.1 segundo
    private static final double GRAVITY = 9.81; // Gravidade terrestre em m/s²

    private static LinkedList<LocationData> locationHistory = new LinkedList<>();
    private static final int HISTORY_SIZE = 2;

    private static LinkedList<PseudoSensorData> pseudoSensorHistory = new LinkedList<>();
    private static final int PSEUDO_HISTORY_SIZE = 50;
 
    private Handler handler;
    private Runnable runnable; 

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                processSensorData();
                handler.postDelayed(this, INTERVAL); // Executa a cada 0.1 segundo
            }
        };
        handler.post(runnable);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }

    private void processSensorData() {
        try {
            File inputFile = SuFile.open(DIRECTORY_PATH, INPUT_FILE);
            if (!inputFile.exists()) {
                Log.e(TAG, "Input file does not exist.");
                return;
            }

            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            StringBuilder contentBuilder = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                contentBuilder.append(line);
            }
            reader.close();

            String content = contentBuilder.toString();
            JSONArray data = new JSONArray(content);
            JSONObject currentLocationJson = data.getJSONObject(0);

            long currentTime = Instant.now().toEpochMilli(); // Timestamp em milissegundos

            LocationData currentLocation = new LocationData(
                currentLocationJson.getDouble("Latitude"),
                currentLocationJson.getDouble("Longitude"),
                currentLocationJson.getDouble("Altitude"),
                currentLocationJson.getDouble("Bearing"),
                currentLocationJson.getLong("Timestamp")
            );

            locationHistory.add(currentLocation);
            if (locationHistory.size() > HISTORY_SIZE) {
                locationHistory.removeFirst();
            }

            if (locationHistory.size() == 2) {
                LocationData previousLocation = locationHistory.get(0);
                double deltaTempo = currentLocation.time - previousLocation.time;

                if (deltaTempo > 0) {
                    // Calcular as distâncias em metros usando a fórmula de Haversine
                    double distanciaLat = haversineDistance(previousLocation.latitude, previousLocation.longitude, currentLocation.latitude, previousLocation.longitude);
                    double distanciaLon = haversineDistance(previousLocation.latitude, previousLocation.longitude, previousLocation.latitude, currentLocation.longitude);
                    double distanciaAlt = currentLocation.altitude - previousLocation.altitude;

                    // Calcular as componentes da velocidade
                    double velocidadeX = distanciaLat / deltaTempo;
                    double velocidadeY = distanciaLon / deltaTempo;
                    double velocidadeZ = distanciaAlt / deltaTempo;

                    // Calcular as componentes da aceleração e adicionar gravidade terrestre
                    double aceleracaoX = (velocidadeX - previousLocation.velocidadeX) / deltaTempo;
                    double aceleracaoY = (velocidadeY - previousLocation.velocidadeY) / deltaTempo;
                    double aceleracaoZ = ((velocidadeZ - previousLocation.velocidadeZ) / deltaTempo) + GRAVITY;

                    // Atualizar velocidades
                    currentLocation.velocidadeX = velocidadeX;
                    currentLocation.velocidadeY = velocidadeY;
                    currentLocation.velocidadeZ = velocidadeZ;

                    // Calcular valores pseudo de giroscópio com base na mudança de direção
                    double deltaBearing = Math.toRadians(currentLocation.bearing - previousLocation.bearing);
                    double gx = deltaBearing / deltaTempo; // Yaw
                    double gy = Math.atan2(distanciaAlt, Math.sqrt(Math.pow(distanciaLat, 2) + Math.pow(distanciaLon, 2))) / deltaTempo; // Pitch
                    double gz = Math.atan2(distanciaLon, distanciaLat) / deltaTempo; // Roll

                    // Adicionar dados de pseudo-sensores à lista histórica
                    PseudoSensorData pseudoSensorData = new PseudoSensorData(aceleracaoX, aceleracaoY, aceleracaoZ, gx, gy, gz);
                    pseudoSensorHistory.add(pseudoSensorData);
                    if (pseudoSensorHistory.size() > PSEUDO_HISTORY_SIZE) {
                        pseudoSensorHistory.removeFirst();
                    }

                    // Calcular média ponderada dos pseudo-sensores
                    double totalWeight = 0;
                    double weight = 1.0;
                    double sumAx = 0, sumAy = 0, sumAz = 0, sumGx = 0, sumGy = 0, sumGz = 0;

                    for (int i = pseudoSensorHistory.size() - 1; i >= 0; i--) {
                        PseudoSensorData dataPoint = pseudoSensorHistory.get(i);
                        sumAx += dataPoint.ax * weight;
                        sumAy += dataPoint.ay * weight;
                        sumAz += dataPoint.az * weight;
                        sumGx += dataPoint.gx * weight;
                        sumGy += dataPoint.gy * weight;
                        sumGz += dataPoint.gz * weight;
                        totalWeight += weight;
                        weight *= 0.99; // Decrescimento exponencial do peso
                    }

                    double avgAx = sumAx / totalWeight;
                    double avgAy = sumAy / totalWeight;
                    double avgAz = sumAz / totalWeight;
                    double avgGx = sumGx / totalWeight;
                    double avgGy = sumGy / totalWeight;
                    double avgGz = sumGz / totalWeight;

                    // Verificar se os valores não são NaN antes de salvar no JSON
                    if (!Double.isNaN(avgAx) && !Double.isNaN(avgAy) && !Double.isNaN(avgAz) &&
                        !Double.isNaN(avgGx) && !Double.isNaN(avgGy) && !Double.isNaN(avgGz)) {
                        // Salvar dados interpolados no JSON de saída
                        JSONArray outputData = new JSONArray();
                        JSONObject interpolatedData = new JSONObject();
                        interpolatedData.put("Timestamp", currentTime);
                        interpolatedData.put("Latitude", currentLocation.latitude);
                        interpolatedData.put("Longitude", currentLocation.longitude);
                        interpolatedData.put("Altitude", currentLocation.altitude);
                        interpolatedData.put("Bearing", currentLocation.bearing);
                        interpolatedData.put("Ax", avgAx);
                        interpolatedData.put("Ay", avgAy);
                        interpolatedData.put("Az", avgAz);
                        interpolatedData.put("Gx", avgGx);
                        interpolatedData.put("Gy", avgGy);
                        interpolatedData.put("Gz", avgGz);
                        outputData.put(interpolatedData);

                        // Salvar todos os dados processados no arquivo JSON de saída
                        File outputFile = SuFile.open(DIRECTORY_PATH, OUTPUT_FILE);
                        if (!outputFile.exists()) {
                            outputFile.createNewFile();
                        }

                        FileWriter fileWriter = new FileWriter(outputFile);
                        fileWriter.write(outputData.toString(4));
                        fileWriter.close();
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing sensor data", e);
        }
    }

    private double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // Raio da Terra em metros
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private static class LocationData {
        double latitude;
        double longitude;
        double altitude;
        double bearing;
        double time;
        double velocidadeX;
        double velocidadeY;
        double velocidadeZ;

        LocationData(double latitude, double longitude, double altitude, double bearing, double time) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.altitude = altitude;
            this.bearing = bearing;
            this.time = time;
            this.velocidadeX = 0;
            this.velocidadeY = 0;
            this.velocidadeZ = 0;
        }
    }

    private static class PseudoSensorData {
        double ax, ay, az;
        double gx, gy, gz;

        PseudoSensorData(double ax, double ay, double az, double gx, double gy, double gz) {
            this.ax = ax;
            this.ay = ay;
            this.az = az;
            this.gx = gx;
            this.gy = gy;
            this.gz = gz;
        }
    }
}
