package com.carlex.drive;
import android.location.GnssStatus;
import android.util.Log;

public class GNSSStatusHolder {
    public static GnssStatus gnssStatus;
    private static final String TAG = "GNSSStatusHolder";

    public static void setGnssStatus(GnssStatus status) {
        gnssStatus = status;
    }

    public static GnssStatus getGnssStatus() {
        return gnssStatus;
    }

    public static void logGnssStatus() {
        if (gnssStatus != null) {
            for (int i = 0; i < gnssStatus.getSatelliteCount(); i++) {
                int constellationType = gnssStatus.getConstellationType(i);
                int svid = gnssStatus.getSvid(i);
                float cn0DbHz = gnssStatus.getCn0DbHz(i);
                Log.d(TAG, "GNSS satellite: " + svid + ", Constellation: " + constellationType + ", CN0: " + cn0DbHz);
            }
        } else {
            Log.d(TAG, "No GNSS status available");
        }
    }
}

