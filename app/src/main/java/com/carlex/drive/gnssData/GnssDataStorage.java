package com.carlex.drive.gnssData;
import java.util.ArrayList;
import java.util.List;

public class GnssDataStorage {
    private static List<String> nmeaMessages = new ArrayList<>();
    private static List<SatelliteData> satelliteDataList = new ArrayList<>();
    private static String latitude;
    private static String longitude;
    private static double altitude;
    private static String hdop;
    private static String vdop;
    private static boolean validFix;
    private static int numSatellites;
    private static double speed;
    private static double course;

    public static void clear() {
        nmeaMessages.clear();
        satelliteDataList.clear();
        latitude = null;
        longitude = null;
        altitude = 0;
        hdop = null;
        vdop = null;
        validFix = false;
        numSatellites = 0;
        speed = 0;
        course = 0;
    }

    public static List<String> getNmeaMessages() {
        return new ArrayList<>(nmeaMessages);
    }

    public static List<SatelliteData> getSatelliteDataList() {
        return new ArrayList<>(satelliteDataList);
    }

    public static String getLatitude() {
        return latitude;
    }

    public static String getLongitude() {
        return longitude;
    }

    public static double getAltitude() {
        return altitude;
    }

    public static String getHdop() {
        return hdop;
    }

    public static String getVdop() {
        return vdop;
    }

    public static boolean isValidFix() {
        return validFix;
    }

    public static int getNumSatellites() {
        return numSatellites;
    }

    public static double getSpeed() {
        return speed;
    }

    public static double getCourse() {
        return course;
    }

    public static void printGnssData() {
        System.out.println("NMEA Messages:");
        for (String message : nmeaMessages) {
            System.out.println(message);
        }

        System.out.println("\nNMEA Data:");
        //System.out.println("Latitude: " + latitude);
        //System.out.println("Longitude: " + longitude);
        //System.out.println("Altitude: " + altitude);
        System.out.println("HDOP: " + hdop);
        System.out.println("VDOP: " + vdop);
        //System.out.println("Valid Fix: " + validFix);
        //System.out.println("Number of Satellites: " + numSatellites);
        //System.out.println("Speed: " + speed + " knots");
        //System.out.println("Course: " + course + " degrees");

        //System.out.println("Satellite Data:");
        for (SatelliteData satellite : satelliteDataList) {
            System.out.println("PRN: " + satellite.getPrn() +
                               ", Elevation: " + satellite.getElevation() +
                               ", Azimuth: " + satellite.getAzimuth() +
                               ", SNR: " + satellite.getSnr());
        }
    }

    public static void addNmeaMessage(String message) {
        nmeaMessages.add(message);
    }

    public static void addSatelliteData(SatelliteData data) {
        satelliteDataList.add(data);
    }

    public static void setLatitude(String lat) {
        latitude = lat;
    }

    public static void setLongitude(String lon) {
        longitude = lon;
    }

    public static void setAltitude(double alt) {
        altitude = alt;
    }

    public static void setHdop(String hd) {
        hdop = hd;
    }

    public static void setVdop(String vd) {
        vdop = vd;
    }

    public static void setValidFix(boolean fix) {
        validFix = fix;
    }

    public static void setNumSatellites(int numSat) {
        numSatellites = numSat;
    }

    public static void setSpeed(double spd) {
        speed = spd;
    }

    public static void setCourse(double crs) {
        course = crs;
    }
}

