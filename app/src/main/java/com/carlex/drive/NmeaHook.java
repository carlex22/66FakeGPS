package com.carlex.drive;

import android.content.Context;
import android.content.res.Resources;
import android.location.LocationManager;
import android.location.OnNmeaMessageListener;
import android.util.Log;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class NmeaHook implements IXposedHookLoadPackage {
    private static final String TAG = "NMEAHook";
    private static final String NMEA = "$GPGGA,123519,4807.038,N,01131.000,E,1,08,0.9,545.4,M,46.9,M,,";
    private static Long Timestamp = 123456789L;
    private Context systemContext;

    public void setSystemContext(Context context) {
        this.systemContext = context;
    }

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        try {
            if (!isPackageInScope(lpparam.packageName)) {
               // return;
            }

            XposedBridge.log("Hooking package: " + lpparam.packageName);

            XposedHelpers.findAndHookMethod(LocationManager.class, "addNmeaListener", OnNmeaMessageListener.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    final OnNmeaMessageListener originalListener = (OnNmeaMessageListener) param.args[0];
                    try {
                        OnNmeaMessageListener hookedListener = new OnNmeaMessageListener() {
                            @Override
                            public void onNmeaMessage(String message, long timestamp) {
                                // Valor original
                                Log.i(TAG, "Original: " + message + " " + timestamp);
                                // Modifique a mensagem NMEA 
                                message = NMEA;
                                timestamp = Timestamp;

                                // Passe a mensagem modificada
                                originalListener.onNmeaMessage(message, timestamp);
                                Log.i(TAG, "Hooked NMEA message: " + message + " " + timestamp);
                            }
                        };
                        param.args[0] = hookedListener;
                        Log.i(TAG, "Hooked addNmeaListener");
                    } catch (Throwable t) {
                        Log.e(TAG, "Error: " + t);
                        t.printStackTrace();
                    }
                }
            });
        } catch (Throwable t) {
            Log.e(TAG, "Error: " + t);
            t.printStackTrace();
        }
    }

    private boolean isPackageInScope(String packageName) {
        try {
            if (systemContext == null) {
                Log.e(TAG, "System context is null");
                return false;
            }
            Resources res = systemContext.getResources();
            String[] scope = res.getStringArray(res.getIdentifier("scope", "array", systemContext.getPackageName()));

            for (String pkg : scope) {
                if (packageName.equals(pkg)) {
                    return true;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in isPackageInScope: " + e.getMessage(), e);
        }
        return false;
    }
}

