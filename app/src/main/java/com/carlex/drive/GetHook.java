package com.carlex.drive;

import android.app.AndroidAppHelper;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.bluetooth.BluetoothAdapter;
import android.util.DisplayMetrics;
import android.provider.Settings;
import android.os.Build;

import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class GetHook implements IXposedHookZygoteInit, IXposedHookLoadPackage {

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        //XposedBridge.log("Zygote initialized");
    }

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("com.carlex.drive")) {
            return;
        }

        //XposedBridge.log("Carlex Drive app loaded");

        XposedHelpers.findAndHookMethod(
            "com.carlex.drive.PreferencesActivity", 
            lpparam.classLoader, 
            "initializePreferences", 
            new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Class<?> cls = Class.forName("com.carlex.drive.PreferencesActivity");
                    Method initializePreferencesMethod = cls.getDeclaredMethod("initializePreferences");
                    initializePreferencesMethod.setAccessible(true);

                    Map<String, Class<?>> classes = new HashMap<>();
                    classes.put("android.os.Build", Build.class);
                    classes.put("android.provider.Settings$Secure", Settings.Secure.class);
                    classes.put("android.telephony.TelephonyManager", TelephonyManager.class);
                    classes.put("android.net.wifi.WifiManager", WifiManager.class);
                    classes.put("android.bluetooth.BluetoothAdapter", BluetoothAdapter.class);
                    classes.put("android.util.DisplayMetrics", DisplayMetrics.class);

                    JSONObject jsonObject = new JSONObject();

                    for (Map.Entry<String, Class<?>> entry : classes.entrySet()) {
                        String className = entry.getKey();
                        Class<?> clazz = entry.getValue();
                        Object instance = getInstance(clazz, param);
                        Method[] methods = clazz.getDeclaredMethods();

                        for (Method method : methods) {
                            if (isGetter(method) || isBooleanGetter(method)) {
                                String methodName = method.getName();
                                try {
                                    method.setAccessible(true);
                                    Object returnValue = method.invoke(instance);
                                    String typeName = method.getReturnType().getSimpleName();
                                    String valueString = convertToString(returnValue);
                                    String value = typeName + " " + valueString;

                                    if (value != null && !value.isEmpty()) {
                                        String chave = className + "." + methodName;
                                        jsonObject.put(chave, value);
                                        //XposedBridge.log("Added preference: " + chave + " = " + value);
                                    }
                                } catch (Exception e) {
                                    //XposedBridge.log("Error invoking method: " + methodName + " for class: " + className + " " + e.getMessage());
                                }
                            }
                        }
                    }

                    //XposedBridge.log("Preferences initialized and saved.");
                }
            }
        );

        // Ignorar verificações de permissão para métodos específicos
        ignorePermissionChecks(WifiManager.class, "getCountryCode", lpparam.classLoader);
        ignorePermissionChecks(WifiManager.class, "getCurrentNetwork", lpparam.classLoader);
        ignorePermissionChecks(WifiManager.class, "getFactoryMacAddresses", lpparam.classLoader);
        ignorePermissionChecks(WifiManager.class, "getPrivilegedConfiguredNetworks", lpparam.classLoader);
        ignorePermissionChecks(WifiManager.class, "getSoftApConfiguration", lpparam.classLoader);
        ignorePermissionChecks(DisplayMetrics.class, "getDeviceDensity", lpparam.classLoader);
    }

    private void ignorePermissionChecks(Class<?> clazz, String methodName, ClassLoader classLoader) {
        XposedHelpers.findAndHookMethod(
            clazz.getName(),
            classLoader,
            methodName,
            new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    // Ignorar verificações de permissão
                    param.setResult(null);
                }
            }
        );
    }

    private Object getInstance(Class<?> cls, XC_MethodHook.MethodHookParam param) throws Exception {
        Context context = AndroidAppHelper.currentApplication().getApplicationContext();
        if (cls.equals(WifiManager.class)) {
            return context.getSystemService(Context.WIFI_SERVICE);
        } else if (cls.equals(BluetoothAdapter.class)) {
            return BluetoothAdapter.getDefaultAdapter();
        } else if (cls.equals(TelephonyManager.class)) {
            return context.getSystemService(Context.TELEPHONY_SERVICE);
        } else if (cls.equals(DisplayMetrics.class)) {
            return context.getResources().getDisplayMetrics();
        } else {
            return cls.getDeclaredConstructor().newInstance();
        }
    }

    private String convertToString(Object obj) {
        if (obj == null) {
            return "null";
        }
        if (obj instanceof Iterable) {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for (Object item : (Iterable<?>) obj) {
                sb.append(convertToString(item)).append(", ");
            }
            if (sb.length() > 1) {
                sb.setLength(sb.length() - 2); // remove trailing comma and space
            }
            sb.append("]");
            return sb.toString();
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

    private boolean isBooleanGetter(Method method) {
        return method.getName().startsWith("is") && method.getParameterTypes().length == 0 && boolean.class.equals(method.getReturnType());
    }
}
