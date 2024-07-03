package com.carlex.drive;

import android.app.AndroidAppHelper;
import android.content.Context;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import com.carlex.drive.NmeaHook.*;

public class MainHook implements IXposedHookZygoteInit, IXposedHookLoadPackage {
    private XSharedPreferences prefs;
    private static final String TAG = "MAINHook";
    
    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        // Inicialize as preferências
        prefs = new XSharedPreferences("com.carlex.drive", "com.carlex.drive_preferences");
        prefs.makeWorldReadable();
        XposedBridge.log(TAG + ": Preferences initialized");
    }

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
       // if (!lpparam.packageName.equals("com.carlex.drive")) {
       //     return;
      //  }
        XposedBridge.log(TAG + ": Handling package " + lpparam.packageName);

        // Hook para o método setNmea de SpaceManService
        XposedHelpers.findAndHookMethod(
            "com.carlex.drive.SpaceManService",
            lpparam.classLoader,
            "setNmea",
            String.class,
            new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    String nmea = (String) param.args[0];
                    XposedBridge.log(TAG + ": Intercepted xNMEA setNmea call with value: " + nmea);
                    // Atualize o valor no NmeaHook
                  //  NmeaHook.setNmea(nmea);
                }
            }
        );

        // Atualize a string nmea quando o pacote é carregado
        prefs.reload();
        String nmea = prefs.getString("nmea", "");
        XposedBridge.log(TAG + ": xLoaded xNMEA from preferences: " + nmea);

        // Atualize o valor no NmeaHook
         //NmeaHook.setNmea(nmea);
        XposedBridge.log(TAG + ": Updated xNMEAHook with nmea: " + nmea);
    }
}
