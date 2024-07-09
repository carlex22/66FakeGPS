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
import java.util.LinkedList;

import com.topjohnwu.superuser.Shell;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import com.topjohnwu.superuser.io.SuFile;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import androidx.core.app.NotificationCompat;
import java.util.Queue;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class SensorProcessor  {
    private static final String TAG = "SensorProcessorService";
    private static final String DIRECTORY_PATH = "/storage/self/primary/carlex/";
    private static final String INPUT_FILE = "locations.json";
    private static final String OUTPUT_FILE = "sensor.json";
    private static final double GRAVITY = 9.81;

    public static double lat = 0.1, lon = 0.1, alt  = 0.1, bear = 0.1, speed = 0.1;
    public static double tempo = 100.0;
    
    public static double[] last = { 
                 0.001,
                 0.002, 0.003, 0.004, 
                 0.005, 0.006,
                 0.007,
                 0.008,
                0.009,
                 0.001
            };
    
    public static boolean isRunning = false;
    
    private static Thread backgroundThread;
    
    private Timer timer;
    private Handler handler;
    private static Random random;
    private static Queue<Double> yawHistory = new LinkedList<>();
    private static Queue<Double> pithHistory = new LinkedList<>();
    private static Queue<Double> rowHistory = new LinkedList<>();
    private static Queue<Double> axHistory = new LinkedList<>();
    private static Queue<Double> ayHistory = new LinkedList<>();
    private static Queue<Double> azHistory = new LinkedList<>();

    private static Queue<Double> timeQueue = new LinkedList<>();

        
    

   
    
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
      //  Log.d(TAG, "Latitude set to " + latitude);
    }
    
    public static void setBearing(double bearing) {
        bear = bearing;
      //  Log.d(TAG, "Bearing set to " + bearing);
    }

    public static void setLongitude(double longitude) {
        lon = longitude;
      //  Log.d(TAG, "Longitude set to " + longitude);
    }

    public static void setAltitude(double altitude) {
        alt = altitude;
     //   Log.d(TAG, "Altitude set to " + alt);
    }
    
   public static void setTempo(long tem) {
        tempo = (double) tem;
     //   Log.d(TAG, "tempo set to " + tempo);
    }
    
    
    public static void setSpeed(double spe) {
        speed = spe;
     //   Log.d(TAG, "tempo set to " + tempo);
    }
    
    
    private static double[] processLocationData() {
        
        // Garantir que o array last tem pelo menos 10 elementos
        //if (last.length < 10) {
           // throw new IllegalArgumentException("Array 'last' deve conter pelo menos 10 elementos.");
        //}
        
        tempo/= 100;
            
       // Log.d(TAG, "Processing location data...");
        
        double distanciaT = Math.abs(calculateDistance(lat, lon, last[4], last[5]));
       // double distanciaLon = Math.abs(calculateDistance(lon1, lon1, last[7], last[7]) + generateNoise());
        double distanciaLat =   Math.abs(calculateLatDistance(lat, last[4]));
        double distanciaLon = Math.abs(calculateLonDistance(lon, last[5], lat));
      
        double distanciaAlt = (alt-last[6]);
        Log.d(TAG, "Calculated diferencd altitude:"  + distanciaAlt);
        double anguloZ = calcularAngulo(distanciaAlt,distanciaT);
        
        double difanZ = (anguloZ - last[8]);
        
        // Garantir que a diferença seja sempre menor ou igual a 180 graus
        if (difanZ > 180.0) {
            difanZ = 360.0 - difanZ;
        }
        
        Log.d(TAG, "Calculated diferencd ang alt:"  + distanciaAlt);
    
        double angulox = calcularDiferencaAngulos( last[7], bear);
        
         
        double yaw =  calcularVelocidadeAngular(angulox,tempo);
        double roll = calcularVelocidadeAngular(grausParaRadianos(difanZ), tempo);
        double pitch = 0.0000000;

        
        
        double velocidadeX = ((distanciaLon) / (tempo));
        double velocidadeY = ((distanciaT) / (tempo));
        double velocidadeZ = (calcularHipotenusa(distanciaAlt,distanciaT)) / (tempo);

        double acex =  (9.8-calcularDistribuicao(grausParaRadianos(angulox))); //(((velocidadeX-last[1])) / (tempo));
        double acey = -3+((speed - last[9])/tempo) + calcularDistribuicao(grausParaRadianos(anguloZ-angulox));; //(((velocidadeY-last[2])) / (tempo));//+calcularDistribuicao(grausParaRadianos(anguloZ));
        double acez =  9.8-(calcularDistribuicao(grausParaRadianos(anguloZ)));//(((velocidadeZ-last[3])) / (tempo));//+(9.8-(calcularDistribuicao(grausParaRadianos(anguloZ))));

        
        Log.d(TAG, "Calculated values - acex: "+acex+", acey: "+acey+", acez: "+acez+", yaw: "+yaw+", pitch: "+pitch+", roll: "+roll);

        
        double totalSum = tempo;
        for (double time : timeQueue) {
            totalSum += time;
        }
        
        timeQueue.add(tempo);
        yawHistory.add(yaw);
        rowHistory.add(roll);
        pithHistory.add(pitch);
        axHistory.add(acex);
        ayHistory.add(acey);
        azHistory.add(acez);

        // Enquanto a soma dos registros + novo tempo for maior que 1000, remova o registro mais antigo
        if (timeQueue.size()>2){
            while (totalSum > 2.00) {
                double removedTime = timeQueue.poll(); // Remove o registro mais antigo (início da fila)
                yawHistory.poll();
                rowHistory.poll();
                pithHistory.poll();
                azHistory.poll();
                axHistory.poll();
                ayHistory.poll();
                if (removedTime > 0) {
                    totalSum -= removedTime;
                }
            }
        }
        
        
       
        
        double totalPt = calculateWeightedAverage(pithHistory);
        double totalYa = calculateWeightedAverage(yawHistory);
        double totalRol = calculateWeightedAverage(rowHistory);

        double totalax = calculateWeightedAverage(axHistory);
        double totalay = calculateWeightedAverage(ayHistory);
        double totalaz = calculateWeightedAverage(azHistory);

        
        
        try {
            JSONArray outputData = new JSONArray();
            JSONObject interpolatedData = new JSONObject();
            interpolatedData.putOpt("Timestamp", Instant.now().toEpochMilli());
            interpolatedData.putOpt("Ax", formatarParaQuatroDecimais(totalax+generateNoise()));
            interpolatedData.putOpt("Ay", formatarParaQuatroDecimais(totalay+generateNoise()));
            interpolatedData.putOpt("Az", formatarParaQuatroDecimais(totalaz+generateNoise()));
            interpolatedData.putOpt("Gz", formatarParaQuatroDecimais(totalPt+generateNoise()));
            interpolatedData.putOpt("Gy", formatarParaQuatroDecimais(totalYa+generateNoise()));
            interpolatedData.putOpt("Gx", formatarParaQuatroDecimais(totalRol+generateNoise()));
            outputData.put(interpolatedData);
            
            // Salvar todos os dados processados no arquivo JSON de saída
            File file = SuFile.open(DIRECTORY_PATH, OUTPUT_FILE);
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(outputData.toString());
                Shell.su("chmod 777 " + file.getAbsolutePath()).exec();
                Log.d(TAG, "sensor Saved  data to: " + file.getAbsolutePath());
            } catch (IOException e) {
                Log.d(TAG, "Error writing cell data to file: " + e.getMessage());
                return last;
            }

            //Log.d(TAG, "Processed data saved to file");
          //  Log.d(TAG, "Latdata: "+ doubleArrayToString(last));
            
            
            double[] data = { 
                 tempo,
                 velocidadeX, velocidadeY, velocidadeZ, 
                 lat, lon,
                 alt,
                 bear ,
                 anguloZ,  
                  speed
            };
            
            
            
                
           Log.d(TAG, "Post data:"+ doubleArrayToString(data));
            
            last = data;

            Log.d(TAG, "---------- end sensor cycle-----------");

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

    
    
    private static double calculateWeightedAverage(Queue<Double> history) {
        double total = 0;
        double weightSum = 0;
        int index = history.size();
        for (double value : history) {
            total += value * index;
            weightSum += index;
            index--;
        }
        return total / weightSum;
    }
    
    
    
   public static double calcularDistribuicao(double x) {
        // Converte o ângulo x de graus para radianos
        double xRadianos = grausParaRadianos(x);
        // Calcula o peso usando a função cosseno
        double peso = Math.cos(xRadianos);
        // Multiplica pelo valor base de 9.8
        return 9.8 * peso;
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
    
    public static double formatarParaQuatroDecimais(double numero) {
        BigDecimal bd = new BigDecimal(Double.toString(numero));
        bd = bd.setScale(3, RoundingMode.HALF_UP); // Arredonda para 4 casas decimais
        return bd.doubleValue();
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
         double rad = grausParaRadianos(Math.atan2(deltaZ, dXY));
         Log.d(TAG, String.format("Ângulo de deslocamento em radianos calculated: %f rad, | %f | %f", rad, dXY, deltaZ));
         return rad;
    }

    
    
    public static double grausParaRadianos(double graus) {
        return graus * Math.PI / 180.0;
    }

    // Método para calcular a diferença entre dois ângulos em radianos
    public static double calcularDiferencaAngulos(double angulo1, double angulo2) {
        // Calcula a diferença bruta
        double delta = grausParaRadianos(angulo2) - grausParaRadianos(angulo1);
        
        // Ajusta para o intervalo [-π, π]
       /* if (delta > Math.PI) {
            delta -= 2 * Math.PI;
        } else if (delta < -Math.PI) {
            delta += 2 * Math.PI;
        }*/
        
        return delta;
    }
    
    
    /*
    // Função para calcular a diferença entre dois ângulos em radianos
    public static double calcularDiferencaAngulos(double angulo1, double angulo2) {
        // Calcula a diferença entre os ângulos
        double diferenca = angulo2 - angulo1;

        // Ajusta a diferença para estar no intervalo [-π, π]
     //   diferenca = (diferenca + Math.PI) % (2 * Math.PI) - Math.PI;

        // Converter diferença negativa para positiva
     // /  if (diferenca < 0) {
        //    diferenca += 2 * Math.PI;
     //   }
        
        Log.d(TAG, String.format("Diferença entre os ânguloss calculated: %f rad | %f | %f", diferenca, angulo1, angulo2));
  
        return diferenca + generateNoise();
    }*/
    
    
   // Método para calcular a hipotenusa
    public static double calcularHipotenusa(double cateto1, double cateto2) {
        return Math.sqrt(Math.pow(cateto1, 2) + Math.pow(cateto2, 2));
    }

    // Método para calcular um dos ângulos agudos em graus
    public static double calcularAngulo(double catetoOposto, double catetoAdjacente) {
        double angulo = Math.toDegrees(Math.atan(catetoOposto / catetoAdjacente));
        Log.d(TAG, String.format("angulo: %f graus | %f | %f", angulo, catetoOposto, catetoAdjacente));
        return angulo;
    }
    
    
    // Função para calcular a velocidade angular em rad/s
    public static double calcularVelocidadeAngular(double deltaTheta, double deltaTempo) {
        // Velocidade angular em rad/s
        double rads =  deltaTheta / (deltaTempo);
        Log.d(TAG, String.format("Velocidade angular calculated: %f rad | %f | ms ", rads, deltaTheta)+ deltaTempo);
        return rads;
    }
    


    // Função para calcular a distância entre duas coordenadas
    private static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        try {
           // Log.d(TAG, "Calculating distance...");
            final int R = 6371000; // Radius of the Earth in km
            double latDistance = Math.toRadians(lat2 - lat1);
            double lonDistance = Math.toRadians(lon2 - lon1);
            double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                    + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                    * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            double distance = R * c ; // convert to meters

         //   Log.d(TAG, String.format("Distance calculated: %f meters", distance));

            return Math.abs(distance);
        } catch (Exception e) {
            Log.e(TAG, "Error calculating distance", e);
            return generateNoise(); // Retorna 0 em caso de erro
        }
    }

    // Função para gerar ruído
    public static double generateNoise() {
        double noise = Math.abs(ThreadLocalRandom.current().nextDouble(0.001, 0.005) );
        double noise2 = Math.abs(ThreadLocalRandom.current().nextDouble(0.001, 0.005) );
     
        return noise-noise2;
    }
}
