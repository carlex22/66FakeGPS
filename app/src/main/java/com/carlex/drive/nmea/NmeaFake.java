package com.carlex.drive.nmea;



import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.location.OnNmeaMessageListener;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import java.io.DataOutputStream;


public class NmeaFake implements IXposedHookLoadPackage, IXposedHookZygoteInit {
    private static final String TAG = "NmeaFake";
    public static double latitude;
    public static double longitude;
    public static float accuracy;
    public static double altitude;
    public static float speed;
    public static float bearing;
    public static long time;
    public static String NMEA;

    private Context moduleContext;

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        XposedBridge.log("NmeaFake module initialized in Zygote");
        
        //Conceder permissão WRITE_SECURE_SETTINGS ao aplicativo
        try {
            Process su = Runtime.getRuntime().exec("su");
            DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());

            outputStream.writeBytes("pm grant com.carlex.drive android.permission.WRITE_SECURE_SETTINGS\n");
            outputStream.flush();

            outputStream.writeBytes("exit\n");
            outputStream.flush();
            outputStream.close();

            int suExitValue = su.waitFor();
            if (suExitValue == 0) {
                XposedBridge.log("Permission WRITE_SECURE_SETTINGS granted to com.carlex.drive");
            } else {
                XposedBridge.log("Failed to grant WRITE_SECURE_SETTINGS permission");
            }
        } catch (Exception e) {
            XposedBridge.log("Error granting WRITE_SECURE_SETTINGS permission: " + e.getMessage());
        }
    }

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (lpparam.packageName.equals("com.carlex.drive") || lpparam.packageName.equals("com.google.android.apps.location.gps.gnsslogger")) {
            XposedBridge.log("NmeaFake module loaded for package: " + lpparam.packageName);

            
            try {
                final Class<?> activityClass = XposedHelpers.findClass("android.app.Activity", lpparam.classLoader);

                XposedHelpers.findAndHookMethod(activityClass, "onCreate", Bundle.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Context context = ((Activity) param.thisObject).getApplicationContext();
                        if (moduleContext == null) {
                            moduleContext = context.createPackageContext("com.carlex.drive", Context.CONTEXT_IGNORE_SECURITY);
                        }
                        // Grant the permission
                        XposedHelpers.callMethod(context, "grantUriPermission", "com.carlex.drive", Settings.Global.CONTENT_URI, PackageManager.PERMISSION_GRANTED);
                        updateSystemSettings(moduleContext);
                    }
                });
            } catch (Exception e) {
                XposedBridge.log("Error in NmeaFake module: " + e.getMessage());
            }

            ////// Nmea 
            try {
                // Hook para o método addNmeaListener
                XposedHelpers.findAndHookMethod(LocationManager.class, "addNmeaListener", OnNmeaMessageListener.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        final OnNmeaMessageListener originalListener = (OnNmeaMessageListener) param.args[0];

                        OnNmeaMessageListener hookedListener = new OnNmeaMessageListener() {
                            @Override
                            public void onNmeaMessage(String message, long timestamp) {
                                // Log.i(TAG, "Original NMEA message: " + message + " " + timestamp);
                                
                                if (NMEA != null) {
                                    message = NMEA;
                                } else { 
                                    message = message + "66"; 
                                }
                                
                                timestamp = time / 3;
                                
                                // Passe a mensagem modificada
                                originalListener.onNmeaMessage(message, timestamp);
                                Log.i(TAG, "Hooked NMEA message passed: " + message + " " + timestamp);
                            }
                        };
                        param.args[0] = hookedListener;
                        // Log.i(TAG, "Hooked addNmeaListener");
                    }
                });
            } catch (Throwable t) {
                Log.e(TAG, "Error hooking addNmeaListener: " + t);
            }
            /////////////
        }
    }

    private void updateSystemSettings(Context context) {
        try {
            ContentResolver resolver = context.getContentResolver();

            latitude = Double.parseDouble(Settings.Global.getString(resolver, "latitude"));
            longitude = Double.parseDouble(Settings.Global.getString(resolver, "longitude"));
            accuracy = Float.parseFloat(Settings.Global.getString(resolver, "accuracy"));
            altitude = Double.parseDouble(Settings.Global.getString(resolver, "altitude"));
            speed = Float.parseFloat(Settings.Global.getString(resolver, "speed"));
            bearing = Float.parseFloat(Settings.Global.getString(resolver, "bearing"));
            time = Long.parseLong(Settings.Global.getString(resolver, "time"));
            NMEA = Settings.Global.getString(resolver, "NMEA");

            XposedBridge.log("System settings updated with simulated location data");
        } catch (Exception e) {
            XposedBridge.log("Error updating system settings in NmeaFake module: " + e.getMessage());
        }
    }
}
