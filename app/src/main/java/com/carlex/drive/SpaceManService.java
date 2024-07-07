package com.carlex.drive;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.content.res.XmlResourceParser;
import android.content.ContentResolver;
import android.os.IBinder;
import com.google.android.gms.location.LastLocationRequest;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import com.carlex.drive.R;
import org.json.JSONArray;
import java.util.Locale;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Service;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import android.os.Process;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.content.Intent;
import android.location.Criteria;
import android.location.GnssMeasurementsEvent;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.OnNmeaMessageListener;
import android.os.Bundle;
import android.provider.Settings;
import android.os.Handler;
import android.os.Binder;
import android.util.Log;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;
import android.content.SharedPreferences;
import uk.me.g4dpz.satellite.GroundStationPosition;
import uk.me.g4dpz.satellite.SatPos;
import uk.me.g4dpz.satellite.Satellite;
import uk.me.g4dpz.satellite.SatelliteFactory;
import uk.me.g4dpz.satellite.TLE;
import android.preference.PreferenceManager;
import com.topjohnwu.superuser.Shell;
import java.io.IOException;
  
import com.topjohnwu.superuser.io.SuFile;
// com.topjohnwu.superuser.io.SuFile;

public class SpaceManService extends Service {

    
    
    private static final String TAG = "SpaceManService";
    private static final long INTERVAL_MS = 2500;
    private static final String PREF_SATELLITES = "fake_satellites";
    private static final String PREF_LOCATIONS = "fake_locations";
    private static final String KEY_SATELLITES = "satellites.json";
    private static final String KEY_LOCATIONS = "locations.json";
    private static final long SAVE_INTERVAL_MS = 1000;
    private static final String DIRECTORY_PATH = "/data/system/carlex/";

    private static long lastSaveTime = 0;
    private static long lastSaveTimeM = 0;

    private Context context;
    private Handler handler;
    private Runnable runnable;
    private LocationManager locationManager;

    private OnNmeaMessageListener nmeaMessageListener;
    private GnssStatus.Callback gnssStatusCallback;
    private LocationListener locationListener;
    private GnssMeasurementsEvent.Callback gnssMeasurementsCallback;

    private static GroundStationPosition groundStationPosition;
    private static  Date now;
    private static  Location calculatedLocation;
    private static Location loc;

    private static String newNmeaSentence = "$XPGGA,123519,4807.038,N,01131.000,E,1,08,0.9,545.4,M,46.9,M,,";
    private static String fakeNmeaSentence = newNmeaSentence;

    private static ArrayList<MyGpsSatellite> gpsSatellites = new ArrayList<>();
    private static ArrayList<SatelliteInfo> sats = new ArrayList<>();

    private static  GnssStatus fakegnssStatus;
    private static SpaceManService instance;
    private SystemPreferencesHandler systemPreferencesHandler;

    public static Location lastLocation;
    private static  SharedPreferences satellitePreferences;
    private static SharedPreferences locationPreferences;
    private static GetCell getCell;
    private static GetCell.CellInfoData cellInfoData;

    public static boolean isRunning() {
        return instance != null;
    }

