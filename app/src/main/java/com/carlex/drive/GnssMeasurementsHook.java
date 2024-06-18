package com.carlex.drive;

import android.content.Context;
import android.location.GnssMeasurementsEvent;
import android.location.GnssMeasurement;
import android.location.GnssClock;
import android.location.LocationManager;
import android.util.Log;
import androidx.annotation.NonNull;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import android.location.GnssMeasurementsEvent;
import android.location.GnssMeasurement;
import android.location.GnssClock;
import android.location.GnssStatus;
import android.location.LocationManager;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import com.carlex.drive.SpaceMan;

//package com.carlex.drive;

import android.content.Context;
import android.location.GnssMeasurementsEvent;
import android.location.GnssMeasurement;
import android.location.GnssClock;
import android.location.LocationManager;
import android.util.Log;
import androidx.annotation.NonNull;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import com.carlex.drive.SpaceMan;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class GnssMeasurementsHook implements IXposedHookLoadPackage {
    private static final String TAG = "GnssMeasurementsHook";
    private Context systemContext;

    public void setSystemContext(Context context) {
        this.systemContext = context;
    }

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        try {
            if (!isPackageInScope(lpparam.packageName)) {
                return;
            }

            XposedBridge.log("Hooking package: " + lpparam.packageName);
            
            

            XposedHelpers.findAndHookMethod(LocationManager.class, "registerGnssMeasurementsCallback", GnssMeasurementsEvent.Callback.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    final GnssMeasurementsEvent.Callback originalCallback = (GnssMeasurementsEvent.Callback) param.args[0];
                    try {
                        GnssMeasurementsEvent.Callback hookedCallback = new GnssMeasurementsEvent.Callback() {
                            @Override
                            public void onGnssMeasurementsReceived(@NonNull GnssMeasurementsEvent event) {
                                Log.i(TAG, "Original GNSS measurements: " + event);
                                GnssMeasurementsEvent modifiedEvent = createModifiedGnssMeasurementsEvent(event);
                                originalCallback.onGnssMeasurementsReceived(modifiedEvent);
                                Log.i(TAG, "Hooked GNSS measurements: " + modifiedEvent);
                            }

                            @Override
                            public void onStatusChanged(int status) {
                                originalCallback.onStatusChanged(status);
                            }
                        };
                        param.args[0] = hookedCallback;
                        Log.i(TAG, "Hooked registerGnssMeasurementsCallback");
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
            String[] scope = systemContext.getResources().getStringArray(systemContext.getResources().getIdentifier("scope", "array", systemContext.getPackageName()));

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





    private GnssMeasurementsEvent createModifiedGnssMeasurementsEvent(GnssMeasurementsEvent originalEvent) {
        try {
            GnssClock clock = originalEvent.getClock();
            //List<GnssMeasurement> measurementList = new ArrayList<>();

          //  List<SpaceMan.MyGpsSatellite> gpsSatellites = SpaceMan.getGpsSatellites();

            /* // Itere sobre a lista de satélites e processe os dados
            for (SpaceMan.MyGpsSatellite satellite : gpsSatellites) {

               GnssMeasurement measurement = createGnssMeasurement();
                if (measurement != null) {
                    setField(measurement, "mSvid", satellite.getPrn());
                    setField(measurement, "mConstellationType", GnssStatus.CONSTELLATION_GPS);
                    setField(measurement, "mCnDbHz", satellite.getSnr());
                    setField(measurement, "mElevationDegrees", satellite.getElevation());
                    setField(measurement, "mAzimuthDegrees", satellite.getAzimuth());
                    setField(measurement, "mHasEphemerisData", satellite.getHasEphemeris());
                    setField(measurement, "mHasAlmanacData", satellite.getHasAlmanac());
                    setField(measurement, "mUsedInFix", satellite.getUsedInFix());
                    measurementList.add(measurement);
                    Log.d(TAG, "SpaceMan add");
                 }
            }

            // Use reflection to create the new GnssMeasurementsEvent
            Constructor<GnssMeasurementsEvent> constructor = GnssMeasurementsEvent.class.getDeclaredConstructor(GnssClock.class, GnssMeasurement[].class);
            constructor.setAccessible(true);
            GnssMeasurementsEvent newEvent = constructor.newInstance(clock, measurementList.toArray(new GnssMeasurement[0]));
           */
            return null;
            //return newEvent;
        } catch (Exception e) {
            Log.e(TAG, "Error creating modified GNSS measurements: " + e.getMessage(), e);
            return null;
        }
    }

    private GnssMeasurement createGnssMeasurement() {
        try {
            Constructor<GnssMeasurement> constructor = GnssMeasurement.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception e) {
            Log.e(TAG, "Error creating GnssMeasurement instance: " + e.getMessage(), e);
            return null;
        }
    }

    private void setField(Object obj, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }
}

