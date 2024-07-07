package com.carlex.drive;

import java.math.RoundingMode;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Handler;
import android.util.Log;
import java.math.BigDecimal;
import com.topjohnwu.superuser.io.SuFile;
import java.io.BufferedReader;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Locale;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import androidx.core.app.NotificationCompat;

public class SensorProcessor  {
    private static final String TAG = "SensorProcessorService";
    private static final String DIRECTORY_PATH = "/data/system/carlex/";
    private static final String INPUT_FILE = "locations.json";
    private static final String OUTPUT_FILE = "sensor.json";
    private static final double GRAVITY = 9.81;

    public static double lat = 0.1, lon = 0.1, alt  = 0.1, bear = 0.1, speed = 0.1;
    public static long tempo = 100l;
    
    public static double[] last = { 
                 0.001,
                 0.002, 0.003, 0.004, 
                 0.005, 0.006,
                 0.007,
                 0.008
            };
    
    public static boolean isRunning = false;
    
    private static Thread backgroundThread;
    
    private Timer timer;
    private Handler handler;
    private static Random random;

   
    
    public static boolean starTask() {
       // backgroundThread = new Thread(() -> {
           if (tempo > 0l) {
               // try {
                   // Log.d(TAG, "Checking location update...");
                   // if (lat != last[6] || lon != last[7] || alt != last[9]) {
                        Log.d(TAG, "tempo: " + tempo + String.format(" Updating location data, lat=%f, lon=%f, alt=%f, bear=%f", lat, lon, alt, bear));
                        last = processLocationData();
                       // tempo = 0;
                        /*try {
                            Thread.sleep(tempo);
                        } catch (InterruptedException e) {
                            Log.e(TAG, "Background thread interrupted", e);
                        */
                    /* } else {
                        tempo += 10L;
                        try {
                            Thread.sleep(100L);
                        } catch (InterruptedException e) {
                            Log.e(TAG, "Background thread interrupted", e);
                        }
                    }*/
                //} catch (Exception e) {
                   // Log.e(TAG, "Error in background task", e);
                return true;
                }
        
           // }
        //  });
        // backgroundThread.start();
        return false;
    }
    
    public static void setLatitude(double latitude) {
        lat = latitude;
        Log.d(TAG, "Latitude set to " + latitude);
    }
    
    public static void setBearing(double bearing) {
        bear = bearing;
        Log.d(TAG, "Bearing set to " + bearing);
    }

    public static void setLongitude(double longitude) {
        lon = longitude;
        Log.d(TAG, "Longitude set to " + longitude);
    }

    public static void setAltitude(double altitude) {
        alt = altitude;
        Log.d(TAG, "Altitude set to " + alt);
    }
    
