package com.carlex.drive;

import android.location.Location;
import android.util.Log;
import androidx.dynamicanimation.animation.FlingAnimation;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import org.json.JSONArray;
import org.json.JSONObject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import android.content.*;
import android.content.Context;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.preference.*;

import android.hardware.GeomagneticField;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;

public class DataLocation {
    private  final String TAG = "DATALocation";
    private  double time;
    private  Random random;
    
    private Queue<Double> yawHistory = new LinkedList<>();
    private Queue<Double> pithHistory = new LinkedList<>();
    private Queue<Double> rowHistory = new LinkedList<>();
    private Queue<Double> axHistory = new LinkedList<>();
    private Queue<Double> ayHistory = new LinkedList<>();
    private Queue<Double> azHistory = new LinkedList<>();
    private Queue<Double> timeQueue = new LinkedList<>();
    private Queue<Double> magXHistory = new LinkedList<>();
    private Queue<Double> magYHistory = new LinkedList<>();
    private Queue<Double> magZHistory = new LinkedList<>();

    
   public SharedPreferences prefs;
    
    
    // Construtor público para inicializar o objeto e definir o limitador de registros do histórico
    public DataLocation() {
        this.random = new Random();
        
    }

    
    
   private  double generateNoise(double valorOriginal) {
        Random random = new Random();
        double ruido = (random.nextDouble() * 2 - 1) * 0.001;
        if(valorOriginal + ruido == 0.0)
        return -ruido;
        
        return ruido;
    }
    

