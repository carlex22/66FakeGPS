package com.carlex.drive;

import android.content.Context;
import android.location.GnssMeasurementsEvent;
import android.location.GnssMeasurement;
import android.location.LocationManager;
import android.location.GnssClock;
import android.util.Log;
import androidx.annotation.NonNull;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class GnssMeasurementsHook implements IXposedHookLoadPackage {
    private static final String TAG = "GnssMeasurementsHook";
    private static Context context;

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        try {
            //XposedBridge.log("Hooking package: " + lpparam.packageName);
            XposedHelpers.findAndHookMethod(LocationManager.class, "registerGnssMeasurementsCallback", GnssMeasurementsEvent.Callback.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    final GnssMeasurementsEvent.Callback originalCallback = (GnssMeasurementsEvent.Callback) param.args[0];
                    try {
                        GnssMeasurementsEvent.Callback hookedCallback = new GnssMeasurementsEvent.Callback() {
                            @Override
                            public void onGnssMeasurementsReceived(@NonNull GnssMeasurementsEvent event) {
                                //Log.i(TAG, "Original GNSS measurements received: " + event);
                                GnssMeasurementsEvent fakeEvent = createFakeGnssMeasurementsEvent(event);
                                originalCallback.onGnssMeasurementsReceived(fakeEvent);
                                //Log.i(TAG, "Hooked GNSS measurements: " + fakeEvent);
                            }

                            @Override
                            public void onStatusChanged(int status) {
                                //Log.i(TAG, "GNSS measurements status changed: " + status);
                                originalCallback.onStatusChanged(status);
                            }
                        };
                        param.args[0] = hookedCallback;
                        //Log.i(TAG, "Hooked registerGnssMeasurementsCallback");
                    } catch (Throwable t) {
                        //Log.e(TAG, "Error: " + t);
                        t.printStackTrace();
                    }
                }
            });
        } catch (Throwable t) {
            //Log.e(TAG, "Error: " + t);
            t.printStackTrace();
        }
    }

    public GnssMeasurementsEvent createFakeGnssMeasurementsEvent(GnssMeasurementsEvent originalEvent) {
        try {
            //Log.i(TAG, "Creating fake GNSS measurements event...");

            // Create a fake GnssClock using reflection
            GnssClock gnssClock = createGnssClock();

            // Create a list of fake GnssMeasurement using reflection
            ArrayList<GnssMeasurement> fakeMeasurements = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                GnssMeasurement measurement = createGnssMeasurement();
                if (measurement != null) {
                    fakeMeasurements.add(measurement);
                }
            }

            // Create the fake GnssMeasurementsEvent using reflection
            Constructor<GnssMeasurementsEvent> constructor = GnssMeasurementsEvent.class.getDeclaredConstructor(GnssClock.class, Iterable.class);
            constructor.setAccessible(true);
            GnssMeasurementsEvent fakeEvent = constructor.newInstance(gnssClock, fakeMeasurements);
            return fakeEvent;
        } catch (Exception e) {
            //Log.e(TAG, "Error creating fake GNSS measurements event: " + e.getMessage(), e);
            return originalEvent;
        }
    }

    private GnssClock createGnssClock() {
        try {
            Constructor<GnssClock> constructor = GnssClock.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            GnssClock gnssClock = constructor.newInstance();

            Method setLeapSecond = GnssClock.class.getDeclaredMethod("setLeapSecond", int.class);
            setLeapSecond.setAccessible(true);
            setLeapSecond.invoke(gnssClock, 18);

            Method setTimeNanos = GnssClock.class.getDeclaredMethod("setTimeNanos", long.class);
            setTimeNanos.setAccessible(true);
            setTimeNanos.invoke(gnssClock, 242411000000L);

            Method setFullBiasNanos = GnssClock.class.getDeclaredMethod("setFullBiasNanos", long.class);
            setFullBiasNanos.setAccessible(true);
            setFullBiasNanos.invoke(gnssClock, -1402774495033002260L);

            Method setBiasNanos = GnssClock.class.getDeclaredMethod("setBiasNanos", double.class);
            setBiasNanos.setAccessible(true);
            setBiasNanos.invoke(gnssClock, -0.4395635426044464);

            Method setBiasUncertaintyNanos = GnssClock.class.getDeclaredMethod("setBiasUncertaintyNanos", double.class);
            setBiasUncertaintyNanos.setAccessible(true);
            setBiasUncertaintyNanos.invoke(gnssClock, 112542.763);

            Method setDriftNanosPerSecond = GnssClock.class.getDeclaredMethod("setDriftNanosPerSecond", double.class);
            setDriftNanosPerSecond.setAccessible(true);
            setDriftNanosPerSecond.invoke(gnssClock, 30.716);

            Method setDriftUncertaintyNanosPerSecond = GnssClock.class.getDeclaredMethod("setDriftUncertaintyNanosPerSecond", double.class);
            setDriftUncertaintyNanosPerSecond.setAccessible(true);
            setDriftUncertaintyNanosPerSecond.invoke(gnssClock, 76.248);

            Method setHardwareClockDiscontinuityCount = GnssClock.class.getDeclaredMethod("setHardwareClockDiscontinuityCount", int.class);
            setHardwareClockDiscontinuityCount.setAccessible(true);
            setHardwareClockDiscontinuityCount.invoke(gnssClock, 21);

            return gnssClock;
        } catch (Exception e) {
            //Log.e(TAG, "Error creating GnssClock: " + e.getMessage(), e);
            return null;
        }
    }

    private GnssMeasurement createGnssMeasurement() {
        try {
            Constructor<GnssMeasurement> constructor = GnssMeasurement.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            GnssMeasurement measurement = constructor.newInstance();

            Method setSvid = GnssMeasurement.class.getDeclaredMethod("setSvid", int.class);
            setSvid.setAccessible(true);
            setSvid.invoke(measurement, 15);

            Method setConstellationType = GnssMeasurement.class.getDeclaredMethod("setConstellationType", int.class);
            setConstellationType.setAccessible(true);
            setConstellationType.invoke(measurement, 3);

            Method setTimeOffsetNanos = GnssMeasurement.class.getDeclaredMethod("setTimeOffsetNanos", double.class);
            setTimeOffsetNanos.setAccessible(true);
            setTimeOffsetNanos.invoke(measurement, 0.0);

            Method setState = GnssMeasurement.class.getDeclaredMethod("setState", int.class);
            setState.setAccessible(true);
            setState.invoke(measurement, 49359);

            Method setReceivedSvTimeNanos = GnssMeasurement.class.getDeclaredMethod("setReceivedSvTimeNanos", long.class);
            setReceivedSvTimeNanos.setAccessible(true);
            setReceivedSvTimeNanos.invoke(measurement, 81519375679486L);

            Method setReceivedSvTimeUncertaintyNanos = GnssMeasurement.class.getDeclaredMethod("setReceivedSvTimeUncertaintyNanos", long.class);
            setReceivedSvTimeUncertaintyNanos.setAccessible(true);
            setReceivedSvTimeUncertaintyNanos.invoke(measurement, 35);

            Method setCn0DbHz = GnssMeasurement.class.getDeclaredMethod("setCn0DbHz", float.class);
            setCn0DbHz.setAccessible(true);
            setCn0DbHz.invoke(measurement, 25.9f);

            Method setPseudorangeRateMetersPerSecond = GnssMeasurement.class.getDeclaredMethod("setPseudorangeRateMetersPerSecond", float.class);
            setPseudorangeRateMetersPerSecond.setAccessible(true);
            setPseudorangeRateMetersPerSecond.invoke(measurement, -58.836f);

            // Add more fields as necessary

            return measurement;
        } catch (Exception e) {
            //Log.e(TAG, "Error creating GnssMeasurement: " + e.getMessage(), e);
            return null;
        }
    }

    public void setSystemContext(Context context) {
        this.context = context;
    }
}
