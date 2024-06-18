package com.carlex.drive;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PreferencesActivity extends AppCompatActivity {

    private static final String TAG = "PreferencesActivity";

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

        try {
            initializePreferences();
            loadPreferences();
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
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (!prefs.contains("initialized")) {
            try {
                Map<String, Integer> classes = new HashMap<>();
                Map<String, Integer> tipoDados = new HashMap<>();

               /* classes.put("android.telephony.AvailableNetworkInfo", 1);
                classes.put("android.telephony.TelephonyManager", 2);
                classes.put("android.telephony.CellIdentity", 3);
                */
		classes.put("android.telephony.cdma.CdmaCellLocation", 1);
                /*classes.put("android.telephony.CellLocation", 5);
                classes.put("android.telephony.CellSignalStrength", 6);
                classes.put("android.telephony.NetworkScan", 7);
                classes.put("android.telephony.ServiceState", 8);
                classes.put("android.telephony.SignalStrength", 9);
                classes.put("android.telephony.SubscriptionInfo", 10);
                */
		//classes.put("android.os.Buildr", 1);

                tipoDados.put("Int", 1);
                tipoDados.put("String", 2);
                tipoDados.put("Double", 3);
                tipoDados.put("Float", 4);
                tipoDados.put("Boolean", 5);
                tipoDados.put("List", 6);

                List<Class<?>> classesToInspect = new ArrayList<>();
                for (String className : classes.keySet()) {
                    classesToInspect.add(getClassByName(className));
                }

                for (int i = 0; i < classesToInspect.size(); i++) {
                    Class<?> cls = classesToInspect.get(i);
                    if (cls == null) continue;

                    Method[] methods = cls.getDeclaredMethods();
                    for (Method method : methods) {
                        if (isGetter(method)) {
                            String key = cls.getSimpleName() + "." + method.getName();
                            String value;
                            try {
                                value = method.invoke(null).toString();
                                if (value == null) value = "";
                            } catch (Exception e) {
                                value = "";
                                Log.e(TAG, "Error invoking method: " + method.getName() + " for class: " + cls.getSimpleName(), e);
                            }
                            defaultValues.put(key, value);
                            Log.i(TAG, "Initialized preference: " + key + " with value: " + value);
                        }
                    }

                    Object instance = createInstance(cls);
                    if (instance != null) {
                        Method[] instanceMethods = instance.getClass().getDeclaredMethods();
                        for (Method instanceMethod : instanceMethods) {
                            if (isGetter(instanceMethod)) {
                                String key = instance.getClass().getSimpleName() + "." + instanceMethod.getName();
                                String value;
                                try {
                                    value = instanceMethod.invoke(instance).toString();
                                    if (value == null) value = "";
                                } catch (Exception e) {
                                    value = "";
                                    Log.e(TAG, "Error invoking instance method: " + instanceMethod.getName() + " for class: " + instance.getClass().getSimpleName(), e);
                                }
                                if (!defaultValues.containsKey(key)) {
                                    defaultValues.put(key, value);
                                    Log.i(TAG, "Initialized preference from instance: " + key + " with value: " + value);
                                }

                                // Adiciona a classe da instância à lista de classes para inspecionar
                                Class<?> newClass = instanceMethod.getReturnType();
                                if (!classes.containsKey(newClass.getSimpleName())) {
                                    classes.put(newClass.getSimpleName(), classes.size() + 1);
                                    classesToInspect.add(newClass);
                                    Log.i(TAG, "Added new class to inspect: " + newClass.getSimpleName());
                                }
                            }
                        }
                    }
                }

                SharedPreferences.Editor editor = prefs.edit();
                for (Map.Entry<String, String> entry : defaultValues.entrySet()) {
                    editor.putString(entry.getKey(), entry.getValue());
                }
                editor.putBoolean("initialized", true);
                editor.apply();
                Log.i(TAG, "Preferences initialized and saved");

            } catch (Exception e) {
                Log.e(TAG, "Error initializing preferences: " + e.getMessage(), e);
            }
        }
    }

    private Class<?> getClassByName(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "Class not found: " + className, e);
            return null;
        }
    }

    private Object createInstance(Class<?> cls) {
        try {
            return cls.newInstance();
        } catch (Exception e) {
            Log.e(TAG, "Error creating instance of class: " + cls.getSimpleName(), e);
            return null;
        }
    }

    private boolean isGetter(Method method) {
        if (!method.getName().startsWith("get")) return false;
        if (method.getParameterTypes().length != 0) return false;
        if (void.class.equals(method.getReturnType())) return false;
        return true;
    }

    private void loadPreferences() {
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

            Map<String, ?> allEntries = prefs.getAll();
            for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                String key = entry.getKey();
                if (key.equals("initialized")) continue; // Skip the initialized flag

                Object value = entry.getValue();

                TextView textView = new TextView(this);
                textView.setText(key);
                preferencesContainer.addView(textView);

                EditText editText = new EditText(this);
                if (value instanceof String) {
                    editText.setText((String) value);
                } else if (value instanceof Integer) {
                    editText.setText(String.valueOf(value));
                } else if (value instanceof Boolean) {
                    editText.setText(String.valueOf(value));
                } else if (value instanceof Float) {
                    editText.setText(String.valueOf(value));
                } else if (value instanceof Long) {
                    editText.setText(String.valueOf(value));
                }
                editTextMap.put(key, editText);
                preferencesContainer.addView(editText);

                Log.i(TAG, "Loaded preference: " + key + " with value: " + value);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading preferences: " + e.getMessage(), e);
        }
    }

    private void savePreferences() {
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = prefs.edit();

            for (Map.Entry<String, EditText> entry : editTextMap.entrySet()) {
                String key = entry.getKey();
                EditText editText = entry.getValue();
                String value = editText.getText().toString();

                editor.putString(key, value);
                Log.i(TAG, "Saved preference: " + key + " with value: " + value);
            }
            editor.apply();
            Log.i(TAG, "Preferences saved successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error saving preferences: " + e.getMessage(), e);
        }
    }
}