    // Método para atualizar os dados e retornar um JsonArray
    public void updateData(Context context, Queue<fLocation> dataLoc) {
        
        if (context ==null || dataLoc ==null) return;
        //Log.i(TAG, "+++++++inicio novo ciclo updatadataSensor");

      // Location[] locs = (locations) dataLoc;
        
        if (dataLoc.size() < 3) {
            //Log.i(TAG, "ship dataloc<3");
            return;
        }
        
        fLocation antLocation = (fLocation) dataLoc.toArray()[0];
        fLocation lastLocation =  (fLocation) dataLoc.toArray()[1];
        fLocation currentLocation = (fLocation) dataLoc.toArray()[2];
       
        //Log.i(TAG, "antLocation: " + antLocation.toString() + "lastLocation: " + lastLocation.toString() + ", currentLocation: " + currentLocation.toString());
       
        
            
        // Calcular os valores de aceleração
        double deltaTimeSec = (double) currentLocation.getDeltaTime(lastLocation)/1000.0;
            
        //Log.i(TAG, "deltaTimeSec: " + deltaTimeSec);
       
        
        if (deltaTimeSec>=0.001){
            
            
           Calendar calendar = Calendar.getInstance();

            // Inicializar GeomagneticField com os parâmetros
            GeomagneticField geomagneticField = new GeomagneticField(
                (float) currentLocation.getLatitude(),
                (float) currentLocation.getLongitude(),
                (float) currentLocation.getAltitude(),
                calendar.getTimeInMillis()
            );
            
                
            // Obtenha os componentes do campo geomagnético
            float declination = geomagneticField.getDeclination(); // Declinação em graus
            float inclination = geomagneticField.getInclination(); // Inclinação em graus
            float horizontalStrength = geomagneticField.getHorizontalStrength(); // Força horizontal em nT
            float fieldStrength = geomagneticField.getFieldStrength(); // Força do campo em nT
    
            // Converta os valores para microteslas (µT)
            float horizontalStrengthMicroTesla = horizontalStrength / 1000;
            float fieldStrengthMicroTesla = fieldStrength / 1000;
    
            // Calcule os componentes XYZ
            // Inclinação (I) e declinação (D) devem ser convertidas para radianos
            double inclinationRad = Math.toRadians(inclination);
            double declinationRad = Math.toRadians(declination);
    
            // Componente X (norte)
            float x = (float) (horizontalStrengthMicroTesla * Math.cos(inclinationRad));
            // Componente Y (leste)
            float y = (float) (horizontalStrengthMicroTesla * Math.sin(inclinationRad) * Math.sin(declinationRad));
            // Componente Z (vertical)
            float z = (float) (horizontalStrengthMicroTesla * Math.sin(inclinationRad) * Math.cos(declinationRad));
                
                
            // Ajuste o bearing para calcular a direção do norte
            double bearingRad = Math.toRadians(currentLocation.getBearing());
            float adjustedX = (float) (x * Math.cos(bearingRad) - y * Math.sin(bearingRad));
            float adjustedY = (float) (x * Math.sin(bearingRad) + y * Math.cos(bearingRad));
            
            
            double axy = (( (currentLocation.getDeltaSpeedXY(lastLocation)) - (lastLocation.getDeltaSpeedXY(antLocation))) / 2) / deltaTimeSec;
            double ayaw = (currentLocation.getThetaSpeedXY(lastLocation)) / deltaTimeSec;
            double ay = (( (currentLocation.getThetaSpeedXY(lastLocation)) - (lastLocation.getThetaSpeedXY(antLocation))) / 2) / deltaTimeSec;  // Converter em m/s²
            double az =  ((   (currentLocation.getDeltaSpeedZ(lastLocation)) - (lastLocation.getDeltaSpeedZ(antLocation))) / 2) / deltaTimeSec;
            double aroll = ( currentLocation.getThetaSpeedZ(lastLocation)) / deltaTimeSec; 
       
            double magX = (double) adjustedX;
            double magY = (double)adjustedY;
            double magZ = (double) z;
            
            
          
            /*Log.i(TAG, String.format("Log calculo sensores aceleracao giroscopio -> " +
                    "aceleracao linear m/s² [: velocidade1: %f velocidade2: %f, tempo: %f , resultado %f ]" +
                    "aceleracap yaw r/s2 [:  velocidade1: %f velocidade2: %f, tempo: %f , resultado %f ]" +
                    "aceleracao vertical m/s² [: ((%f + %f) / 2) / %f = %f] " +
                    "aceleracao roll r/s² [: ((%f - %f) / 2) / %f = %f ]",
                    currentLocation.getDeltaSpeedXY(lastLocation), lastLocation.getDeltaSpeedXY(antLocation), deltaTimeSec, axy,
                    currentLocation.getThetaSpeedXY(lastLocation), lastLocation.getThetaSpeedXY(antLocation), deltaTimeSec, ayaw, ayaw, ay,
                    currentLocation.getDeltaSpeedZ(lastLocation), lastLocation.getDeltaSpeedZ(antLocation), deltaTimeSec, axy,
                    currentLocation.getThetaSpeedZ(lastLocation), lastLocation.getThetaSpeedZ(antLocation), deltaTimeSec, aroll));*/

            
            timeQueue.add(deltaTimeSec);
            yawHistory.add(ayaw+generateNoise(ayaw));
            rowHistory.add(aroll+generateNoise(aroll));
            pithHistory.add(generateNoise(0.000));
            axHistory.add(axy+generateNoise(axy));
            ayHistory.add(ay+generateNoise(ay));
            azHistory.add(az);
            magXHistory.add(magX );
            magYHistory.add(magY );
            magZHistory.add(magZ);
            
            
            double totalSum = 0;
            for (double time : timeQueue) {
                totalSum += time;
            }
            
            // Enquanto a soma dos registros + novo tempo for maior que 1000, remova o registro mais antigo
            if (timeQueue.size()>=2){
                while (totalSum > 1) {
                    double removedTime = timeQueue.poll(); // Remove o registro mais antigo (início da fila)
                    yawHistory.poll();
                    rowHistory.poll();
                    pithHistory.poll();
                    azHistory.poll();
                    axHistory.poll();
                    ayHistory.poll();
                    magXHistory.poll();
                    magYHistory.poll();
                    magZHistory.poll();
                    if (removedTime > 0) {
                        totalSum -= removedTime;
                    }
                }
            }
        
        
         
            // Criar JsonObject com os valores e adicionar ruído
            JsonObject jData = new JsonObject();
            double finalAxy = calculateWeightedAverage(axHistory);
            double finalAy = calculateWeightedAverage(ayHistory);
            double finalAz = calculateWeightedAverage(azHistory);
            double finalYaw = calculateWeightedAverage(yawHistory);
            double finalRoll = calculateWeightedAverage(rowHistory);
            double finalPitch = calculateWeightedAverage(pithHistory);
            double finalMagX = calculateWeightedAverage(magXHistory);
            double finalMagY = calculateWeightedAverage(magYHistory);
            double finalMagZ = calculateWeightedAverage(magZHistory);

            
            
            try {
                JSONArray outputData = new JSONArray();
                JSONObject interpolatedData = new JSONObject();
                interpolatedData.putOpt("Timestamp", Instant.now().toEpochMilli());
                interpolatedData.putOpt("Ax",formatarParaQuatroDecimais(finalAxy));
                interpolatedData.putOpt("Ay",formatarParaQuatroDecimais(finalAy)+8.3);
                interpolatedData.putOpt("Az",formatarParaQuatroDecimais(finalAz)+2.5);
                interpolatedData.putOpt("Gy",formatarParaQuatroDecimais(finalYaw));
                interpolatedData.putOpt("Gz",formatarParaQuatroDecimais(finalRoll));
                interpolatedData.putOpt("Gx",formatarParaQuatroDecimais(generateNoise(0.000)));
                interpolatedData.put("MagX", formatarParaQuatroDecimais (finalMagX));
                interpolatedData.put("MagY",formatarParaQuatroDecimais(finalMagY));
                interpolatedData.put("MagZ",formatarParaQuatroDecimais(finalMagZ));
                
                outputData.put(interpolatedData);
                
                compartilharDados(context, outputData);
                
                
                
                SharedPreferences prefs = context.getSharedPreferences("FakeSensor", Context.MODE_WORLD_READABLE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putLong("Timestamp", Instant.now().toEpochMilli());
                editor.putFloat("Ax", (float) formatarParaQuatroDecimais(finalAxy/9.8));
                editor.putFloat("Ay", (float)formatarParaQuatroDecimais(finalAy/9.8));
                editor.putFloat("Az",(float) formatarParaQuatroDecimais(finalAz/9.8));
                editor.putFloat("Gy",(float)formatarParaQuatroDecimais(finalYaw));
                editor.putFloat("Gz",(float) formatarParaQuatroDecimais(finalRoll));
                editor.putFloat("Gx", (float)formatarParaQuatroDecimais(generateNoise(0.000)));
                editor.putFloat("MagX",(float)formatarParaQuatroDecimais (finalMagX));
                editor.putFloat("MagY",(float)formatarParaQuatroDecimais(finalMagY));
                editor.putFloat("MagZ",(float)formatarParaQuatroDecimais(finalMagZ));
                editor.apply();
              
                //Log.i(TAG, "JsonArray criado e retornado: " + outputData); 
                //Log.i(TAG, "----- fim ciclo updatadataSensor");
                //SaveSensorJson.savefile(outputData);
            } catch (Exception e) {
                //Log.e(TAG, "Erro ao criar json", e);
            }
            
        }//else 
       //Log.e(TAG, "Ship deltatime low");
    }
    
    public static double formatarParaQuatroDecimais(double numero) {
        BigDecimal bd = new BigDecimal(Double.toString(numero));
        bd = bd.setScale(3, RoundingMode.HALF_UP); 
        if (bd.doubleValue() ==0.0) return 0.001;
        // Arredonda para 4 casas decimais
        return bd.doubleValue();
    }
    
    private double calculateWeightedAverage(Queue<Double> history) {
       if (history.size()>0){
            double total = 0;
            double weightSum = 0;
            int index =  history.size();
            for (double value : history) {
                total += value * index;
                weightSum += index;
                index--;
            }
            double to = total / weightSum;
            to += generateNoise(to);
            return to;
        }
        else return generateNoise(0.000);
    }
    
    
    private void compartilharDados(Context context, JSONArray jsonArray) {
        ContentValues values = new ContentValues();
        values.put("json_data", jsonArray.toString());
        context.getContentResolver().insert(DataContentProvider.CONTENT_URI, values);
    }
}