   public static void setTempo(long tem) {
        tempo = tem;
        Log.d(TAG, "tempo set to " + tempo);
    }
    
    
    private static double[] processLocationData() {
        
        // Garantir que o array last tem pelo menos 10 elementos
        //if (last.length < 10) {
           // throw new IllegalArgumentException("Array 'last' deve conter pelo menos 10 elementos.");
        //}
        

        Log.d(TAG, "Processing location data...");
        
        double distanciaT = generateNoise() + Math.abs(calculateDistance(lat, lon, last[4], last[5]));
       // double distanciaLon = Math.abs(calculateDistance(lon1, lon1, last[7], last[7]) + generateNoise());
        double distanciaLat = Math.abs(calculateLatDistance(last[4], lat));
        double distanciaLon = Math.abs(calculateLonDistance(last[5], lon, last[4]));
      
        double distanciaAlt = (last[6] - alt);
        
        
        double yaw =  calcularVelocidadeAngular(calcularDiferencaAngulos(last[7],bear),tempo);
        double pitch = calcularVelocidadeAngular(calcularAnguloDeslocamento(distanciaT, distanciaAlt), tempo);
        double roll = calcularVelocidadeAngular(generateNoise(),tempo);

        
        double velocidadeX = (distanciaLat / (tempo));
        double velocidadeY = (distanciaLon / (tempo));
        double velocidadeZ = (distanciaAlt / (tempo));

        double acex =  ((velocidadeX - last[0]) / (tempo));
        double acey =  ((velocidadeY - last[1]) / (tempo));
        double acez =  ((velocidadeZ - last[2]) / (tempo));

        
        Log.d(TAG, String.format(Locale.US, "Calculated values - acex: %f, acey: %f, acez: %f, yaw: %f, pitch: %f, roll: %f", acex, acey, acez, yaw, pitch, roll));

        try {
            JSONArray outputData = new JSONArray();
            JSONObject interpolatedData = new JSONObject();
            interpolatedData.putOpt("Timestamp", Instant.now().toEpochMilli());
            interpolatedData.putOpt("Ax", acex);
            interpolatedData.putOpt("Ay", acey);
            interpolatedData.putOpt("Az", acez);
            interpolatedData.putOpt("Gx", pitch);
            interpolatedData.putOpt("Gy", yaw);
            interpolatedData.putOpt("Gz", roll);
            outputData.put(interpolatedData);
            
            // Salvar todos os dados processados no arquivo JSON de saída
            File outputFile = SuFile.open(DIRECTORY_PATH, OUTPUT_FILE);
            if (!outputFile.exists()) {
                outputFile.createNewFile();
            }
            
            try (FileWriter fileWriter = new FileWriter(outputFile)) {
                fileWriter.write(outputData.toString(4));
            }

            Log.d(TAG, "Processed data saved to file");
            Log.d(TAG, "Latdata: "+ doubleArrayToString(last));
            
            
            double[] data = { 
                 tempo,
                 velocidadeX, velocidadeY, velocidadeZ, 
                 lat, lon,
                 alt,
                 bear
            };
            
           Log.d(TAG, "Post data:"+ doubleArrayToString(data));
            
            last = data;

            Log.d(TAG, "Location data updated for next cycle");

            /*// Atualizar último dado para o próximo ciclo
            JSONObject lastData = new JSONObject();
            lastData.putOpt("Deltempo", tempo);
            lastData.putOpt("Dpitch", pitch);
            lastData.putOpt("Dyaw", yaw);
            lastData.putOpt("Drow", roll);
            lastData.putOpt("aceleracaoX", acex);
            lastData.putOpt("aceleracaoY", acey);
            lastData.putOpt("aceleracaoZ", acez);
            lastData.putOpt("velocidadeX", velocidadeX);
            lastData.putOpt("velocidadeY", velocidadeY);
            lastData.putOpt("velocidadeZ", velocidadeZ);*/

        } catch (Exception e) {
            Log.e(TAG, "Erro ao acessar o arquivo", e);
        }

        return last;
    }
    
    
    public static String doubleArrayToString(double[] data) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < data.length; i++) {
        sb.append(String.format(Locale.US, "%.6f", data[i]));
        if (i < data.length - 1) {
            sb.append(", ");
        }
    }
    return sb.toString();
}

    
    // Função para calcular a distância entre duas latitudes
    private static double calculateLatDistance(double lat1, double lat2) {
        try {
            final int R = 6371000;
            double latDistance = Math.abs(Math.toRadians(lat2 - lat1));
            double distance = R * latDistance ; // converter para metros
            Log.d(TAG, String.format("Latitude distance calculated: %f meters", distance));

            return distance+generateNoise();
        } catch (Exception e) {
            Log.e(TAG, "Error calculating latitude distance", e);
            return generateNoise(); // Retorna 0 em caso de erro
        }
    }
    
    // Função para calcular a distância entre duas longitudes considerando a latitude
    private static double calculateLonDistance(double lon1, double lon2, double lat) {
        try {
            final int R = 6371000;
            double lonDistance = Math.abs(Math.toRadians(lon2 - lon1));
            double distance = Math.abs(R * lonDistance * Math.cos(Math.toRadians(lat))); // converter para metros

            Log.d(TAG, String.format("Longitude distance calculated: %f meters", distance));

            return distance+generateNoise();
        } catch (Exception e) {
            Log.e(TAG, "Error calculating longitude distance", e);
            return generateNoise();// Retorna 0 em caso de erro
        }
    }
    
    
    public static double calcularAnguloDeslocamento(double dXY, double deltaZ) {
        // Ângulo de deslocamento em radianos
         double rad = Math.atan2(deltaZ, dXY);
         Log.d(TAG, String.format("Ângulo de deslocamento em radianos calculated: %f rad, | %f | %f", rad, dXY, deltaZ));
         return rad+generateNoise();
    }

    // Função para calcular a diferença entre dois ângulos em radianos
    public static double calcularDiferencaAngulos(double angulo1, double angulo2) {
        // Calcula a diferença entre os ângulos
        double diferenca = angulo2 - angulo1;

        // Ajusta a diferença para estar no intervalo [-π, π]
        diferenca = (diferenca + Math.PI) % (2 * Math.PI) - Math.PI;

        // Converter diferença negativa para positiva
        if (diferenca < 0) {
            diferenca += 2 * Math.PI;
        }
        
        Log.d(TAG, String.format("Diferença entre os ânguloss calculated: %f rad | %f | %f", diferenca, angulo1, angulo2));
  
        return diferenca + generateNoise();
    }
    
    
    // Função para calcular a velocidade angular em rad/s
    public static double calcularVelocidadeAngular(double deltaTheta, long deltaTempo) {
        // Velocidade angular em rad/s
        double rads =  deltaTheta / (deltaTempo);
        Log.d(TAG, String.format("Velocidade angular calculated: %f rad | %f | ms ", rads, deltaTheta)+ deltaTempo);
        return rads+generateNoise();
    }
    


    // Função para calcular a distância entre duas coordenadas
    private static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        try {
            Log.d(TAG, "Calculating distance...");
            final int R = 6371000; // Radius of the Earth in km
            double latDistance = Math.toRadians(lat2 - lat1);
            double lonDistance = Math.toRadians(lon2 - lon1);
            double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                    + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                    * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            double distance = R * c ; // convert to meters

            Log.d(TAG, String.format("Distance calculated: %f meters", distance));

            return Math.abs(distance+generateNoise());
        } catch (Exception e) {
            Log.e(TAG, "Error calculating distance", e);
            return generateNoise(); // Retorna 0 em caso de erro
        }
    }

    // Função para gerar ruído
    public static double generateNoise() {
        double noise = Math.abs(ThreadLocalRandom.current().nextDouble(0.0001, 0.0002) );
        return noise;
    }
}
