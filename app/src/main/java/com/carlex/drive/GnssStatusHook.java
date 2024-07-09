package com.carlex.drive;

import android.content.Context;
import android.location.GnssStatus;
import android.location.LocationManager;
import android.util.Log;
import androidx.annotation.NonNull;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.XposedHelpers;
import android.content.Context;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONException;
import com.topjohnwu.superuser.io.SuFile;
import org.json.JSONObject;

public class GnssStatusHook implements IXposedHookLoadPackage, IXposedHookZygoteInit {
    private static final String TAG = "GnssStatusHook";
    private static final String DIRECTORY_PATH = "/storage/self/primary/carlex/";
    private static JSONArray satelliteData;
    //private static final String DIRECTORY_PATH = "/storage/self/primary/carlex/";
    private static final String CELL_DATA_FILE = "satellites.json";
 
    private static Context systemContext;

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        XposedHelpers.findAndHookMethod(
                "android.app.ActivityThread",
                null,
                "systemMain",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        systemContext =
                                (Context)
                                        XposedHelpers.callStaticMethod(
                                                XposedHelpers.findClass(
                                                        "android.app.ActivityThread", null),
                                                "currentApplication");
                        // Log.d(TAG, "System context obtained");
                    }
                });
    }

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        try {
            XposedBridge.log("Hooking package: " + lpparam.packageName);
            XposedHelpers.findAndHookMethod(
                    LocationManager.class,
                    "registerGnssStatusCallback",
                    GnssStatus.Callback.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            final GnssStatus.Callback originalCallback =
                                    (GnssStatus.Callback) param.args[0];
                            try {
                                GnssStatus.Callback hookedCallback =
                                        new GnssStatus.Callback() {
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
                                            public void onSatelliteStatusChanged(
                                                    @NonNull GnssStatus status) {
                                                Log.i(TAG, "Original GNSS status: " + status);
                                                updateSatelliteDataFromJson();
                                                GnssStatus fakeStatus =
                                                        createFakeGnssStatus(status);
                                                originalCallback.onSatelliteStatusChanged(
                                                        fakeStatus);
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

    private static void updateSatelliteDataFromJson() {
        
       try {
            Process process = Runtime.getRuntime().exec(new String[]{"su", "-c", "chmod 777 "+DIRECTORY_PATH+CELL_DATA_FILE});
            process.waitFor();
        } catch (Exception e) {
           Log.e(TAG, "Error setting file permissions: " + e.getMessage());
        }
        
        
      //  File file = new File(FILE_PATH);
        
        File file = SuFile.open(DIRECTORY_PATH, CELL_DATA_FILE);
        
        if (!file.exists()) {
            Log.e(TAG, "File not found: " + CELL_DATA_FILE);
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder jsonString = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }
            satelliteData = new JSONArray(jsonString.toString());
        } catch (IOException | JSONException e) {
            Log.e(TAG, "Error reading satellite data from JSON file: ", e);
        }
    }

    public GnssStatus createFakeGnssStatus(GnssStatus originalStatus) {
        if (satelliteData == null || satelliteData.length() == 0) {
            Log.e(TAG, "Satellite data not loaded or empty");
            return originalStatus;
        }

        try {
            GnssStatus.Builder builder = new GnssStatus.Builder();
            JSONObject satelliteInfo = satelliteData.getJSONObject(0);
            JSONArray satellitesArray = satelliteInfo.getJSONArray("SatelitesData");

            for (int i = 0; i < satellitesArray.length(); i++) {
                JSONObject satelliteObject = satellitesArray.getJSONObject(i);
                String prnKey = satelliteObject.keys().next();
                JSONObject satelliteDetails = satelliteObject.getJSONObject(prnKey);

                int svid = Integer.parseInt(prnKey);
                int constellationType = GnssStatus.CONSTELLATION_GPS;
                float cn0DbHz = (float) satelliteDetails.getDouble("snr");
                float elevationDegrees = (float) satelliteDetails.getDouble("elevation");
                float azimuthDegrees = (float) satelliteDetails.getDouble("azimuth");
                boolean hasEphemerisData = satelliteDetails.getBoolean("hasEphemeris");
                boolean hasAlmanacData = satelliteDetails.getBoolean("hasAlmanac");
                boolean usedInFix = satelliteDetails.getBoolean("usedInFix");

                builder.addSatellite(
                        constellationType,
                        svid,
                        cn0DbHz,
                        elevationDegrees,
                        azimuthDegrees,
                        hasAlmanacData,
                        hasEphemerisData,
                        usedInFix,
                        false,
                        0.0f,
                        false,
                        2.0f);
            }

            return builder.build();
        } catch (Exception e) {
            Log.e(TAG, "Error creating fake GNSS status: " + e.getMessage(), e);
            return originalStatus;
        }
    }
}
