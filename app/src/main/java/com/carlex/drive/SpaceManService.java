package com.carlex.drive;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import android.app.people.ConversationStatus;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import java.util.Random;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.GpsSatellite;
import android.os.Environment;
import android.content.res.XmlResourceParser;
import java.util.List;

import android.widget.Toast;

import android.content.ContentResolver;
import android.os.IBinder;
import com.google.android.gms.location.LastLocationRequest;
import kotlin.BuilderInference;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import com.carlex.drive.R;
import org.json.JSONArray;
import java.util.Locale;
import android.os.Build;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Service;
import android.content.Context;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
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

import android.location.GpsStatus;

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


import androidx.core.content.ContextCompat;



import java.io.IOException;
//import com.topjohnwu.superuser.io.SuFile;
  
//import com.topjohnwu.superuser.io.SuFile;
// com.topjohnwu.superuser.io.SuFile;

public class SpaceManService extends Service {
    
    private static final String CHANNEL_ID = "SpaceManServiceChannel";

    
    private static final String TAG = "SpaceManService";
    private static final long INTERVAL_MS = 2500;
    private static final String KEY_CELL = "cell_data.json";
    private static final String PREF_SATELLITES = "fake_satellites";
    private static final String PREF_LOCATIONS = "fake_locations";
    private static final String KEY_SATELLITES = "satellites.json";
    private static final String KEY_LOCATIONS = "locations.json";
    private static final long SAVE_INTERVAL_MS = 1000;
    private static final String DIRECTORY_PATH = "/storage/emulated/0/carlex/";

    private static long lastSaveTime = 0;
    private static long lastSaveTimeM = 0;

    private static Context context;
    private Handler handler;
    private Runnable runnable;
    private LocationManager locationManager;

    private OnNmeaMessageListener nmeaMessageListener;
    private GnssStatus.Callback gnssStatusCallback;
    private LocationListener locationListener;
    private GnssMeasurementsEvent.Callback gnssMeasurementsCallback;

   public static boolean bC, bL, bS;
    public static JsonFileHandler saveCell, saveLoc, saveSat;

    
    
    private static GroundStationPosition groundStationPosition;
    private static  Date now;
    public static  Location calculatedLocation;
    private static Location loc;

    private static String newNmeaSentence = "$XPGGA,123519,4807.038,N,01131.000,E,1,08,0.9,545.4,M,46.9,M,,";
    private static String fakeNmeaSentence = newNmeaSentence;

    private static ArrayList<MyGpsSatellite> gpsSatellites = new ArrayList<>();
    private static ArrayList<SatelliteInfo> sats = new ArrayList<>();

    private static  CustomGnssStatus fakegnssStatus;
    private static SpaceManService instance;
    public static CustomGnssStatus customGnssStatus;
    
    public static Location lastLocation;
    private static  SharedPreferences satellitePreferences;
    private static SharedPreferences locationPreferences;
    private static GetCell getCell;
    private static GetCell.CellInfoData cellInfoData;

    public static boolean isRunning() {
        return isRunning;
    }

