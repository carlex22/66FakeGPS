
package com.carlex.drive;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import uk.me.g4dpz.satellite.GroundStationPosition;
import uk.me.g4dpz.satellite.SatPos;
import uk.me.g4dpz.satellite.Satellite;
import uk.me.g4dpz.satellite.SatelliteFactory;
import uk.me.g4dpz.satellite.TLE;

public class SpaceMan {
    private static final String TAG = "SpaceMan";

    protected Date now;
    protected Location calculatedLocation;
    protected GroundStationPosition groundStationPosition;
    public String xtles;

    public SpaceMan() {
        // Constructor padrão com contexto fictício, substitua pelo contexto real na sua aplicação
        this(MainActivity.mainApp, "", new Location("dummyprovider"));
    }

    public SpaceMan(Context context) {
        // Inicialização padrão com o contexto
        this(context, "", new Location("dummyprovider"));
    }

    public SpaceMan(Context context, String tles, Location loc) {
        setNow();
        setGroundStationPosition(loc);
        this.xtles = readRawTextFile(context, R.raw.gps);
        parseTLE();
        calculatePositions();
        dumpSatelliteInfo();
        dumpGpsSatellites();
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

    public ArrayList<MyGpsSatellite> getGpsSatellites() {
        return gpsSatellites;
    }

    public int getTLECount() {
        return sats.size();
    }

    public void parseTLE() {
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
                if (gs.elevation > 10.0) {
                    gpsSatellites.add(gs);
                }
            }
        }
    }

    public void dumpSatelliteInfo() {
        for (SatelliteInfo si : sats) {
            log("SAT " + si.tle.getName() + " above_horizon " + si.satPos.isAboveHorizon());
            log("azi " + si.satPos.getAzimuth() + " ele " + si.satPos.getElevation() +
                    " lon " + si.satPos.getLongitude() + " lat " + si.satPos.getLatitude() + " alt " + si.satPos.getAltitude());
        }
    }

    public void dumpGpsSatellites() {
        //log("Fix: " + gpsSatellites.size() + " sats");
        //for (MyGpsSatellite gs : gpsSatellites) {
            //log(gs.toString());
        //}
    }

    protected void log(String s) {
        //Log.d(TAG, s);
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

    // Adicione a classe SatelliteInfo e MyGpsSatellite aqui, conforme necessário
    public class SatelliteInfo {
        public TLE tle;
        public Satellite satellite;
        public SatPos satPos;
    }

    public class MyGpsSatellite {
        public float azimuth, elevation; // deg
        public int prn;
        public float snr;
        public boolean hasAlmanac, hasEphemeris, usedInFix;

        @Override
        public String toString() {
            return "MyGpsSatellite PRN " + prn + " SNR " + snr +
                    " Azi " + azimuth + " Ele " + elevation +
                    " ALM " + hasAlmanac + " EPH " + hasEphemeris + " USE " + usedInFix;
        }
    }

    // Defina o ArrayList gpsSatellites
    protected ArrayList<MyGpsSatellite> gpsSatellites = new ArrayList<>();

    // Defina o ArrayList sats
    protected ArrayList<SatelliteInfo> sats = new ArrayList<>();
}

