package com.carlex.drive;

import android.os.Bundle;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.CellIdentityCdma;
import android.util.DisplayMetrics;
import android.widget.LinearLayout;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.topjohnwu.superuser.Shell;
import com.topjohnwu.superuser.io.SuFile;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;  // Adicionado para capturar InvocationTargetException
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoCdma;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.topjohnwu.superuser.Shell;
import com.topjohnwu.superuser.io.SuFile;

public class PreferencesActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1;

    private static final String TAG = "PreferencesActivity";
    private static final String DIRECTORY_PATH = "/storage/emulated/0/carlex/";
    private static final String PREFS_FILE_NAME = "preferences.json";
    private static final String XPOSED_PREFS_FILE_NAME = "xposed_prefs.json";
    private static final String XPOSED_DIRECTORY_PATH = "/data/user_de/0/com.android.xposed.installer/conf/";

    private LinearLayout preferencesContainer;
    private Button buttonSave;
    private Map<String, EditText> editTextMap;
    private Map<String, String> defaultValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        preferencesContainer = findViewById(R.id.preferences_container);
        buttonSave = findViewById(R.id.button_save);
        editTextMap = new HashMap<>();
        defaultValues = new HashMap<>();

        Shell.su("pm grant " + getPackageName() + " android.permission.READ_PHONE_STATE").exec();
        Shell.su("pm grant " + getPackageName() + " android.permission.ACCESS_WIFI_STATE").exec();
        Shell.su("pm grant " + getPackageName() + " android.permission.BLUETOOTH").exec();
        Shell.su("pm grant " + getPackageName() + " android.permission.BLUETOOTH_ADMIN").exec();
        Shell.su("pm grant " + getPackageName() + " android.permission.INTERNET").exec();
        Shell.su("pm grant " + getPackageName() + " android.permission.ACCESS_NETWORK_STATE").exec();
        Shell.su("pm grant " + getPackageName() + " android.permission.READ_EXTERNAL_STORAGE").exec();
        Shell.su("pm grant " + getPackageName() + " android.permission.WRITE_EXTERNAL_STORAGE").exec();
        Shell.su("pm grant " + getPackageName() + " android.permission.ACCESS_FINE_LOCATION").exec();
        Shell.su("pm grant " + getPackageName() + " android.permission.ACCESS_COARSE_LOCATION").exec();
        Shell.su("pm grant " + getPackageName() + " android.permission.NETWORK_SETTINGS").exec();
        Shell.su("pm grant " + getPackageName() + " android.permission.READ_WIFI_CREDENTIAL").exec();

        try {
            initializePreferences();
            loadPreferencesFromFile();
        } catch (Exception e) {
            Log.e(TAG, "Error during onCreate: " + e.getMessage(), e);
        }

        buttonSave.setOnClickListener(view -> {
            try {
                savePreferences();
            } catch (Exception e) {
                Log.e(TAG, "Error saving preferences: " + e.getMessage(), e);
            }
        });
    }

    private void initializePreferences() {
        Log.d(TAG, "Initializing preferences...");
        File file = SuFile.open(DIRECTORY_PATH, PREFS_FILE_NAME);
        if (!file.exists()) {
            Log.d(TAG, "Preferences file does not exist. Creating new preferences file...");
            try {
                Map<String, Class<?>> classes = new HashMap<>();
                classes.put("android.telephony.CellInfoGsm", CellInfoGsm.class);
                classes.put("android.telephony.CellInfoGsm", CellIdentityCdma.class);
                classes.put("android.telephony.TelephonyManager", TelephonyManager.class);
              //  classes.put("android.net.wifi.WifiManager", WifiManager.class);
              //)/  classes.put("android.bluetooth.BluetoothAdapter", BluetoothAdapter.class);
                //classes.put("android.util.DisplayMetrics", DisplayMetrics.class);

                JSONObject jsonObject = new JSONObject();

                for (Map.Entry<String, Class<?>> entry : classes.entrySet()) {
                    String className = entry.getKey();
                    Class<?> cls = entry.getValue();
                  //  Object instance = getInstance(cls);
                    Method[] methods = cls.getDeclaredMethods();

                    for (Method method : methods) {
                        if (isGetter(method)) {
                            String methodName = method.getName();
                            try {
                                Object returnValue = method.invoke(methodName);
                                String typeName = method.getReturnType().getSimpleName();
                                String valueString = convertToString(returnValue);
                                String value =  valueString;

                                if (value != null && !value.isEmpty()) {
                                    String chave = className + "." + methodName;
                                    String  t = method.getReturnType().getCanonicalName();
                                    JSONObject valueObject = new JSONObject();
                                    valueObject.put(t, value);
                                    jsonObject.put(chave, valueObject);
                           
                                    
                                    Log.d(TAG, "Added preference: " + chave + " = " + value);
                                }
                            } catch (InvocationTargetException e) {
                                Throwable cause = e.getCause();
                                if (cause instanceof SecurityException) {
                                    Log.e(TAG, "SecurityException invoking method: " + methodName + " for class: " + className, e);
                                } else {
                                    Log.e(TAG, "Error invoking method: " + methodName + " for class: " + className, e);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error invoking method: " + methodName + " for class: " + className, e);
                            }
                        }
                    }
                }

                JSONArray jsonArray = new JSONArray();
                jsonArray.put(jsonObject);

                Shell.su("mkdir -p " + DIRECTORY_PATH).exec();
                Shell.su("chmod 777 " + DIRECTORY_PATH).exec();
                Log.d(TAG, "Created and set permissions for directory: " + DIRECTORY_PATH);

                try (FileWriter writer = new FileWriter(file)) {
                    writer.write(jsonArray.toString());
                    Shell.su("chmod 666 " + file.getAbsolutePath()).exec();
                    Log.i(TAG, "Preferences initialized and saved to: " + file.getAbsolutePath());
                } catch (IOException e) {
                    Log.e(TAG, "Error writing preferences to file: " + e.getMessage());
                }

            } catch (Exception e) {
                Log.e(TAG, "Error initializing preferences: " + e.getMessage(), e);
            }
        }
    }

    
    private String convertToString(Object obj) {
        if (obj == null) {
            return "null";
        }
        if (obj instanceof Iterable) {
            
            return null;
        }
        if (obj.getClass().isArray()) {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for (int i = 0; i < java.lang.reflect.Array.getLength(obj); i++) {
                sb.append(convertToString(java.lang.reflect.Array.get(obj, i))).append(", ");
            }
            if (sb.length() > 1) {
                sb.setLength(sb.length() - 2); // remove trailing comma and space
            }
            sb.append("]");
            return sb.toString();
        }
        return obj.toString();
    }

    private boolean isGetter(Method method) {
        return method.getName().startsWith("get") && method.getParameterTypes().length == 0 && !void.class.equals(method.getReturnType());
    }



    private void loadPreferencesFromFile() {
        Log.d(TAG, "Loading preferences from file...");
        File file = SuFile.open(DIRECTORY_PATH, PREFS_FILE_NAME);
        if (!file.exists()) {
            Log.i(TAG, "Preferences file does not exist");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }

            JSONArray jsonArray = new JSONArray(content.toString());
            if (jsonArray.length() > 0) {
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                Iterator<String> keys = jsonObject.keys();

                while (keys.hasNext()) {
                    String key = keys.next();
                    String value = jsonObject.getString(key);

                    TextView textView = new TextView(this);
                    textView.setText(key);
                    preferencesContainer.addView(textView);

                    EditText editText = new EditText(this);
                    editText.setText(value);
                    editTextMap.put(key, editText);
                    preferencesContainer.addView(editText);

                    Log.i(TAG, "Loaded preference from file: " + key + " with value: " + value);
                }
            }
        } catch (IOException | JSONException e) {
            Log.e(TAG, "Error loading preferences from file: " + e.getMessage(), e);
        }
    }

    private void savePreferences() {
        Log.d(TAG, "Saving preferences...");
        try {
            JSONArray jsonArray = new JSONArray();
            JSONObject jsonObject = new JSONObject();

            for (Map.Entry<String, EditText> entry : editTextMap.entrySet()) {
                String key = entry.getKey();
                EditText editText = entry.getValue();
                String value = editText.getText().toString();
                jsonObject.put(key, value);
                Log.i(TAG, "Saved preference: " + key + " with value: " + value);
            }

            jsonArray.put(jsonObject);

            Shell.su("mkdir -p " + DIRECTORY_PATH).exec();
            File dir = SuFile.open(DIRECTORY_PATH);
            File file = SuFile.open(dir, PREFS_FILE_NAME);

            try (FileWriter writer = new FileWriter(file)) {
                writer.write(jsonArray.toString());
                Shell.su("chmod 666 " + file.getAbsolutePath()).exec();
                Log.i(TAG, "Saved preferences to: " + file.getAbsolutePath());
            } catch (IOException e) {
                Log.e(TAG, "Error writing preferences to file: " + e.getMessage());
            }

            File xposedFile = SuFile.open(XPOSED_DIRECTORY_PATH, XPOSED_PREFS_FILE_NAME);
            try (FileWriter writer = new FileWriter(xposedFile)) {
                writer.write(jsonArray.toString());
                Shell.su("chmod 666 " + xposedFile.getAbsolutePath()).exec();
                Log.i(TAG, "Saved Xposed preferences to: " + xposedFile.getAbsolutePath());
            } catch (IOException e) {
                Log.e(TAG, "Error writing Xposed preferences to file: " + e.getMessage());
            }

        } catch (Exception e) {
            Log.e(TAG, "Error saving preferences: " + e.getMessage(), e);
        }
    }
}
