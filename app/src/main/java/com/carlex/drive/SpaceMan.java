package com.carlex.drive;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import uk.me.g4dpz.satellite.GroundStationPosition;
import uk.me.g4dpz.satellite.SatPos;
import uk.me.g4dpz.satellite.Satellite;
import uk.me.g4dpz.satellite.SatelliteFactory;
import uk.me.g4dpz.satellite.TLE;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SpaceMan {
    private static final String TAG = "SpaceMan";
    private static final String PREF_SATELLITES = "pref_satellites";
    private static final String PREF_LOCATIONS = "pref_locations";
    private static final String KEY_SATELLITES = "satellites";
    private static final String KEY_LOCATIONS = "locations";
    private static final long SAVE_INTERVAL_MS = 1000; // 5 segundos

    private static SharedPreferences satellitePreferences;
    private static SharedPreferences locationPreferences;
    protected static Date now;
    protected static  Location calculatedLocation;
    protected static GroundStationPosition groundStationPosition;
    private static long lastSaveTime = 0;

    public SpaceMan(Context context, String tles, Location loc) {
        try {
            satellitePreferences = context.getSharedPreferences(PREF_SATELLITES, Context.MODE_PRIVATE);
            locationPreferences = context.getSharedPreferences(PREF_LOCATIONS, Context.MODE_PRIVATE);

            setNow();
            setGroundStationPosition(loc);
            parseTLE(tles);
            calculatePositions();
        } catch (Exception e) {
            log("Erro na inicialização do SpaceMan: " + e.getMessage());
        }
    }

    public static void setNow() {
        setNow(System.currentTimeMillis());
    }

    public static void setNow(long time) {
        try {
            now = new Date(time);
        } catch (Exception e) {
            log("Erro ao definir a data atual: " + e.getMessage());
        }
    }

    public static long getNow() {
        return now != null ? now.getTime() : 0;
    }

    public static void setGroundStationPosition(Location loc) {
        try {
            groundStationPosition = new GroundStationPosition(loc.getLatitude(), loc.getLongitude(), loc.getAltitude());
            calculatedLocation = loc;
            if (shouldSaveLocations()) {
                saveLocationToPreferences(loc);
            }
        } catch (Exception e) {
            log("Erro ao definir a posição da estação terrestre: " + e.getMessage());
        }
    }

    private static boolean shouldSaveLocations() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSaveTime >= SAVE_INTERVAL_MS) {
            lastSaveTime = currentTime;
            return true;
        }
        return false;
    }

    private static void saveLocationToPreferences(Location loc) {
        try {
            SharedPreferences.Editor editor = locationPreferences.edit();
            editor.clear(); // Limpa as preferências antes de salvar novos valores
            JSONArray jsonArray = new JSONArray();
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("latitude", loc.getLatitude());
                jsonObject.put("longitude", loc.getLongitude());
                jsonObject.put("altitude", loc.getAltitude());
                jsonObject.put("bearing", loc.getBearing());
                jsonObject.put("speed", loc.getSpeed());
                jsonObject.put("accuracy", loc.getAccuracy());
                jsonObject.put("time", loc.getTime());
                jsonObject.put("provider", loc.getProvider());
                jsonArray.put(jsonObject);
            } catch (JSONException e) {
                log("Erro ao converter localização para JSON: " + e.getMessage());
            }
            editor.putString(KEY_LOCATIONS, jsonArray.toString());
            editor.apply();
        } catch (Exception e) {
            log("Erro ao salvar localização nas preferências: " + e.getMessage());
        }
    }

    public static Location getCalculatedLocation() {
        return calculatedLocation;
    }

    public static ArrayList<MyGpsSatellite> getGpsSatellites() {
	    Log.d(TAG, "getGpsSatellites");
        return gpsSatellites;
    }
    
    public static float getMeanCn0() {
        float sumSnr = 0.0f;
        List<MyGpsSatellite> gpsSatellites = getGpsSatellites();
        for (SpaceMan.MyGpsSatellite satellite : gpsSatellites) {
            sumSnr += satellite.getSnr();
        }
        return gpsSatellites.isEmpty() ? 0.0f : sumSnr / gpsSatellites.size();
    }
    
    
    public static float getMaxCn0() {
        float maxSnr = 0.0f;
        List<MyGpsSatellite> gpsSatellites = getGpsSatellites();
        for (SpaceMan.MyGpsSatellite satellite : gpsSatellites) {
            if (maxSnr< satellite.getSnr()){
                maxSnr = satellite.getSnr();
            }
        }
        return gpsSatellites.isEmpty() ? 0.0f : maxSnr;
    }
    
    
    public static int getSatelliteCount() {
        return getGpsSatellites().size();
    }


    public int getTLECount() {
        return sats.size();
    }

    public void parseTLE(String xtles) {
        parseTLE(xtles.split("\n"));
    }

    public void parseTLE(String[] tles) {
        try {
            sats.clear();
            for (int i = 0; (i + 2) < tles.length; i += 3) {
                String[] tlein = new String[3];
                tlein[0] = tles[i];
                tlein[1] = tles[i + 1];
                tlein[2] = tles[i + 2];
                SatelliteInfo si = new SatelliteInfo();
                si.tle = new TLE(tlein);
                si.satellite = SatelliteFactory.createSatellite(si.tle);
                sats.add(si);
            }
        } catch (Exception e) {
            log("Erro ao analisar os TLEs: " + e.getMessage());
        }
    }

    public void calculatePositions() {
        try {
            gpsSatellites.clear();
            for (SatelliteInfo si : sats) {
                si.satPos = si.satellite.getPosition(groundStationPosition, now);
                if (si.satPos.isAboveHorizon()) {
                    MyGpsSatellite gs = new MyGpsSatellite();
                    gs.azimuth = (float) Math.toDegrees(si.satPos.getAzimuth());
                    gs.elevation = (float) Math.toDegrees(si.satPos.getElevation());
                    gs.prn = Integer.valueOf(si.tle.getName().split("\\(PRN ")[1].split("\\)")[0]);
                    gs.snr = Math.round(20.0f + 70.0f * gs.elevation / 90.0f);
                    gs.hasAlmanac = (gs.elevation > 20.0);
                    gs.hasEphemeris = (gs.elevation > 10.0);
                    gs.usedInFix = (gs.elevation > 10.0);
                    gs.range = si.satPos.getRange();
                    if (gs.elevation > 10.0) {
                        gpsSatellites.add(gs);
                    }
                }
            }
            if (shouldSaveSatellites()) {
                saveSatellitesToPreferences();
            }
        } catch (Exception e) {
            log("Erro ao calcular as posições dos satélites: " + e.getMessage());
        }
    }

    private static boolean shouldSaveSatellites() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSaveTime >= SAVE_INTERVAL_MS) {
            lastSaveTime = currentTime;
            return true;
        }
        return false;
    }

    private static void saveSatellitesToPreferences() {
        try {
            SharedPreferences.Editor editor = satellitePreferences.edit();
            editor.clear(); // Limpa as preferências antes de salvar novos valores

            JSONObject satellitesData = new JSONObject();
            JSONArray satellitesArray = new JSONArray();
            int satelliteCount = gpsSatellites.size();
            float maxSnr = 0;
            float sumSnr = 0;

            for (MyGpsSatellite satellite : gpsSatellites) {
                JSONObject satelliteObject = new JSONObject();
                try {
                    satelliteObject.put("azimuth", satellite.getAzimuth());
                    satelliteObject.put("elevation", satellite.getElevation());
                    satelliteObject.put("snr", satellite.getSnr());
                    satelliteObject.put("range", satellite.getRange());
                    satelliteObject.put("hasAlmanac", satellite.getHasAlmanac());
                    satelliteObject.put("hasEphemeris", satellite.getHasEphemeris());
                    satelliteObject.put("usedInFix", satellite.getUsedInFix());
                    satellitesArray.put(new JSONObject().put(String.valueOf(satellite.getPrn()), satelliteObject));

                    if (satellite.getSnr() > maxSnr) {
                        maxSnr = satellite.getSnr();
                    }
                    sumSnr += satellite.getSnr();
                } catch (JSONException e) {
                    log("Erro ao converter satélite para JSON: " + e.getMessage());
                }
            }

            float meanSnr = satelliteCount > 0 ? sumSnr / satelliteCount : 0;

            satellitesData.put("NumeroSatellites", satelliteCount);
            satellitesData.put("MediaSnr", meanSnr);
            satellitesData.put("MaximoSnr", maxSnr);
            satellitesData.put("SatelitesData", satellitesArray);

            editor.putString(KEY_SATELLITES, satellitesData.toString());
            editor.apply();
        } catch (Exception e) {
            log("Erro ao salvar satélites nas preferências: " + e.getMessage());
        }
    }

    

    protected static void log(String s) {

        Log.d(TAG, s);
    }

    
    public class MyGpsSatellite {
        public float azimuth, elevation;
        public int prn;
        public float snr;
        public double range;
        public boolean hasAlmanac, hasEphemeris, usedInFix;

        @Override
        public String toString() {
            return "PRN " + prn + " SNR " + snr + " Azi " + azimuth + " Ele " + elevation + " ALM " + hasAlmanac + " EPH " + hasEphemeris + " USE " + usedInFix;
        }
        public double getRange() {
            return range;
        }
        public float getAzimuth() {
            return azimuth;
        }
        public float getElevation() {
            return elevation;
        }
        public int getPrn() {
            return prn;
        }
        public float getSnr() {
            return snr;
        }
        public boolean getHasAlmanac() {
            return hasAlmanac;
        }
        public boolean getHasEphemeris() {
            return hasEphemeris;
        }
        public boolean getUsedInFix() {
            return usedInFix;
        }
    }

    public static ArrayList<MyGpsSatellite> gpsSatellites = new ArrayList<>();
    public static ArrayList<SatelliteInfo> sats = new ArrayList<>();

    
    
    
    private class SatelliteInfo {
        TLE tle;
        Satellite satellite;
        SatPos satPos;
    }
}



