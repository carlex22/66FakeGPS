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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class SensorProcessorService extends Service {
    private static final String TAG = "SensorProcessorService";
    private static final String DIRECTORY_PATH = "/data/system/carlex/";
    private static final String INPUT_FILE = "locations.json";
    private static final String OUTPUT_FILE = "sensor.json";
    private static final double GRAVITY = 9.81;

    
    public static boolean isRunning = false;
    
    private static Thread backgroundThread;
    
    private Timer timer;
    private Handler handler;
    private static Random random;

    @Override
    public void onCreate() {
        super.onCreate();
        random = new Random();
       Log.i(TAG, "service created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (isRunning) {
           Log.e(TAG, "Fake 66 service is runnig");
            return START_STICKY;
        }
        Log.i(TAG, "service start");
        isRunning = true;
        
        startBackgroundTask();
        
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (backgroundThread != null) {
            backgroundThread.interrupt();
        }
        Log.i(TAG, "service Stop");
    }
    
    
    public static boolean isServiceRunning() {
        return isRunning;
    }
    
    private static void startBackgroundTask() {
        backgroundThread = new Thread(() -> {
            while (true) {
                    processLocationData();
           }
        });
        backgroundThread.start();
    }

   public static JSONObject lastData;
    
    private static void processLocationData() {
        try {
            // Ler dados do arquivo JSON de entrada
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
            JSONObject currentLocation = data.getJSONObject(0); 
                   
            long currentTime = Instant.now().toEpochMilli(); // Timestamp em milissegundos

            JSONObject currentData = new JSONObject();
            currentData.put("Timestamp", currentTime);
            currentData.put("Latitude", currentLocation.getDouble("Latitude"));
            currentData.put("Longitude", currentLocation.getDouble("Longitude"));
            currentData.put("Altitude", currentLocation.getDouble("Altitude"));
            currentData.put("Bearing", currentLocation.getDouble("Bearing"));

                    //  Log.d(TAG, "Current Data: " + currentData.toString());
                    
                    //Long deltaTempo = 1000l;
            
                    if (lastData == null){
                          lastData = currentData;
                    } /*else {
                           deltaTempo = (currentData.getLong("Timestamp") - lastData.getLong("Timestamp"));
                    }
            
                     if (deltaTempo < 1000l ){
                          deltaTempo = 1000l;
                    }*/
           
                    long deltaTempo = 500l; 
                    double distanciaLat = calculateDistance(currentData.getDouble("Latitude"), currentData.getDouble("Latitude"), lastData.getDouble("Latitude"), lastData.getDouble("Latitude"))+generateNoise();
                    double distanciaLon = calculateDistance(currentData.getDouble("Longitude"), currentData.getDouble("Longitude"), lastData.getDouble("Longitude"), lastData.getDouble("Longitude"))+generateNoise();
                    double distanciaAlt = currentData.getDouble("Altitude") - lastData.getDouble("Altitude")+generateNoise();
                    double velocidadeX = distanciaLat / (deltaTempo);
                    double velocidadeY = distanciaLon / (deltaTempo);
                    double velocidadeZ = distanciaAlt / (deltaTempo);
            
                    Log.d(TAG, "Last Data: " + lastData.toString());
            
             
                    int passo = 5;

                    // Calcular as componentes da aceleração e adicionar gravidade terrestre
                    double acex = (velocidadeX - lastData.optDouble("velocidadeX", 0.008))/deltaTempo;
                    double acey = (velocidadeY - lastData.optDouble("velocidadeY", 0.008))/deltaTempo;
                    double acez = (velocidadeZ - lastData.optDouble("velocidadeZ", 0.007))/deltaTempo;
            
                     double difax= ( acex -lastData.optDouble("aceleracaoX", 0.008))/passo;
                    double difay= ( acey -lastData.optDouble("aceleracaoY", 0.008))/passo;
            
                    double difaz= ( acez -lastData.optDouble("aceleracaoZ", 0.008))/passo;
            
            
                    // Calcular valores pseudo de giroscópio com base na mudança de direção
                    double deltaBearing = Math.toRadians(currentData.getDouble("Bearing") - lastData.getDouble("Bearing"))+generateNoise();
                    double Dyaw= (generateNoise()) + deltaBearing / deltaTempo; // Yaw
                    double Dpitch = (generateNoise()) + Math.atan2(distanciaAlt, Math.sqrt(Math.pow(distanciaLat, 2) + Math.pow(distanciaLon, 2))) / deltaTempo; // Pitch
                    double Drow = (generateNoise()) + Math.atan2(distanciaLon, distanciaLat) / deltaTempo; 

                    long diff_tempo = deltaTempo / passo;
            
                    // Calcular a diferença de gx, gy e gz do registro anterior ao atual
                    double diff_gx = (Dpitch - lastData.optDouble("Dpitch", 0.007)) / passo;
                    double diff_gy = ((Dyaw - lastData.optDouble("Dyaw", 0.0080)) / passo);
                    double diff_gz = ((Drow - lastData.optDouble("Drow", 0.008)) / passo);


                    double interpolated_ax = 0.0;
                    double interpolated_ay = 0.0;
                    double interpolated_az =0.0;
                    double interpolated_gx = 0.0;
                    double interpolated_gy = 0.0;
                    double interpolated_gz =0.0;
                        
                    // Loop para exibir os valores após cada 0.1 segundo em 10 passos
                    for (int step = 1; step <= passo; step++) {
                
                            try {
                          interpolated_ax = lastData.optDouble("aceleracaoX", 0.008) + (difax*step) ;
                          interpolated_ay = lastData.optDouble("aceleracaoY", 0.008) + (difay*step);
                        interpolated_az = lastData.optDouble("aceleracaoZ", 0.008) + (difaz*step);
                    
                        interpolated_ay += (GRAVITY*(interpolated_ay+interpolated_az))/interpolated_ay;
                        interpolated_az += (GRAVITY*(interpolated_ay+interpolated_az))/interpolated_az;
                 
                    Log.i(TAG, "" + interpolated_ay);
                    
                    // interpolated_ay = lastData.optDouble("aceleracaoY", 0.0080) + (diff_ay * step);
                         //  interpolated_az = lastData.optDouble("aceleracaoZ", 0.007) + (diff_az * step);
                          interpolated_gx = lastData.optDouble("Dpitch", 0.007) + (diff_gx * step);
                        interpolated_gy = lastData.optDouble("Dyaw", 0.008) + (diff_gy * step);
                         interpolated_gz = lastData.optDouble("Drow", 0.008) + (diff_gz * step);

                        // Handler para executar a exibição e salvamento a cada 0.1 segundo
                    
                                // Exibir os valores de Ax, Ay, Az após cada 0.1 segundo
                                
                                // Salvar dados interpolados no JSON de saída
                                
                                    JSONArray outputData = new JSONArray();
                                    JSONObject interpolatedData = new JSONObject();
                                    interpolatedData.put("Timestamp", Instant.now().toEpochMilli());
                                    interpolatedData.put("Ax", String.format("%.4f", interpolated_ax,3));
                                    interpolatedData.put("Ay", String.format("%.4f", interpolated_ay,3));
                                    interpolatedData.put("Az", String.format("%.4f", interpolated_az,3));
                                    interpolatedData.put("Gx", String.format("%.4f", interpolated_gx,3));
                                    interpolatedData.put("Gy", String.format("%.4f", interpolated_gy,3));
                                    interpolatedData.put("Gz", String.format("%.4f", interpolated_gz,3));
                                    outputData.put(interpolatedData);
                    
                                    Log.d(TAG, String.format("Acell:[%.4f,%.4f,%.4f] m/s²... Giro:[%.4f,%.4f,%.4f] rad/s",
                                        interpolated_ax, interpolated_ay, interpolated_az,
                                        interpolated_gx, interpolated_gy, interpolated_gz));


                                    // Salvar todos os dados processados no arquivo JSON de saída
                                    File outputFile = SuFile.open(DIRECTORY_PATH, OUTPUT_FILE);
                                    if (!outputFile.exists()) {
                                        outputFile.createNewFile();
                                    }
                            
                                    FileWriter fileWriter = new FileWriter(outputFile);
                                    fileWriter.write(outputData.toString(4));
                                    fileWriter.close();
                    
                                    try {Thread.sleep(100l);}   
                                    catch (InterruptedException e) {} 
                    
                                } catch (IOException e) {
                                    Log.e(TAG, "Erro ao acessar o arquivo: " + e.getMessage());
                                } catch (Exception e) {
                                    Log.e(TAG, "Erro ao processar os dados: " + e.getMessage());
                                }
                    }
            
                    lastData = currentData;
                    
                    // Atualizar último dado para o próximo ciclo
                    lastData.put("Deltempo", deltaTempo);
                    lastData.put("Dpitch",String.format("%.4f",interpolated_gx));
                    lastData.put("Dyaw",String.format("%.4f",interpolated_gy));
                    lastData.put("Drow",String.format("%.4f",interpolated_gz));
                    lastData.put("aceleracaoX",String.format("%.4f",interpolated_ax));
                    lastData.put("aceleracaoY",String.format("%.4f",interpolated_ay));
                    lastData.put("aceleracaoZ",String.format("%.4f",interpolated_az));
                    lastData.put("velocidadeX",String.format("%.4f",velocidadeX));
                    lastData.put("velocidadeY",String.format("%.4f",velocidadeY));
                    lastData.put("velocidadeZ",String.format("%.4f",velocidadeZ));
                
        } catch (IOException e) {
            Log.e(TAG, "Erro ao acessar o arquivo: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Erro ao processar os dados: " + e.getMessage());
        }
    }
    
    
   
    
    
    private static double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // Raio da Terra em metros
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    // Função para calcular a diferença entre dois valores
    private static double calculateDifference(double current, double previous) {
        return current - previous;
    }

    // Função para calcular a distância entre duas coordenadas
    private static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the Earth in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters
        return distance;
    }

    // Função para gerar ruído
    public static double generateNoise() {
        return 0.00111 + (0.00299 - 0.00111) * random.nextDouble();
    }
}
