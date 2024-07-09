package com.carlex.drive;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import java.util.List;
import java.io.IOException;
import java.util.ArrayList;
import com.topjohnwu.superuser.io.SuFile;
import com.topjohnwu.superuser.io.SuFile;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.Random;
import java.util.Timer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.TimerTask;
import org.json.JSONArray;
import org.json.JSONObject;
import java.math.BigDecimal;


import android.content.Context;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.Queue;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class SensorHook implements IXposedHookLoadPackage, IXposedHookZygoteInit {
    private static final String TAG = "SensorInterceptor";
    private static final String DIRECTORY_PATH = "/storage/self/primary/carlex/";
    private static final String INPUT_FILE = "sensor.json";

    private static Context systemContext;
    private SensorEventListener originalAccelListener;
    private SensorEventListener originalGyroListener;

    private Queue<float[]> accelHistory = new LinkedList<>();
    private Queue<float[]> gyroHistory = new LinkedList<>();

     @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        XposedBridge.log(TAG + ": initialized in Zygote");
        systemContext = (Context) XposedHelpers.callMethod(
            XposedHelpers.callStaticMethod(
                XposedHelpers.findClass("android.app.ActivityThread", null), "currentActivityThread"
            ), "getSystemContext"
        );
    }

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        XposedBridge.hookAllMethods(SensorManager.class, "registerListener", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                SensorEventListener originalListener = (SensorEventListener) param.args[0];
                Sensor sensor = (Sensor) param.args[1];

                if (sensor.getType() == Sensor.TYPE_ACCELEROMETER || sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                    SensorEventListener proxyListener = new SensorEventListener() {
                        @Override
                        public void onSensorChanged(SensorEvent event) {
                            try {
                                // Read sensor data from file
                                float[] modifiedValues = readSensorDataFromFile(sensor.getType());
                                if (modifiedValues != null) {
                                    // Process and smooth the values
                                    float[] smoothedValues = processSensorData(sensor.getType(), modifiedValues);

                                    // Create a new SensorEvent with smoothed values
                                    SensorEvent modifiedEvent = createSensorEvent(sensor, smoothedValues, event.accuracy, event.timestamp);

                                    // Pass the modified event to the original listener
                                    originalListener.onSensorChanged(modifiedEvent);
                                } else {
                                    // Pass the original event to the original listener if no modification
                                    originalListener.onSensorChanged(event);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error in onSensorChanged", e);
                            }
                        }

                        @Override
                        public void onAccuracyChanged(Sensor sensor, int accuracy) {
                            try {
                                originalListener.onAccuracyChanged(sensor, accuracy);
                            } catch (Exception e) {
                                Log.e(TAG, "Error in onAccuracyChanged", e);
                            }
                        }
                    };

                    param.args[0] = proxyListener;

                    if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                        originalAccelListener = originalListener;
                    } else if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                        originalGyroListener = originalListener;
                    }
                } else {
                    Log.d(TAG, "Blocking sensor type: " + sensor.getType());
                    param.setResult(null);
                }
            }
        });

      /*  XposedBridge.hookAllMethods(SensorManager.class, "unregisterListener", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                SensorEventListener listener = (SensorEventListener) param.args[0];
                Sensor sensor = (Sensor) param.args[1];

                if ((sensor.getType() == Sensor.TYPE_ACCELEROMETER && listener == originalAccelListener) ||
                    (sensor.getType() == Sensor.TYPE_GYROSCOPE && listener == originalGyroListener)) {
                    param.setResult(null);
                }
            }
        });*/

        XposedBridge.hookAllMethods(SensorManager.class, "getSensorList", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                List<Sensor> originalList = (List<Sensor>) param.getResult();
                List<Sensor> filteredList = new ArrayList<>();

                for (Sensor sensor : originalList) {
                    if (sensor.getType() == Sensor.TYPE_ACCELEROMETER || sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                        filteredList.add(sensor);
                    }
                }

                param.setResult(filteredList);
            }
        });
    }

    
    public static float[] ace = {0f, 0f, 0f};
    public static float[] gir = {0f, 0f, 0f};
    
    private float[] readSensorDataFromFile(int sensorType) {
       
       try {
            Process process = Runtime.getRuntime().exec(new String[]{"su", "-c", "chmod 777 "+DIRECTORY_PATH+ INPUT_FILE});
            process.waitFor();
        } catch (Exception e) {
           Log.e(TAG, "Error setting file permissions: " + e.getMessage());
        }
       
        File inputFile = SuFile.open(DIRECTORY_PATH, INPUT_FILE);
        if (!inputFile.exists()) {
            if (sensorType == Sensor.TYPE_ACCELEROMETER) 
               return ace;
            else  if (sensorType == Sensor.TYPE_ACCELEROMETER) 
               return gir;
            else
               return null;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }

            JSONArray jsonArray = new JSONArray(content.toString());
            if (jsonArray.length() > 0) {
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                if (sensorType == Sensor.TYPE_ACCELEROMETER) { 
                    float ax = (float) jsonObject.optDouble("Ax");
                    float ay = (float) jsonObject.optDouble("Ay");
                    float az = (float) jsonObject.optDouble("Az");
                    ace = new float[]{ax, ay, az};
                    return ace;
                } else if (sensorType == Sensor.TYPE_GYROSCOPE) {
                    gir  = new float[]{
                            (float) jsonObject.optDouble("Gx"),
                            (float) jsonObject.optDouble("Gy"),
                            (float) jsonObject.optDouble("Gz")
                    };
                    return gir;
                }
            }
        } catch (IOException | org.json.JSONException e) {
            Log.e(TAG, "Error reading sensor data from file", e);
        }
        return null;
    }

    private float[] processSensorData(int sensorType, float[] newValues) {
        Queue<float[]> history = sensorType == Sensor.TYPE_ACCELEROMETER ? accelHistory : gyroHistory;

        // Add new values to the history queue
        if (history.size() >= 2) {
            history.poll(); // Remove the oldest entry if the queue is full
        }
        history.add(newValues);

        // Calculate the average of the historical values
        float[] smoothedValues = new float[3];
        for (float[] values : history) {
            for (int i = 0; i < 3; i++) {
                smoothedValues[i] += values[i];
                smoothedValues[i] = round(smoothedValues[i], 3); // Arredondar para 3 casas decimais
            }
        }
        for (int i = 0; i < 3; i++) {
            smoothedValues[i] /= history.size();
        }
        
        // Calculate the threshold
        float[] threshold = new float[3];
        for (int i = 0; i < 3; i++) {
            threshold[i] = smoothedValues[i] * 1.25f;
        }

        // Ignore outliers
        for (int i = 0; i < 3; i++) {
            if (Math.abs(newValues[i] - smoothedValues[i]) > threshold[i]) {
                return smoothedValues; // Return the smoothed values if the new value is an outlier
            }
        }

        return newValues; // Return the new values if they are not outliers
    }

    private SensorEvent createSensorEvent(Sensor sensor, float[] values, int accuracy, long timestamp) {
        try {
            Constructor<?> constructor = SensorEvent.class.getDeclaredConstructor(int.class);
            constructor.setAccessible(true);
            SensorEvent event = (SensorEvent) constructor.newInstance(values.length);
            Field sensorField = SensorEvent.class.getDeclaredField("sensor");
            Field valuesField = SensorEvent.class.getDeclaredField("values");
            Field accuracyField = SensorEvent.class.getDeclaredField("accuracy");
            Field timestampField = SensorEvent.class.getDeclaredField("timestamp");
            sensorField.setAccessible(true);
            valuesField.setAccessible(true);
            accuracyField.setAccessible(true);
            timestampField.setAccessible(true);

            sensorField.set(event, sensor);
            valuesField.set(event, values);
            accuracyField.set(event, accuracy);
            timestampField.set(event, timestamp);

            return event;
        } catch (Exception e) {
            Log.e(TAG, "Error creating SensorEvent", e);
            return null;
        }
    }
    
   private float round(float value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.floatValue();
    }
    
}