/*package com.carlex.drive;

import android.content.Context;
import android.location.Location;
import android.util.Log;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import uk.me.g4dpz.satellite.GroundStationPosition;
import uk.me.g4dpz.satellite.SatPos;
import uk.me.g4dpz.satellite.Satellite;
import uk.me.g4dpz.satellite.SatelliteFactory;
import uk.me.g4dpz.satellite.TLE;

public class SpaceMan {
    private static final String TAG = "SpaceMan";

    private Context context;
    private String tles;
    private Location loc;
    protected Date now;
    protected Location calculatedLocation;
    protected GroundStationPosition groundStationPosition;

    public SpaceMan(Context context, String tles, Location loc) {
        this.context = context;
        this.tles = tles;
        this.loc = loc;

        //MyGpsSatellite gs = new MyGpsSatellite();
        setNow();
        setGroundStationPosition(loc);
        parseTLE(tles);
        calculatePositions();
    }
0
    public void setNow() {
        setNow(System.currentTimeMillis());
    }

    public void setNow(long time) {
        now = new Date(time);
    }

    public long getNow() {
        return now.getTime();
    }

    public void setGroundStationPosition(Location loc) {
        groundStationPosition = new GroundStationPosition(loc.getLatitude(), loc.getLongitude(), loc.getAltitude());
        calculatedLocation = loc;
    }

    public Location getCalculatedLocation() {
        return calculatedLocation;
    }

    public static ArrayList<MyGpsSatellite> getGpsSatellites() {
        return gpsSatellites;
    }

    public int getTLECount() {
        return sats.size();
    }

    public void parseTLE(String xtles) {
        parseTLE(xtles.split("\n"));
    }

    public void parseTLE(String[] tles) {
        sats.clear();
        for (int i = 0; (i + 2) < tles.length; i += 3) {
            String[] tlein = new String[3];
            tlein[0] = tles[i];
            tlein[1] = tles[i + 1];
            tlein[2] = tles[i + 2];
            SatelliteInfo si = new SatelliteInfo();
            si.tle = new TLE(tlein);
            si.satellite = SatelliteFactory.createSatellite(si.tle);
            sats.add(si);
        }
    }

    public void calculatePositions() {
        gpsSatellites.clear();
        for (SatelliteInfo si : sats) {
            si.satPos = si.satellite.getPosition(groundStationPosition, now);
            if (si.satPos.isAboveHorizon()) {
                MyGpsSatellite gs = new MyGpsSatellite();
                gs.azimuth = (float) Math.toDegrees(si.satPos.getAzimuth());
                gs.elevation = (float) Math.toDegrees(si.satPos.getElevation());
                gs.prn = Integer.valueOf(si.tle.getName().split("\\(PRN ")[1].split("\\)")[0]);
                gs.snr = Math.round(20.0f + 70.0f * gs.elevation / 90.0f);
                gs.hasAlmanac = (gs.elevation > 20.0);
                gs.hasEphemeris = (gs.elevation > 10.0);
                gs.usedInFix = (gs.elevation > 10.0);
                gs.range = si.satPos.getRange();
                if (gs.elevation > 10.0) {
                    gpsSatellites.add(gs);
                }
            }
        }
    }

    protected void log(String s) {
        Log.d(TAG, s);
    }

    private String readRawTextFile(Context context, int resId) {
        InputStream inputStream = context.getResources().openRawResource(resId);
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    public static class MyGpsSatellite {
        public float azimuth, elevation;
        public int prn;
        public float snr;
        public double range;
        public boolean hasAlmanac, hasEphemeris, usedInFix;

        @Override
        public String toString() {
            return "PRN " + prn + " SNR " + snr + " Azi " + azimuth + " Ele " + elevation + " ALM " + hasAlmanac + " EPH " + hasEphemeris + " USE " + usedInFix;
        }

        public double getRange() {
            return range;
        }

        public float getAzimuth() {
            return azimuth;
        }

        public float getElevation() {
            return elevation;
        }

        public int getPrn() {
            return prn;
        }

        public float getSnr() {
            return snr;
        }

        public boolean getHasAlmanac() {
            return hasAlmanac;
        }

        public boolean getHasEphemeris() {
            return hasEphemeris;
        }

        public boolean getUsedInFix() {
            return usedInFix;
        }
    }

    public static ArrayList<MyGpsSatellite> gpsSatellites = new ArrayList<>();
    private static ArrayList<SatelliteInfo> sats = new ArrayList<>();

    public static class SatelliteInfo {
        public TLE tle;
        public Satellite satellite;
        public SatPos satPos;
    }
}


/*package com.carlex.drive;


import android.content.Context;
import android.location.Location;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import uk.me.g4dpz.satellite.GroundStationPosition;
import uk.me.g4dpz.satellite.SatPos;
import uk.me.g4dpz.satellite.Satellite;
import uk.me.g4dpz.satellite.SatelliteFactory;
import uk.me.g4dpz.satellite.TLE;

public class SpaceMan {
    private static final String TAG = "SpaceMan";

    private Context context;
    private String tles;
    private Location loc;
    protected Date now;
    protected Location calculatedLocation;
    protected GroundStationPosition groundStationPosition;

    public SpaceMan(Context context, String tles, Location loc) {
        this.context = context;
        this.tles = tles;
        this.loc = loc;

	gs = new MyGpsSatellite();
        setNow();
        setGroundStationPosition(loc);
        parseTLE(tles);
        calculatePositions();
	}
	

    public void setNow() {
        setNow(System.currentTimeMillis());
    }

    public void setNow(long time) {
        now = new Date(time);
    }

    public long getNow() {
        return now.getTime();
    }

    public void setGroundStationPosition(Location loc) {
        groundStationPosition = new GroundStationPosition(loc.getLatitude(), loc.getLongitude(), loc.getAltitude());
        calculatedLocation = loc;
    }

    public Location getCalculatedLocation() {
        return calculatedLocation;
    }

    public static ArrayList<MyGpsSatellite> getGpsSatellites() {
        return gpsSatellites;
    }

    public int getTLECount() {
        return sats.size();
    }

    public void parseTLE(String xtles) {
        parseTLE(xtles.split("\n"));
    }

    public void parseTLE(String[] tles) {
	sats.clear();
        for (int i = 0; (i + 2) < tles.length; i += 3) {
            String[] tlein = new String[3];
            tlein[0] = tles[i];
            tlein[1] = tles[i + 1];
            tlein[2] = tles[i + 2];
            SatelliteInfo si = new SatelliteInfo();
            si.tle = new TLE(tlein);
            si.satellite = SatelliteFactory.createSatellite(si.tle);
            sats.add(si);
        }
    }

    public void calculatePositions() {
        gpsSatellites.clear();
        for (SatelliteInfo si : sats) {
            si.satPos = si.satellite.getPosition(groundStationPosition, now);
            if (si.satPos.isAboveHorizon()) {
                MyGpsSatellite gs = new MyGpsSatellite();
                gs.azimuth = (float) Math.toDegrees(si.satPos.getAzimuth());
                gs.elevation = (float) Math.toDegrees(si.satPos.getElevation());
                gs.prn = Integer.valueOf(si.tle.getName().split("\\(PRN ")[1].split("\\)")[0]);
                gs.snr = Math.round(20.0f + 70.0f * gs.elevation / 90.0f);
                gs.hasAlmanac = (gs.elevation > 20.0);
                gs.hasEphemeris = (gs.elevation > 10.0);
                gs.usedInFix = (gs.elevation > 10.0);
                gs.range = si.satPos.getRange();
                if (gs.elevation > 10.0) {
                    gpsSatellites.add(gs);
                }
            }
        }
    }


    protected void log(String s) {
        Log.d(TAG, s);
    }

    private String readRawTextFile(Context context, int resId) {
        InputStream inputStream = context.getResources().openRawResource(resId);
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }


    public static class MyGpsSatellite {
        public float azimuth, elevation;
        public int prn;
        public float snr;
        public double range;
        public boolean hasAlmanac, hasEphemeris, usedInFix;

        @Override
        public String toString() {
            return "PRN " + prn + " SNR " + snr + " Azi " + azimuth + " Ele " + elevation + " ALM " + hasAlmanac + " EPH " + hasEphemeris + " USE " + usedInFix;
        }
        public double getRange() {
            return range;
        }
        public float getAzimuth() {
            return azimuth;
        }
        public float getElevation() {
            return elevation;
        }
        public int getPrn() {
            return prn;
        }
        public float getSnr() {
            return snr;
        }
        public boolean getHasAlmanac() {
            return hasAlmanac;
        }
        public boolean getHasEphemeris() {
            return hasEphemeris;
        }
        public boolean getUsedInFix() {
            return usedInFix;
        }
    }

    public static ArrayList<MyGpsSatellite> gpsSatellites = new ArrayList<>();
}
*/
