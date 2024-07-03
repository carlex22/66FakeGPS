package com.carlex.drive;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import android.util.Log;

public class MainHook1 implements IXposedHookLoadPackage {

    private static final String TAG = "MainHook";
    
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        Log.d(TAG, "Target app loaded: " + lpparam.packageName);
    
        // Carregar as preferências do módulo
        XSharedPreferences pref = new XSharedPreferences("com.carlex.drive", "com.carlex.drive_preferences");
        if (pref.hasFileChanged()) {
            pref.makeWorldReadable();
            pref.reload();
        }
    
        // Ler os valores armazenados nas preferências
        double latitude = (double) pref.getFloat("latitude", 0f);
        double longitude = (double) pref.getFloat("longitude", 0f);
        double altitude = (double) pref.getFloat("altitude", 0f);
        double speed = (double) pref.getFloat("speed", 0f);
    
        Log.d(TAG, "Latitude: " + latitude);
        Log.d(TAG, "Longitude: " + longitude);
        Log.d(TAG, "Altitude: " + altitude);
        Log.d(TAG, "Speed: " + speed);
    }

}
