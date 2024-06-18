
package com.carlex.drive;

import android.content.Context;
import android.content.res.Resources;
import android.location.GnssStatus;
import android.location.LocationManager;
import android.util.Log;
import androidx.annotation.NonNull;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.location.Location;
import java.util.Map;
import com.carlex.drive.SpaceMan;

import java.lang.reflect.Field;

public class GnssStatusHook implements IXposedHookLoadPackage {
    private static final String TAG = "GnssStatusHook";
    private static 	Context systemContext;
    private static List<SpaceMan.MyGpsSatellite> gpsSatellites;
    protected SpaceMan spaceman;
    
    public static void setSystemContext(Context context) {
	    Log.d(TAG, "SET GPSSATDATA");
            systemContext = context;
    }

    

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        try {
            if (!isPackageInScope(lpparam.packageName)) {
           //     return;
            }

            XposedBridge.log("Hooking package: " + lpparam.packageName);

            XposedHelpers.findAndHookMethod(LocationManager.class, "registerGnssStatusCallback", GnssStatus.Callback.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    final GnssStatus.Callback originalCallback = (GnssStatus.Callback) param.args[0];
                    try {
                        GnssStatus.Callback hookedCallback = new GnssStatus.Callback() {
                            @Override	
                            public void onStarted() {
                                Log.i(TAG, "GNSS started");
                                originalCallback.onStarted();
                            }

                            @Override
                            public void onStopped() {
                                Log.i(TAG, "GNSS stopped");
                                originalCallback.onStopped();
                            }

                            @Override
                            public void onFirstFix(int ttffMillis) {
                                Log.i(TAG, "GNSS first fix: " + ttffMillis);
                                originalCallback.onFirstFix(ttffMillis);
                            }

                            @Override
                            public void onSatelliteStatusChanged(@NonNull GnssStatus status) {
                                // Log original status
                                Log.i(TAG, "Original GNSS status: " + status);
                                // Modify the GNSS status
                                GnssStatus fakeStatus = createFakeGnssStatus(status);
                                // Pass the modified status to the original callback
                                originalCallback.onSatelliteStatusChanged(fakeStatus);
                                Log.i(TAG, "Hooked GNSS status: " + fakeStatus);
                            }
                        };
                        param.args[0] = hookedCallback;
                        Log.i(TAG, "Hooked registerGnssStatusCallback");
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
    
    private static void updateSpaceMan(Location location) {
        SpaceMan.setGroundStationPosition(location);
        // SpaceMan.calculatePositions(); // Uncomment if needed
        Log.d(TAG, "Max C/N0: " + SpaceMan.getMaxCn0());
        Log.d(TAG, "Mean C/N0: " + SpaceMan.getMeanCn0());
        Log.d(TAG, "Satellite Count: " + SpaceMan.getSatelliteCount());
    }

    

    private boolean isPackageInScope(String packageName) {
       /* try {
            if (systemContext == null) {
                Log.e(TAG, "System context is null");
                return true;
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
        }*/
        return true;
    }

    public GnssStatus createFakeGnssStatus(GnssStatus originalStatus) {
        try {
            // Log the creation start
            Log.i(TAG, "Creating fake GNSS status...");

            //int satelliteCount = originalStatus.getSatelliteCount();
            GnssStatus.Builder builder = new GnssStatus.Builder();
	    gpsSatellites = SpaceMan.gpsSatellites;              

	    Log.i(TAG, "Satellite count: " + gpsSatellites.size());
	    if (gpsSatellites.size() == 0) {
		    gpsSatellites = SpaceMan.getGpsSatellites();
		    Log.i(TAG, "Get Satellite count: " + gpsSatellites.size());
	    }

	    // Itere sobre a lista de satélites e processe os dados
            for (SpaceMan.MyGpsSatellite satellite : gpsSatellites) {
            	Log.i(TAG, "Satellite add: " + satellite.getPrn());
            	// Acessar propriedades do satellite
            	float azimuth = satellite.getAzimuth();
            	float elevation = satellite.getElevation();
            	int prn = satellite.getPrn();
            	float snr = satellite.getSnr();
            	double range = satellite.getRange();
            	boolean hasAlmanac = satellite.getHasAlmanac();
            	boolean hasEphemeris = satellite.getHasEphemeris();
            	boolean usedInFix = satellite.getUsedInFix();

            	builder.addSatellite(
		prn, GnssStatus.CONSTELLATION_GPS, snr, azimuth, 
		elevation, hasAlmanac, hasEphemeris, usedInFix, 
		false, (float) range, true, 0.2f);
              } 

	      return builder.build();
        } catch (Exception e) {
            Log.e(TAG, "Error creating fake GNSS status: " + 
	    e.getMessage(), e);
            return originalStatus;
        }
    }
}



/*package com.carlex.drive;

import android.content.Context;
import android.content.res.Resources;
import android.location.GnssStatus;
import android.location.LocationManager;
import android.util.Log;
import androidx.annotation.NonNull;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class GnssStatusHook implements IXposedHookLoadPackage {
    private static final String TAG = "GnssStatusHook";
    private Context systemContext;

    public void setSystemContext(Context context) {
        this.systemContext = context;
    }

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        try {
            if (!isPackageInScope(lpparam.packageName)) {
                //return;
            }

            XposedBridge.log("Hooking package: " + lpparam.packageName);

            XposedHelpers.findAndHookMethod(LocationManager.class, "registerGnssStatusCallback", GnssStatus.Callback.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    final GnssStatus.Callback originalCallback = (GnssStatus.Callback) param.args[0];
                    try {
                        GnssStatus.Callback hookedCallback = new GnssStatus.Callback() {
                            @Override
                            public void onStarted() {
                                Log.i(TAG, "GNSS started");
                                originalCallback.onStarted();
                            }

                            @Override
                            public void onStopped() {
                                Log.i(TAG, "GNSS stopped");
                                originalCallback.onStopped();
                            }

                            @Override
                            public void onFirstFix(int ttffMillis) {
                                Log.i(TAG, "GNSS first fix: " + ttffMillis);
                                originalCallback.onFirstFix(ttffMillis);
                            }

                            @Override
                            public void onSatelliteStatusChanged(@NonNull GnssStatus status) {
                                // Log original status
                                Log.i(TAG, "Original GNSS status: " + status);
                                // Modify the GNSS status
                                modifyGnssStatus(status);
                                // Pass the modified status to the original callback
                                originalCallback.onSatelliteStatusChanged(status);
                                Log.i(TAG, "Hooked GNSS status: " + status);
                            }
                        };
                        param.args[0] = hookedCallback;
                        Log.i(TAG, "Hooked registerGnssStatusCallback");
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
                return true;
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
        return true;
    }

    private void modifyGnssStatus(GnssStatus status) {
        try {
            // Map of possible fields and values to set
            Map<String, Object> fieldsToSet = new HashMap<>();
            fieldsToSet.put("mSvCount", 5);
            //fieldsToSet.put("mSvidWithFlags", new int[]{1, 2, 3, 4, 5});
            //fieldsToSet.put("mCn0DbHz", new float[]{30.0f, 35.0f, 40.0f, 45.0f, 50.0f});
            fieldsToSet.put("mElevations", new float[]{45.0f, 50.0f, 55.0f, 60.0f, 65.0f});
            fieldsToSet.put("mAzimuths", new float[]{100.0f, 110.0f, 120.0f, 130.0f, 140.0f});
            //fieldsToSet.put("mCarrierFrequencies", new float[]{1.57542f, 1.22760f, 1.17645f, 1.20714f, 1.19145f});
            //fieldsToSet.put("mConstellationTypes", new int[]{GnssStatus.CONSTELLATION_GPS, GnssStatus.CONSTELLATION_GPS, GnssStatus.CONSTELLATION_GPS, GnssStatus.CONSTELLATION_GPS, GnssStatus.CONSTELLATION_GPS});

            // Attempt to set each field
            for (Map.Entry<String, Object> entry : fieldsToSet.entrySet()) {
                try {
                    setGnssStatusField(status, entry.getKey(), entry.getValue());
                } catch (NoSuchFieldException e) {
                    Log.e(TAG, "No field " + entry.getKey() + " in class " + GnssStatus.class.getName());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error modifying GNSS status: " + e.getMessage(), e);
        }
    }

    private void setGnssStatusField(GnssStatus status, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = GnssStatus.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(status, value);
    }
}



/*package com.carlex.drive;

import android.content.Context;
import android.content.res.Resources;
import android.location.GnssStatus;
import android.location.LocationManager;
import android.util.Log;
import androidx.annotation.NonNull;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import java.lang.reflect.Field;

public class GnssStatusHook implements IXposedHookLoadPackage {
    private static final String TAG = "GnssStatusHook";
    private Context systemContext;

    public void setSystemContext(Context context) {
        this.systemContext = context;
    }

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        try {
            if (!isPackageInScope(lpparam.packageName)) {
                //return;
            }

            XposedBridge.log("Hooking package: " + lpparam.packageName);

            XposedHelpers.findAndHookMethod(LocationManager.class, "registerGnssStatusCallback", GnssStatus.Callback.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    final GnssStatus.Callback originalCallback = (GnssStatus.Callback) param.args[0];
                    try {
                        GnssStatus.Callback hookedCallback = new GnssStatus.Callback() {
                            @Override
                            public void onStarted() {
                                Log.i(TAG, "GNSS started");
                                originalCallback.onStarted();
                            }

                            @Override
            0                public void onStopped() {
                                Log.i(TAG, "GNSS stopped");
                                originalCallback.onStopped();
                            }

                            @Override
                            public void onFirstFix(int ttffMillis) {
                                Log.i(TAG, "GNSS first fix: " + ttffMillis);
                                originalCallback.onFirstFix(ttffMillis);
                            }

                            @Override
                            public void onSatelliteStatusChanged(@NonNull GnssStatus status) {
                                // Log original status
                                Log.i(TAG, "Original GNSS status: " + status);
                                // Modify the GNSS status
                                modifyGnssStatus(status);
                                // Pass the modified status to the original callback
                                originalCallback.onSatelliteStatusChanged(status);
                                Log.i(TAG, "Hooked GNSS status: " + status);
                            }
                        };
                        param.args[0] = hookedCallback;
                        Log.i(TAG, "Hooked registerGnssStatusCallback");
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
                //return false;
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

    private void modifyGnssStatus(GnssStatus status) {
        try {
            // Modify the fields of the existing GnssStatus object
            setGnssStatusField(status, "mSvCount", 5);
            setGnssStatusField(status, "mSvidWithFlags", new int[]{1, 2, 3, 4, 5});
            setGnssStatusField(status, "mCn0DbHz", new float[]{30.0f, 35.0f, 40.0f, 45.0f, 50.0f});
            setGnssStatusField(status, "mElevations", new float[]{45.0f, 50.0f, 55.0f, 60.0f, 65.0f});
            setGnssStatusField(status, "mAzimuths", new float[]{100.0f, 110.0f, 120.0f, 130.0f, 140.0f});
            setGnssStatusField(status, "mCarrierFrequencies", new float[]{1.57542f, 1.22760f, 1.17645f, 1.20714f, 1.19145f});
            setGnssStatusField(status, "mConstellationTypes", new int[]{GnssStatus.CONSTELLATION_GPS, GnssStatus.CONSTELLATION_GPS, GnssStatus.CONSTELLATION_GPS, GnssStatus.CONSTELLATION_GPS, GnssStatus.CONSTELLATION_GPS});
        } catch (NoSuchFieldException e) {
            Log.e(TAG, "Field not found: " + e.getMessage());
        } catch (IllegalAccessException e) {
            Log.e(TAG, "Illegal access: " + e.getMessage());
        }
    }

    private void setGnssStatusField(GnssStatus status, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
        try {
            Field field = GnssStatus.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(status, value);
        } catch (NoSuchFieldException e) {
            Log.e(TAG, "No field " + fieldName + " in class " + GnssStatus.class.getName());
            throw e;
        }
    }
}
*/