    public static boolean checkSave() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSaveTime < 1000) {
            return false;
        }
        lastSaveTime = currentTime;
        return true;
    }
    
    
   private static boolean checkPermissions() {
        if (context ==null) return false;
        int writePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return writePermission == PackageManager.PERMISSION_GRANTED;
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
                log("xCell distancia mauor 300mtbter novos dados célula.");
                return true;
            }
        }
        return false;
    }
    
    
    private static boolean saveCellToPreferences(Location loc) {
        
        if (cellInfoData == null || isSignificantLocationChange(loc) || checkSaveM()){
             log("xCell obtendo informações da célula.");
          
            // Usando a classe GetCell para obter informações da célula
             cellInfoData = getCell.getCellInfo(loc.getLatitude(), loc.getLongitude());
            
            if (cellInfoData == null) {
                log("xCell Não foi possível obter informações da célula.");
                return false;
            }
    
            
            SharedPreferences prefs = context.getSharedPreferences("FakeSensor", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("getCid", (int) cellInfoData.getCellId());
            editor.putInt("getLac", cellInfoData.getLac());
            editor.putInt("getCi", (int) cellInfoData.getCellId());
            editor.putInt("getTac", cellInfoData.getLac());
            editor.putInt("getMcc", cellInfoData.getMcc());
            editor.putInt("getMnc", cellInfoData.getMnc());
            editor.putInt("getCidString", (int) cellInfoData.getCellId());
            editor.putInt("getLacString", cellInfoData.getLac());
            editor.putInt("getCiString", (int) cellInfoData.getCellId());
            editor.putInt("getTacString", cellInfoData.getLac());
            editor.putInt("getMccString", cellInfoData.getMcc());
            editor.putInt("getMncString", cellInfoData.getMnc());
            editor.putFloat("getLatitude", (float)cellInfoData.getLat());
            editor.putFloat("getLongitude", (float)cellInfoData.getLon());
            editor.apply();
            
            /*
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
            
            
           if (checkPermissions()) {
                    // Inicializar o manipulador de arquivo JSON com a string JSON
                    boolean saveJ = saveCell.saveJson(jsonArray.toString());
                    //Log.d(TAG, "JSONcell salvo " + saveJ);
                    return saveJ;
                } else {
                    //Log.d(TAG, "Permission not granted. JSONcell not saved.");
                    return false;
                }*/
    
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
    //log("set cureLOC" + loc.toString());
    }   

    public static  boolean  saveLocationToPreferences() {
       
            SharedPreferences prefs = context.getSharedPreferences("FakeSensor", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("nmea",'"'+getNmea()+'"');            
            editor.apply();
        
        
        /*if (!bL) return false;
        
        try {
            
            
            
            JSONObject jsonObject = new JSONObject();
            JSONArray jsonArray = new JSONArray();
            if (calculatedLocation!=null){
                    jsonObject = new JSONObject();
                    jsonObject.put("Latitude", calculatedLocation.getLatitude());
                    jsonObject.put("Longitude", calculatedLocation.getLongitude());
                    jsonObject.put("Altitude", calculatedLocation.getAltitude());
                    jsonObject.put("Bearing", calculatedLocation.getBearing());
                    jsonObject.put("Speed", calculatedLocation.getSpeed());
                    jsonObject.put("Accuracy", calculatedLocation.getAccuracy());
                    jsonObject.put("Timestamp", calculatedLocation.getTime());
                    jsonObject.put("Provider", calculatedLocation.getProvider());
                    jsonObject.put("nmea", getNmea());
                    jsonArray.put(0,jsonObject);
                
               if (checkPermissions()) {
                        boolean saveJ = saveLoc.saveJson(jsonArray.toString());
                        //Log.d(TAG, "JSONloc salvo " + saveJ);
                        return saveJ;
                    } else {
                        //Log.d(TAG, "Permission not granted. JSONloc not saved.");
                        return false;
                    }
                
               
                
              }
            } catch (JSONException e) {
                log("Error converting location to JSON: " + e.getMessage());
                return false;
            }*/
            return true;
     //   }
    }

    protected static void log(String s) {
        //Log.d(TAG, s);
    }

    public static int satelliteCount = 0;
    public static float maxSnr = 0;
    public static float meanSnr = 0;
    public static JSONArray satellitesDataArray = new JSONArray();
    
    public static boolean saveSatellitesToPreferences() {
        
        //if (checkSave())
        if (gpsSatellites.size() >0)
            try {
                
                JSONObject  satellitesData = new JSONObject();
                JSONArray satellitesArray = new JSONArray();
                satelliteCount = gpsSatellites.size();
                maxSnr = 0;
                float sumSnr = 0;
            
            
 
                for (MyGpsSatellite satellite : gpsSatellites) {
                    JSONObject satelliteObject = new JSONObject();
                    try {
                    
                        satelliteObject.put("azimuth", adicionarRuido(satellite.getAzimuth(),0.1));
                        satelliteObject.put("elevation", adicionarRuido(satellite.getElevation(), 0.1));
                        satelliteObject.put("snr", adicionarRuido(satellite.getSnr(),0.1));
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

            
                String sattext = satellitesArray.toString();
                sattext = sattext.replace("\"", "\\\"");

            
                SharedPreferences prefs = context.getSharedPreferences("FakeSensor", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("NumeroSatellites", satelliteCount);
                editor.putFloat("MediaSnr", meanSnr);
                editor.putFloat("MaximoSnr", maxSnr);
                editor.putString("SatelitesData", '"'+sattext+'"');
                editor.apply();
        
            
            
           /*
                if (bS) {
            
                
                
                satellitesData.put("NumeroSatellites", satelliteCount);
                satellitesData.put("MediaSnr", meanSnr);
                satellitesData.put("MaximoSnr", maxSnr);
                satellitesData.put("SatelitesData", satellitesArray);
                satellitesDataArray = new JSONArray();
                satellitesDataArray.put(satellitesData);
                
                if (checkPermissions()) {
                    boolean saveJ = saveSat.saveJson(satellitesDataArray.toString());
                    //Log.d(TAG, "JSONSat salvo " + saveJ);
                    return saveJ;
                } else {
                    //Log.d(TAG, "Permission not granted. JSONSat not saved.");
                    return false;
                }
                }
            */
                
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
        
        logAvailableConstructors();
        
        isRunning = false;
    }

    private static  boolean isRunning = false;
    
   private static double adicionarRuido(double valorOriginal, double nivelRuido) {
        Random random = new Random();
        double ruido = (random.nextDouble() * 2 - 1) * nivelRuido;
        return valorOriginal + ruido;
    }
    
    

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (isRunning) {
            //Log.e(TAG, "Fake SpaceMan service is running");
            return START_STICKY;
        }

        
        
        //Log.i(TAG, "Fake SpaceMan service start");

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
        
        if (context == null) {
            context = getApplicationContext();
        }
        
            
       if (checkPermissions()) {
            //saveCell = new JsonFileHandler(DIRECTORY_PATH, KEY_CELL);
            //saveLoc = new JsonFileHandler(DIRECTORY_PATH, KEY_LOCATIONS);
            //saveSat = new JsonFileHandler(DIRECTORY_PATH, KEY_SATELLITES);
            bC = true;//saveCell.createDirectoryAndFileIfNotExists();
            bL = true;// saveLoc.createDirectoryAndFileIfNotExists();
            bS = true;//saveSat.createDirectoryAndFileIfNotExists();
        } else {
            Toast.makeText(context, "Falha ao salvar arquivos temporarios! Spaceman OFF", Toast.LENGTH_SHORT).show();
        }
        

        

        String tles = null;

        if (intent != null) {
            tles = intent.getStringExtra("tles");
        }

        log("Received TLEs: " + tles);
        parseTLE(tles);
        
        
        dumpGpsSatellites();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        
        
        
        
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                log("Runnable triggered");
                if (calculatedLocation != null) {
                    updateSatelliteList(calculatedLocation);
                    
                 //   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) 
                    //    fakegnssStatus = createFakeGnssStatus();
                 //   else
                        fakegnssStatus =  createGnssStatus();
                    
                    //dumpGpsSatellites();
                    logGnssStatus(fakegnssStatus);
                }
                handler.postDelayed(this, INTERVAL_MS);
            }
        };

        /*
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
        };*/
        
        
        nmeaMessageListener = new OnNmeaMessageListener() {
            public void onNmeaMessage(String message, long timestamp) {
                
                //Log.d(TAG, "NMEA message received: " + message);
                int numSatellites = 0;
                
                numSatellites = getSatelliteCount();      
                
                if (numSatellites > 0 && fakegnssStatus != null && calculatedLocation != null) {
                    setNmea(NmeaGenerator.generateNmea(fakegnssStatus, calculatedLocation, message));
                    String nmea = getNmea();
                    //Log.d(TAG, "NMEA fake generated: " + getNmea());
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
                    //  logGnssStatus(status);
                    updateSatelliteList(calculatedLocation);
                   // dumpGpsSatellites();
                    
                    
                 //   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                   //     fakegnssStatus = createFakeGnssStatus();
                  //  }
                  //  else {
                        fakegnssStatus = (createGnssStatus());
                 //   }
                    
                    logGnssStatus(fakegnssStatus);
                }
            }
        };

        
        
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                updateSatelliteList(location);
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

       
      locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
       locationManager.registerGnssStatusCallback(gnssStatusCallback);
       //locationManager.registerGnssMeasurementsCallback(gnssMeasurementsCallback);
       locationManager.addNmeaListener(nmeaMessageListener);
                
       handler.post(runnable);
        log("GNSS Status callback and location listener registerd");

        return START_STICKY;
    }
        
        
    public static void updateSatelliteList(Location loc) { 
      //if (checkSaveM() || isSignificantLocationChange(loc)) {
       calculatedLocation = loc;       
       // if (calculatedLocation != null) {
            groundStationPosition = new GroundStationPosition(calculatedLocation.getLatitude(), calculatedLocation.getLongitude(), calculatedLocation.getAltitude());
            
            calculatePositions();
            int numSatellites =  getSatelliteCount();
            fakegnssStatus = (createGnssStatus());
            boolean sloc = saveLocationToPreferences();
            boolean ssat = saveSatellitesToPreferences();
            boolean scel = saveCellToPreferences(calculatedLocation);
       // } else {
           log("Calculated and  update satellite list");
     //  }
    }

    public static String getFakeMessage() {
        //Log.i(TAG, "NMEA getFakeMessage called");
        return fakeNmeaSentence;
    }

    private static void setNow() {
        now = new Date(System.currentTimeMillis());
        log("Current time set: " + now);
    }

    public static  void calculatePositions() {
        setNow();
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
        //Log.d(TAG, "getGpsSatellites called");
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
        //Log.i(TAG, "Fix: " + gpsSatellites.size() + " sats: " + sta);
    }
    
    /*
    public static GnssStatus createFakeGnssStatus() {
        try {
             
            //Log.i(TAG, "Build GNSS status...");
            calculatePositions();
            GnssStatus.Builder builder = new GnssStatus.Builder();
            //Log.i(TAG, "Satellite count: " + gpsSatellites.size());

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
            //Log.e(TAG, "Error creating fake GNSS status: " + e.getMessage(), e);
            return null;
        }
    }
    
   */
    
     
           
              
   public static  CustomGnssStatus createGnssStatus() {
        try {
            JSONArray satelliteData = new JSONArray(satellitesDataArray.toString());
            
            if (satelliteData == null || satelliteData.length() == 0) {
                //Log.e(TAG, "Satellite data not loaded or empty");
                return new CustomGnssStatus(new ArrayList<>()); // Return an empty custom status
            }

            List<CustomGnssStatus.Satellite> satellites = new ArrayList<>();
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

                satellites.add(new CustomGnssStatus.Satellite(
                        constellationType,
                        svid,
                        cn0DbHz,
                        elevationDegrees,
                        azimuthDegrees,
                        hasEphemerisData,
                        hasAlmanacData,
                        usedInFix));
            }

            return new CustomGnssStatus(satellites);
        } catch (Exception e) {
            //Log.e(TAG, "Error creating fake GNSS status: " + e.getMessage(), e);
            return new CustomGnssStatus(new ArrayList<>()); // Return an empty custom status
        }
    }
    
    
   public static GnssStatus convertToGnssStatus(CustomGnssStatus customStatus) {
    if (customStatus == null) {
        //Log.d(TAG, "customStatus not loaded or empty");
        return null;
    }

    try {
        // Usar reflexão para obter acesso ao construtor e métodos da classe GnssStatus
        Class<?> gnssStatusClass = Class.forName("android.location.GnssStatus");

        // Crie um array de Satélites
        int satelliteCount = customStatus.getSatelliteCount();
        int[] svids = new int[satelliteCount];
        float[] cn0DbHzs = new float[satelliteCount];
        float[] elevations = new float[satelliteCount];
        float[] azimuths = new float[satelliteCount];
        boolean[] hasEphemeris = new boolean[satelliteCount];
        boolean[] hasAlmanac = new boolean[satelliteCount];
        boolean[] usedInFix = new boolean[satelliteCount];

        for (int i = 0; i < satelliteCount; i++) {
            svids[i] = customStatus.getSvid(i);
            cn0DbHzs[i] = customStatus.getCn0DbHz(i);
            elevations[i] = customStatus.getElevationDegrees(i);
            azimuths[i] = customStatus.getAzimuthDegrees(i);
            hasEphemeris[i] = customStatus.hasEphemerisData(i);
            hasAlmanac[i] = customStatus.hasAlmanacData(i);
            usedInFix[i] = customStatus.usedInFix(i);
        }

        // Acesso ao construtor privado de GnssStatus
        Constructor<?> constructor = gnssStatusClass.getDeclaredConstructor(
                int[].class, float[].class, float[].class, float[].class,
                boolean[].class, boolean[].class, boolean[].class
        );
        constructor.setAccessible(true);

        // Crie uma instância de GnssStatus com os dados
        GnssStatus gnssStatus = (GnssStatus) constructor.newInstance(
                svids, cn0DbHzs, elevations, azimuths,
                hasEphemeris, hasAlmanac, usedInFix
        );

        return gnssStatus;

    } catch (Exception e) {
        //Log.e(TAG, "Error converting custom GNSS status: " + e.getMessage(), e);
        return null;
    }
}


    
   private static void logAvailableConstructors() {
    try {
        Class<?> gnssStatusClass = Class.forName("android.location.GnssStatus");
        Constructor<?>[] constructors = gnssStatusClass.getDeclaredConstructors();
        for (Constructor<?> constructor : constructors) {
            //Log.d(TAG, "Constructor: " + constructor.toString());
        }
    } catch (Exception e) {
        //Log.e(TAG, "Error logging constructors: " + e.getMessage(), e);
    }
}

    
    
    

    public static float noise() {
        return (float) (ThreadLocalRandom.current().nextDouble(0, 1) / 25);
    }

    public static void logGnssStatus(CustomGnssStatus gnssStatus) {
        String log = "gnssStatus:";
        if (gnssStatus != null) {
            for (int i = 0; i < gnssStatus.getSatelliteCount(); i++) {
                int constellationType = gnssStatus.getConstellationType(i);
                int svid = gnssStatus.getSvid(i);
                float cn0DbHz = gnssStatus.getCn0DbHz(i);
                log += " [svid: " + svid + ", Constellation: " + constellationType + ", CN0: " + cn0DbHz + "]";
            }
            //Log.i(TAG, log);
        } else {
            //Log.d(TAG, "No GNSS status available");
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
    
    
    public  void  stopSpaceman(){
        if (isRunning){
            isRunning = false;
            log("Service stop");
            if (gnssStatusCallback != null) {
                locationManager.unregisterGnssStatusCallback(gnssStatusCallback);
            // locationManager.unregisterGnssMeasurementsCallback(gnssMeasurementsCallback);
                locationManager.removeNmeaListener(nmeaMessageListener);
                log("GNSS Status callback unregistered");
            }
            if (locationListener != null) {
                locationManager.removeUpdates(locationListener);
                log("Location listener unregistered");
            }
            //handler.removeCallbacks(runnable);
            
            if (handler != null) {
                handler.removeCallbacks(runnable); 
            log("Handler callbacks removed");
            }
        }
    }

    @Override
    public  void onDestroy() {
        super.onDestroy();
        stopSpaceman();
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