    public static boolean checkSave() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSaveTime < 1000) {
            return false;
        }
        lastSaveTime = currentTime;
        return true;
    }
    
    
    public static boolean checkSaveM() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSaveTimeM < 60000) {
            return false;
        }
        lastSaveTimeM = currentTime;
        return true;
    }
    
    public static boolean isSignificantLocationChange(Location loc) {
        float[] result = new float[1];
        if (cellInfoData != null) {
            Location.distanceBetween(loc.getLatitude(), loc.getLongitude(), cellInfoData.getLat(), cellInfoData.getLon(), result);
            if (result[0] > 300){
                log("xCell distancia mauor 300mts obter novos dados célula.");
                return true;
            }
        }
        return false;
    }
    
    
    private static boolean saveCellToPreferences(Location loc) {
        if (checkSaveM() || isSignificantLocationChange(loc) || cellInfoData == null ) {
             log("xCell obtendo informações da célula.");
          
            // Usando a classe GetCell para obter informações da célula
             cellInfoData = getCell.getCellInfo(loc.getLatitude(), loc.getLongitude());
            
            if (cellInfoData == null) {
                log("xCell Não foi possível obter informações da célula.");
                return false;
            }
    
            
            JSONArray jsonArray = new JSONArray();
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("getCid", cellInfoData.getCellId());
                jsonObject.put("getLac", cellInfoData.getLac());
                jsonObject.put("getMccString", cellInfoData.getMcc());
                jsonObject.put("getMncString", cellInfoData.getMnc());
                jsonObject.put("getLatitude", cellInfoData.getLat());
                jsonObject.put("getLongitude", cellInfoData.getLon());
                jsonObject.put("date", cellInfoData.getDate());
                jsonArray.put(jsonObject);
            } catch (JSONException e) {
                log("xCell Error converting cell data to JSON: " + e.getMessage());
                return false;
            }
    
            Shell.su("mkdir -p " + DIRECTORY_PATH).exec();
            File dir = SuFile.open(DIRECTORY_PATH);
            File file = SuFile.open(dir, "cell_data.json");
    
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(jsonArray.toString());
                Shell.su("chmod 777 " + file.getAbsolutePath()).exec();
                log("xCell Saved cell data to: " + file.getAbsolutePath());
            } catch (IOException e) {
                log("xCell Error writing cell data to file: " + e.getMessage());
                return false;
            }
        }
        log("xCell usando cache informações da célula.");
        return true;
    }
    
    public static boolean setLastLoc(Location lLocation){
        lastLocation = lLocation;
        log("set lastLOC"+ lastLocation.toString());
        return true;
    }   
    
    public static void settLoc(Location lLocation){
        loc = lLocation;
        log("set cureLOC" + loc.toString());
    }   

    public static  boolean  saveLocationToPreferences(Location lloc, double latitude, double longitude, float bearing, double speed, double altitude, long timestamp) {
     //   if (checkSave()) {
        try {
            JSONObject jsonObject = new JSONObject();
            JSONArray jsonArray = new JSONArray();
            if (loc!=null && lastLocation!=null){
                    jsonObject = new JSONObject();
                    jsonObject.put("Latitude", lloc.getLatitude());
                    jsonObject.put("Longitude", lloc.getLongitude());
                    jsonObject.put("Altitude", lloc.getAltitude());
                    jsonObject.put("Bearing", lloc.getBearing());
                    jsonObject.put("Speed", lloc.getSpeed());
                    jsonObject.put("Accuracy", lloc.getAccuracy());
                    jsonObject.put("Timestamp", lloc.getTime());
                    jsonObject.put("Provider", loc.getProvider());
                    jsonArray.put(1,jsonObject);
                
                    jsonObject = new JSONObject();
                    jsonObject.put("Latitude", latitude);
                    jsonObject.put("Longitude", longitude);
                    jsonObject.put("Altitude", altitude);
                    jsonObject.put("Bearing", bearing);
                    jsonObject.put("Speed", speed);
                    jsonObject.put("Accuracy", 2);
                    jsonObject.put("Timestamp", timestamp);
                    jsonObject.put("nmea", getNmea());
                    jsonArray.put(0,jsonObject);
                
                    Shell.su("mkdir -p " + DIRECTORY_PATH).exec();
                    File dir = SuFile.open(DIRECTORY_PATH);
                    File file = SuFile.open(dir, KEY_LOCATIONS);
    
                    try (FileWriter writer = new FileWriter(file)) {
                        writer.write(jsonArray.toString());
                        Shell.su("chmod 777 " + file.getAbsolutePath()).exec();
                        log("Saved location to: " + file.getAbsolutePath());
                    } catch (IOException e) {
                        log("Error writing location to file: " + e.getMessage());
                        return false;
                    }
                
                }
            } catch (JSONException e) {
                log("Error converting location to JSON: " + e.getMessage());
                return false;
            }
            return true;
     //   }
    }

    protected static void log(String s) {
        Log.d(TAG, s);
    }

    public static int satelliteCount = 0;
    public static float maxSnr = 0;
    public static float meanSnr = 0;

    public static boolean saveSatellitesToPreferences() {
            try {
                SharedPreferences.Editor editor = satellitePreferences.edit();
                editor.clear();

                JSONObject satellitesData = new JSONObject();
                JSONArray satellitesArray = new JSONArray();
                satelliteCount = gpsSatellites.size();
                maxSnr = 0;
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
                    return false;
                        //"Error converting satellite to JSON: " + e.getMessage());
                    }
                }

                meanSnr = satelliteCount > 0 ? sumSnr / satelliteCount : 0;

                satellitesData.put("NumeroSatellites", satelliteCount);
                satellitesData.put("MediaSnr", meanSnr);
                satellitesData.put("MaximoSnr", maxSnr);
                satellitesData.put("SatelitesData", satellitesArray);
                JSONArray satellitesDataArray = new JSONArray();
                satellitesDataArray.put(satellitesData);
                
                
                Shell.su("mkdir -p " + DIRECTORY_PATH).exec();
                File dir = SuFile.open(DIRECTORY_PATH);
                File file = SuFile.open(dir, KEY_SATELLITES);
    
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write(satellitesDataArray.toString());
                    Shell.su("chmod 777 " + file.getAbsolutePath()).exec();
                    log("Saved location to: " + file.getAbsolutePath());
                } catch (IOException e) {
                    log("Error writing location to file: " + e.getMessage());
                    return false;
                }
                
            } catch (Exception e) {
                log("Error saving satellites to preferences: " + e.getMessage());
            return false;
            }
        return true;
    }

    public static void setNmea(String nmea) {
        fakeNmeaSentence = nmea;
    }

    public static String getNmea() {
        return fakeNmeaSentence;
    }

    @Override
    public void onCreate() {
        super.onCreate();
       Locale.setDefault(Locale.US);
        log("Service onCreate");
        getCell = new GetCell();
    }

    private static  boolean isRunning = false;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (isRunning) {
            Log.e(TAG, "Fake SpaceMan service is running");
            return START_STICKY;
        }

        
        
        Log.i(TAG, "Fake SpaceMan service start");

        isRunning = true;

        if (intent != null && intent.getExtras() != null) {
            String packageName = intent.getExtras().getString("appContext");
            if (packageName != null) {
                try {
                    context = createPackageContext(packageName, 0);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

        log("Service onStartCommand");

        String tles = null;

        if (intent != null) {
            tles = intent.getStringExtra("tles");
        }

        log("Received TLEs: " + tles);
        parseTLE(tles);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (context == null) {
            context = getApplicationContext();
        }

        satellitePreferences = context.getSharedPreferences(PREF_SATELLITES, Context.MODE_PRIVATE);
        locationPreferences = context.getSharedPreferences(PREF_LOCATIONS, Context.MODE_PRIVATE);

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                log("Runnable triggered");
                if (calculatedLocation != null) {
                    updateSatelliteList(null);
                    calculatePositions();
                    fakegnssStatus = createFakeGnssStatus();
                    dumpGpsSatellites();
                    logGnssStatus(fakegnssStatus);
                }
                handler.postDelayed(this, INTERVAL_MS);
            }
        };

        gnssMeasurementsCallback = new GnssMeasurementsEvent.Callback() {
            @Override
            public void onGnssMeasurementsReceived(GnssMeasurementsEvent event) {
                super.onGnssMeasurementsReceived(event);
                log("GNSS Measurement received: " + event.toString());
            }

            @Override
            public void onStatusChanged(int status) {
                super.onStatusChanged(status);
                log("GNSS Measurement status changed: " + status);
            }
        };

        nmeaMessageListener = new OnNmeaMessageListener() {
            public void onNmeaMessage(String message, long timestamp) {
                Log.d(TAG, "NMEA message received: " + message);
                int numSatellites = 0;

                if (fakegnssStatus != null) {
                    numSatellites = fakegnssStatus.getSatelliteCount();
                }
                if (numSatellites > 0 && fakegnssStatus != null && calculatedLocation != null) {
                    setNmea(NmeaGenerator.generateNmea(fakegnssStatus, calculatedLocation, message));
                    String nmea = getNmea();
                    Log.d(TAG, "NMEA fake generated: " + getNmea());
                }
            }
        };

        gnssStatusCallback = new GnssStatus.Callback() {
            @Override
            public void onStarted() {
                super.onStarted();
                log("GNSS Status: GNSS started.");
            }

            @Override
            public void onStopped() {
                super.onStopped();
                log("GNSS Status: GNSS stopped.");
            }

            @Override
            public void onFirstFix(int ttffMillis) {
                super.onFirstFix(ttffMillis);
                log("GNSS Status: First fix in " + ttffMillis + " ms.");
            }

            public void onSatelliteStatusChanged(GnssStatus status) {
                super.onSatelliteStatusChanged(status);
                if (calculatedLocation != null) {
                    log("GNSS Status: Satellite status changed.");
                    logGnssStatus(status);
                    updateSatelliteList(null);
                    calculatePositions();
                    GnssStatus fakeStatus = createFakeGnssStatus();
                    dumpGpsSatellites();
                    logGnssStatus(fakeStatus);
                }
            }
        };

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                calculatedLocation = location;
                log("Location changed: " + location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                log("Status changed: Provider=" + provider + ", Status=" + status);
            }

            @Override
            public void onProviderEnabled(String provider) {
                log("Provider enabled: " + provider);
            }

            @Override
            public void onProviderDisabled(String provider) {
                log("Provider disabled: " + provider);
            }
        };

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        locationManager.registerGnssStatusCallback(gnssStatusCallback);
        locationManager.registerGnssMeasurementsCallback(gnssMeasurementsCallback);
        locationManager.addNmeaListener(nmeaMessageListener);

        handler.post(runnable);
        log("GNSS Status callback and location listener registered, handler started");

        return START_STICKY;
    }

    private static void updateSatelliteList(GnssStatus status) {
        if (calculatedLocation != null) {
            groundStationPosition = new GroundStationPosition(calculatedLocation.getLatitude(), calculatedLocation.getLongitude(), calculatedLocation.getAltitude());
            setNow();
            calculatePositions();
            boolean ssat = saveSatellitesToPreferences();
            boolean scel = saveCellToPreferences(loc);
        } else {
            log("Calculated location is null, cannot update satellite list");
        }
    }

    public static String getFakeMessage() {
        Log.i(TAG, "NMEA getFakeMessage called");
        return fakeNmeaSentence;
    }

    private static void setNow() {
        now = new Date(System.currentTimeMillis());
        log("Current time set: " + now);
    }

    public static  void calculatePositions() {
        gpsSatellites.clear();
        float maxCn0 = 0f;
        float meanCn0 = 0f;
        int satelliteCount = 0;
        float sumSnr = 0;
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
                    satelliteCount++;
                    sumSnr += gs.snr;
                    if (maxCn0 < gs.snr) {
                        maxCn0 = gs.snr;
                    }
                }
            }
        }
        if (satelliteCount > 0) {
            meanCn0 = sumSnr / satelliteCount;
        }
        log("Satellite positions calculated: count=" + satelliteCount + ", maxCn0=" + maxCn0 + ", meanCn0=" + meanCn0);
    }

    public static synchronized ArrayList<MyGpsSatellite> getGpsSatellites() {
        Log.d(TAG, "getGpsSatellites called");
        return new ArrayList<>(gpsSatellites);
    }

    public static float getMeanCn0() {
        return meanSnr;
    }

    public static float getMaxCn0() {
        return maxSnr;
    }

    public static int getSatelliteCount() {
        return satelliteCount;
    }

    public static void dumpGpsSatellites() {
        String sta = "";
        for (MyGpsSatellite gs : gpsSatellites) {
            sta += " [" + gs.toString() + "]";
        }
        Log.i(TAG, "Fix: " + gpsSatellites.size() + " sats: " + sta);
    }

    public static GnssStatus createFakeGnssStatus() {
        try {
            Log.i(TAG, "Build GNSS status...");

             calculatePositions();
            GnssStatus.Builder builder = new GnssStatus.Builder();

            Log.i(TAG, "Satellite count: " + gpsSatellites.size());

            for (MyGpsSatellite satellite : gpsSatellites) {
                float azimuth = satellite.getAzimuth() + noise();
                float elevation = satellite.getElevation() + noise();
                int prn = satellite.getPrn();
                float snr = satellite.getSnr() + noise();
                double range = satellite.getRange() + noise();
                boolean hasAlmanac = satellite.getHasAlmanac();
                boolean hasEphemeris = satellite.getHasEphemeris();
                boolean usedInFix = satellite.getUsedInFix();

                builder.addSatellite(
                        GnssStatus.CONSTELLATION_GPS, prn, snr, azimuth,
                        elevation, hasAlmanac, hasEphemeris, usedInFix,
                        false, (float) range, true, 0.2f);
            }

            return builder.build();
        } catch (Exception e) {
            Log.e(TAG, "Error creating fake GNSS status: " + e.getMessage(), e);
            return null;
        }
    }

    public static float noise() {
        return (float) (ThreadLocalRandom.current().nextDouble(0, 1) / 25);
    }

    public static void logGnssStatus(GnssStatus gnssStatus) {
        String log = "gnssStatus:";
        if (gnssStatus != null) {
            for (int i = 0; i < gnssStatus.getSatelliteCount(); i++) {
                int constellationType = gnssStatus.getConstellationType(i);
                int svid = gnssStatus.getSvid(i);
                float cn0DbHz = gnssStatus.getCn0DbHz(i);
                log += " [svid: " + svid + ", Constellation: " + constellationType + ", CN0: " + cn0DbHz + "]";
            }
            Log.i(TAG, log);
        } else {
            Log.d(TAG, "No GNSS status available");
        }
    }

    private static  boolean isAuthorizedPackage(String packageName) {
        return "com.carlex.drive".equals(packageName);
    }

    public static  void parseTLE(String xtles) {
        parseTLE(xtles.split("\n"));
    }

    public static void parseTLE(String[] tles) {
        log("Parsing TLEs");
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
            log("Satellite info added: " + si.satellite.toString());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        log("Service onDestroy");
        if (gnssStatusCallback != null) {
            locationManager.unregisterGnssStatusCallback(gnssStatusCallback);
            locationManager.unregisterGnssMeasurementsCallback(gnssMeasurementsCallback);
            locationManager.removeNmeaListener(nmeaMessageListener);
            log("GNSS Status callback unregistered");
        }
        if (locationListener != null) {
            locationManager.removeUpdates(locationListener);
            log("Location listener unregistered");
        }
        handler.removeCallbacks(runnable);
        log("Handler callbacks removed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static  class MyGpsSatellite {
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

    private static class SatelliteInfo {
        TLE tle;
        Satellite satellite;
        SatPos satPos;
    }
}
