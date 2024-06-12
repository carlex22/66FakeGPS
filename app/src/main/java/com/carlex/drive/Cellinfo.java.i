/*package com.carlex.drive;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutionException;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;



import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutionException;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class Cellinfo implements IXposedHookLoadPackage {

    private static long lastRequestTime = 0;
    private static final long MIN_REQUEST_INTERVAL = 60 * 1000; 
    private static Location lastLocation = null;

    private static final String PREFS_NAME = "CellInfoPrefs";
    private static final String KEY_CELL_DATA = "cellData";

    private static Context appContext;

    private static Context getSystemContext() {
        try {
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Method currentApplicationMethod = activityThreadClass.getDeclaredMethod("currentApplication");
            currentApplicationMethod.setAccessible(true);
            return (Context) currentApplicationMethod.invoke(null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static class GetCellInfoTask extends AsyncTask<Void, Void, JSONObject> {
        private double lat;
        private double lon;

        public GetCellInfoTask(double lat, double lon) {
            this.lat = lat;
            this.lon = lon;
        }

        @Override
        protected JSONObject doInBackground(Void... voids) {
            try {
                String apiKey = "pk.28294d46ef4d93a0404c0fbb562264f6";
                double centerLat = lat;
                double centerLon = lon;
                double radius = 0.009;

                double minLat = centerLat - radius;
                double maxLat = centerLat + radius;
                double minLon = centerLon - radius;
                double maxLon = centerLon + radius;

                String url = "https://opencellid.org/cell/getInArea?key=" + apiKey +
                        "&BBOX=" + minLat + "," + minLon + "," + maxLat + "," + maxLon +
                        "&format=json&mcc=724&limit=1";

                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                StringBuilder response = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                connection.disconnect();

                JSONObject jsonResponse = new JSONObject(response.toString());
                if (jsonResponse.has("cells")) {
                    JSONArray cells = jsonResponse.getJSONArray("cells");
                    if (cells.length() > 0) {
                        return cells.getJSONObject(0);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private static Location getLastKnownLocation(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Location location = null;
        if (locationManager != null) {
            List<String> providers = locationManager.getProviders(true);
            for (String provider : providers) {
                Location l = locationManager.getLastKnownLocation(provider);
                if (l != null) {
                    if (location == null || l.getAccuracy() < location.getAccuracy()) {
                        location = l;
                    }
                }
            }
        }
        return location;
    }

    public static JSONObject getCellInfo(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Location currentLocation = getLastKnownLocation(context);

        if (currentLocation == null) {
            Log.d("cell", "Não foi possível obter a localização.");
            return null;
        }

        /*if (SystemClock.elapsedRealtime() - lastRequestTime < MIN_REQUEST_INTERVAL &&
            lastLocation != null &&
            currentLocation.distanceTo(lastLocation) < 10) {
            Log.d("cell", "Nova solicitação bloqueada, retornando dados armazenados.");
            String savedData = prefs.getString(KEY_CELL_DATA, null);
            if (savedData != null) {
                try {
                    return new JSONObject(savedData);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }*/


	/*
        lastRequestTime = SystemClock.elapsedRealtime();
        lastLocation = currentLocation;

        try {
            JSONObject cellData = new GetCellInfoTask(currentLocation.getLatitude(), currentLocation.getLongitude()).execute().get();
            if (cellData != null) {
                prefs.edit().putString(KEY_CELL_DATA, cellData.toString()).apply();
            }
            return cellData;
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        Log.d("cell", "Hooking cell info...");
        hookCellInfo(lpparam);
    }

    private void hookCellInfo(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedHelpers.findAndHookMethod("android.telephony.TelephonyManager", lpparam.classLoader, "getAllCellInfo",
                new XC_MethodHook() {

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        List<Object> cellInfoList = (List<Object>) param.getResult();
                        if (cellInfoList != null && !cellInfoList.isEmpty()) {
                            for (Object cellInfo : cellInfoList) {
                                if (cellInfo.getClass().getName().equals("android.telephony.CellInfoLte")) {
                                    injectCellInfoLte(cellInfo);
                                } else if (cellInfo.getClass().getName().equals("android.telephony.CellInfoWcdma")) {
                                    injectCellInfoWcdma(cellInfo);
                                } else if (cellInfo.getClass().getName().equals("android.telephony.CellInfoGsm")) {
                                    injectCellInfoGsm(cellInfo);
                                }
                            }
                        }
                    }
                });
    }

    private void injectCellInfoLte(Object cellInfo) {
        if (appContext == null) {
            appContext = getSystemContext();
        }
        JSONObject cellData = getCellInfo(appContext);
        if (cellData != null) {
            try {
                XposedHelpers.setIntField(cellInfo, "mCi", cellData.getInt("cellid"));
                XposedHelpers.setIntField(cellInfo, "mTac", cellData.getInt("lac"));
                XposedHelpers.setIntField(cellInfo, "mMcc", cellData.getInt("mcc"));
                XposedHelpers.setIntField(cellInfo, "mMnc", cellData.getInt("mnc"));
                XposedHelpers.setIntField(cellInfo, "mRssi", cellData.getInt("averageSignalStrength"));
            } catch (JSONException e) {
                Log.d("cell", "Erro ao injetar dados CellInfoLte");
                e.printStackTrace();
            }
        }
    }

    private void injectCellInfoWcdma(Object cellInfo) {
        if (appContext == null) {
            appContext = getSystemContext();
        }
        JSONObject cellData = getCellInfo(appContext);
        if (cellData != null) {
            try {
                XposedHelpers.setIntField(cellInfo, "mCid", cellData.getInt("cellid"));
                XposedHelpers.setIntField(cellInfo, "mLac", cellData.getInt("lac"));
                XposedHelpers.setIntField(cellInfo, "mMcc", cellData.getInt("mcc"));
                XposedHelpers.setIntField(cellInfo, "mMnc", cellData.getInt("mnc"));
                XposedHelpers.setIntField(cellInfo, "mSignalStrength", cellData.getInt("averageSignalStrength"));
            } catch (JSONException e) {
                Log.d("cell", "Erro ao injetar dados CellInfoWcdma");
                e.printStackTrace();
            }
        }
    }

    private void injectCellInfoGsm(Object cellInfo) {
        if (appContext == null) {
            appContext = getSystemContext();
        }
        JSONObject cellData = getCellInfo(appContext);
        if (cellData != null) {
            try {
                XposedHelpers.setIntField(cellInfo, "mCid", cellData.getInt("cellid"));
                XposedHelpers.setIntField(cellInfo, "mLac", cellData.getInt("lac"));
                XposedHelpers.setIntField(cellInfo, "mMcc", cellData.getInt("mcc"));
                XposedHelpers.setIntField(cellInfo, "mMnc", cellData.getInt("mnc"));
                XposedHelpers.setIntField(cellInfo, "mSignalStrength", cellData.getInt("averageSignalStrength"));
            } catch (JSONException e) {
                Log.d("cell", "Erro ao injetar dados CellInfoGsm");
                e.printStackTrace();
            }
        }
    }
}
*/
