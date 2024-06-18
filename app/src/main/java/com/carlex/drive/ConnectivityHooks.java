package com.carlex.drive;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import java.util.HashMap;
import java.util.Map;

public class ConnectivityHooks implements IXposedHookLoadPackage {

    private static final String TAG = "ExtendedConnectivityHooks";
    private Map<String, Integer> classes;
    private Map<String, Integer> tipoDados;
    private Map<String, Object> valores;
    private Context systemContext;

    public void setSystemContext(Context context) {
        this.systemContext = context;
    }

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        try {
            if (!lpparam.packageName.equals("com.carlex.drive")) {
              //  return;
            }

            Log.d(TAG, "Package loaded: " + lpparam.packageName);

            // Inicializa as sublistas de preferências se não existirem
            initializePreferences();

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(systemContext);

            // Carregar valores das preferências
            loadPreferences(prefs);

            // Aplicar hooks a outros pacotes
            applyHooksToOtherPackages();

            // Registrar um OnSharedPreferenceChangeListener para monitorar mudanças nas preferências
            prefs.registerOnSharedPreferenceChangeListener((sharedPreferences, key) -> {
                try {
                    loadPreferences(sharedPreferences);
                    applyHooksToOtherPackages();
                    Log.d(TAG, "Preferences updated: " + key);
                } catch (Exception e) {
                    Log.e(TAG, "Error updating preferences: " + e.getMessage(), e);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in handleLoadPackage: " + e.getMessage(), e);
        }
    }

    private void initializePreferences() {
        try {
            if (classes == null) {
                classes = new HashMap<>();
                tipoDados = new HashMap<>();
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(systemContext);
                Map<String, ?> allEntries = prefs.getAll();

                // Carrega classes e tipos de dados a partir das preferências
                for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                    String key = entry.getKey();
                    if (key.startsWith("class_")) {
                        classes.put(key.replace("class_", ""), (Integer) entry.getValue());
                    } else if (key.startsWith("tipo_")) {
                        tipoDados.put(key.replace("tipo_", ""), (Integer) entry.getValue());
                    }
                }
                Log.d(TAG, "Preferences initialized: " + classes.size() + " classes and " + tipoDados.size() + " data types loaded");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in initializePreferences: " + e.getMessage(), e);
        }
    }

    private void loadPreferences(SharedPreferences prefs) {
        try {
            valores = new HashMap<>();
            Map<String, ?> allEntries = prefs.getAll();
            for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                valores.put(key, value);
                Log.d(TAG, "Loaded preference: " + key + " = " + value);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading preferences: " + e.getMessage(), e);
        }
    }

    private void applyHooksToOtherPackages() {
        try {
            XposedBridge.hookAllMethods(XposedHelpers.findClass("android.app.Application", null), "attach", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    Context context = (Context) param.args[0];
                    ClassLoader classLoader = context.getClassLoader();
                    updateHooks(classLoader);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error applying hooks to other packages: " + e.getMessage(), e);
        }
    }

    private void updateHooks(ClassLoader classLoader) {
        try {
            for (Map.Entry<String, Object> entry : valores.entrySet()) {
                String key = entry.getKey();
                Object returnValue = entry.getValue();

                String[] parts = key.split("\\.");
                int classId = Integer.parseInt(parts[0]);
                int tipoId = Integer.parseInt(parts[1]);
                String methodName = parts[2];

                String className = getKeyFromValue(classes, classId);
                String tipo = getKeyFromValue(tipoDados, tipoId);

                if (className != null && tipo != null) {
                    XposedHelpers.findAndHookMethod(className, classLoader, methodName, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            param.setResult(returnValue);
                        }
                    });
                    Log.d(TAG, "Hooked method: " + className + "." + methodName + " with return value: " + returnValue);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating hooks: " + e.getMessage(), e);
        }
    }

    private <T, E> T getKeyFromValue(Map<T, E> map, E value) {
        for (Map.Entry<T, E> entry : map.entrySet()) {
            if (entry.getValue().equals(value)) {
                return entry.getKey();
            }
        }
        return null;
    }
}

