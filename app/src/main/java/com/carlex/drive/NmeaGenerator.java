package com.carlex.drive;
import android.location.GnssStatus;
import android.location.Location;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
public class NmeaGenerator {

    public static String generateNmea(CustomGnssStatus gnssStatus, Location location, String originalNmea) {
        StringBuilder nmeaSentences = new StringBuilder();

        // Analisar a sentença NMEA original para determinar qual sentença gerar
        if (originalNmea.startsWith("$GPGGA")) {
            nmeaSentences.append(generateGgaSentence(location));
        } else if (originalNmea.startsWith("$GPRMC")) {
            nmeaSentences.append(generateRmcSentence(location));
        } else if (originalNmea.startsWith("$GPGSV")) {
            nmeaSentences.append(generateGsvSentence(gnssStatus));
        } else if (originalNmea.startsWith("$GPGSA")) {
            nmeaSentences.append(generateGsaSentence(gnssStatus));
        } else if (originalNmea.startsWith("$GPVTG")) {
            nmeaSentences.append(generateVtgSentence(location));
        }
        else {
           nmeaSentences.append(generateGgaSentence(location));
        }

        return nmeaSentences.toString();
    }

    private static String generateGgaSentence(Location location) {
        StringBuilder gga = new StringBuilder();
        gga.append("$GPGGA,");
        gga.append(formatTime(location.getTime())).append(",");
        gga.append(formatCoordinate(location.getLatitude(), "lat")).append(",");
        gga.append(formatCoordinate(location.getLongitude(), "lon")).append(",");
        gga.append("1,"); // Fix quality
        gga.append("08,"); // Number of satellites
        gga.append("1.0,"); // HDOP
        gga.append(String.format(Locale.US, "%.1f", location.getAltitude())).append(",M,"); // Altitude
        gga.append("0.0,M,"); // Height of geoid above WGS84 ellipsoid
        gga.append(",,"); // Time since last DGPS update
        gga.append("*");
        gga.append(calculateChecksum(gga.toString()));

        return gga.toString();
    }

    private static String generateRmcSentence(Location location) {
        StringBuilder rmc = new StringBuilder();
        rmc.append("$GPRMC,");
        rmc.append(formatTime(location.getTime())).append(",A,");
        rmc.append(formatCoordinate(location.getLatitude(), "lat")).append(",");
        rmc.append(formatCoordinate(location.getLongitude(), "lon")).append(",");
        rmc.append(String.format(Locale.US, "%.1f", location.getSpeed() * 1.943844)).append(","); // Speed in knots
        rmc.append(String.format(Locale.US, "%.1f", location.getBearing())).append(","); // Bearing
        rmc.append(formatDate(location.getTime())).append(",");
        rmc.append("*");
        rmc.append(calculateChecksum(rmc.toString()));

        return rmc.toString();
    }


private static String generateGsvSentence(CustomGnssStatus gnssStatus) {
    StringBuilder gsv = new StringBuilder();
    int numSatellites = gnssStatus.getSatelliteCount();
    
    if (numSatellites <= 0) {
        return null;
    }
    
    int numSentences = (int) Math.ceil(numSatellites / 4.0);

    for (int i = 0; i < numSentences; i++) {
        gsv.append(String.format(Locale.US, "$GPGSV,%d,%d,%02d,", numSentences, i + 1, numSatellites));
        for (int j = 0; j < 4; j++) {
            int index = i * 4 + j;
            if (index < numSatellites) {
                gsv.append(String.format(Locale.US, "%02d,%.1f,%.1f,%.1f,", 
                    gnssStatus.getSvid(index), 
                    gnssStatus.getCn0DbHz(index), 
                    gnssStatus.getAzimuthDegrees(index), 
                    gnssStatus.getElevationDegrees(index)));
            } else {
                gsv.append(",,,,");
            }
        }
        gsv.append("*");
        gsv.append(calculateChecksum(gsv.toString()));
        gsv.append("\n");
    }

    return gsv.toString().trim();
}


    private static String generateGsaSentence(CustomGnssStatus gnssStatus) {
        StringBuilder gsa = new StringBuilder();
        gsa.append("$GPGSA,A,3,"); // Auto selection of 2D or 3D fix, 3D fix
        int maxSatellites = 12;
        for (int i = 0; i < maxSatellites; i++) {
            if (i < gnssStatus.getSatelliteCount()) {
                gsa.append(String.format(Locale.US, "%02d,", gnssStatus.getSvid(i)));
            } else {
                gsa.append(",");
            }
        }
        gsa.append("1.0,1.0,1.0*"); // PDOP, HDOP, VDOP
        gsa.append(calculateChecksum(gsa.toString()));

        return gsa.toString();
    }

    private static String generateVtgSentence(Location location) {
        StringBuilder vtg = new StringBuilder();
        vtg.append("$GPVTG,");
        vtg.append(String.format(Locale.US, "%.1f,T,", location.getBearing())); // Course over ground (True)
        vtg.append(",M,"); // Course over ground (Magnetic) - not provided
        vtg.append(String.format(Locale.US, "%.1f,N,", location.getSpeed() * 1.943844)); // Speed over ground in knots
        vtg.append(String.format(Locale.US, "%.1f,K", location.getSpeed() * 3.6)); // Speed over ground in km/h
        vtg.append("*");
        vtg.append(calculateChecksum(vtg.toString()));

        return vtg.toString();
    }

    private static String formatTime(long time) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HHmmss.SSS", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(new Date(time));
    }

    private static String formatDate(long time) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyy", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(new Date(time));
    }

    private static String formatCoordinate(double coordinate, String type) {
        StringBuilder sb = new StringBuilder();
        String direction;

        if (type.equals("lat")) {
            direction = coordinate >= 0 ? "N" : "S";
            coordinate = Math.abs(coordinate);
            int degrees = (int) coordinate;
            double minutes = (coordinate - degrees) * 60;
            sb.append(String.format(Locale.US, "%02d%07.4f,%s", degrees, minutes, direction));
        } else {
            direction = coordinate >= 0 ? "E" : "W";
            coordinate = Math.abs(coordinate);
            int degrees = (int) coordinate;
            double minutes = (coordinate - degrees) * 60;
            sb.append(String.format(Locale.US, "%03d%07.4f,%s", degrees, minutes, direction));
        }

        return sb.toString();
    }

    private static String calculateChecksum(String sentence) {
        int checksum = 0;
        for (int i = 1; i < sentence.length(); i++) {
            checksum ^= sentence.charAt(i);
        }
        return String.format("%02X", checksum);
    }
}