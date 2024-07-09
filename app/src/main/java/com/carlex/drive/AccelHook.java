package com.carlex.drive;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import com.topjohnwu.superuser.io.SuFile;
import java.lang.reflect.Array;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class AccelHook implements IXposedHookLoadPackage, IXposedHookZygoteInit {
    private static final String TAG = "AccelHook";
    private static final String DIRECTORY_PATH = "/storage/emulated/0/carlex/";
    private static final String INPUT_FILE = "sensor.json";
    public static float[] data = new float[]{0.01f, 0.01f, 0.01f};

    private SensorEventListener originalAccelListener;
    private float[] lastValidValues = null;

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        Log.d(TAG, "initZygote: Zygote initialized");
    }

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        XposedBridge.hookAllMethods(SensorManager.class, "registerListener", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                SensorEventListener originalListener = (SensorEventListener) param.args[0];
                Sensor sensor = (Sensor) param.args[1];

                if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    SensorEventListener proxyListener = new SensorEventListener() {
                        @Override
                        public void onSensorChanged(SensorEvent event) {
                            try {
                                float[] modifiedValues = readSensorDataFromFile();
                                if (modifiedValues != null) {
                                    lastValidValues = modifiedValues;
                                    SensorEvent modifiedEvent = createSensorEvent(sensor, modifiedValues, event.accuracy, event.timestamp);
                                    originalListener.onSensorChanged(modifiedEvent);
                                } else if (lastValidValues != null) {
                                    SensorEvent modifiedEvent = createSensorEvent(sensor, lastValidValues, event.accuracy, event.timestamp);
                                    originalListener.onSensorChanged(modifiedEvent);
                                } else {
                                    originalListener.onSensorChanged(event);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error in onSensorChanged", e);
                                if (lastValidValues != null) {
                                    SensorEvent modifiedEvent = createSensorEvent(sensor, lastValidValues, event.accuracy, event.timestamp);
                                    originalListener.onSensorChanged(modifiedEvent);
                                } else {
                                    originalListener.onSensorChanged(event);
                                }
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
                    originalAccelListener = originalListener;
                } else {
                    Log.d(TAG, "Blocking sensor type: " + sensor.getType());
                    param.setResult(null);
                }
            }
        });

        
        XposedBridge.hookAllMethods(SensorManager.class, "unregisterListener", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                SensorEventListener listener = (SensorEventListener) param.args[0];
                Sensor sensor = (Sensor) param.args[1];

              if (sensor.getType() == Sensor.TYPE_ACCELEROMETER && listener == originalAccelListener) {
                param.setResult(null);
              }
            }
        });

        
        
        XposedBridge.hookAllMethods(SensorManager.class, "getSensorList", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                List<Sensor> originalList = (List<Sensor>) param.getResult();
                List<Sensor> filteredList = new ArrayList<>();

                for (Sensor sensor : originalList) {
                    if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                        filteredList.add(sensor);
                    }
                }

                param.setResult(filteredList);
            }
        });
    }

    private float[] readSensorDataFromFile() {
    File inputFile = SuFile.open(DIRECTORY_PATH, INPUT_FILE);
    if (!inputFile.exists() || inputFile.length() == 0) {
       // Log.e(TAG, "Input file does not exist or is empty");
        return data;
    }
    try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
        StringBuilder content = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            content.append(line);
        }

        if (content.length() == 0) {
            //Log.e(TAG, "Input file is empty");
            return data;
        }

        JSONArray jsonArray = new JSONArray(content.toString());
        if (jsonArray.length() > 0) {
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            data =  new float[]{
                (float) jsonObject.getDouble("Ax"),
                        (float) jsonObject.getDouble("Ay"),
                        (float) jsonObject.getDouble("Az")
            };
                return data;
        }
    } catch (IOException | org.json.JSONException e) {
      //  Log.e(TAG, "Error reading sensor data from file", e);
            return data;
    }
    return data;
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
}
