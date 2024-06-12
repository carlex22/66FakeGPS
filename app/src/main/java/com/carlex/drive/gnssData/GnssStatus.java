package com.carlex.drive.gnssData;
import java.util.List;

public class GnssStatus {
    private String latitude;
    private String longitude;
    private String altitude;
    private String hdop;
    private String vdop;
    private boolean validFix;
    private int numSatellites;
    private double speed;
    private double course;
    private List<SatelliteData> satelliteDataList;

    public GnssStatus(NmeaParser parser) {
        this.latitude = parser.getLatitude();
        this.longitude = parser.getLongitude();
        this.altitude = parser.getAltitude();
        this.hdop = parser.getHdop();
        this.vdop = parser.getVdop() != null ? parser.getVdop() : "Unavailable";
        this.validFix = parser.isValidFix();
        this.numSatellites = parser.getNumSatellites();
        this.speed = parser.getSpeed();
        this.course = parser.getCourse();
        this.satelliteDataList = parser.getSatelliteDataList();
    }

    public void printStatus() {
        System.out.println("\nGNSS Status:");
        //System.out.println("Latitude: " + latitude);
        //System.out.println("Longitude: " + longitude);
        //System.out.println("Altitude: " + altitude);
        //System.out.println("HDOP: " + hdop);
        //System.out.println("VDOP: " + vdop);
        System.out.println("Valid Fix: " + validFix);
        System.out.println("Number of Satellites: " + numSatellites);
        //System.out.println("Speed: " + speed + " knots");
        //System.out.println("Course: " + course + " degrees");
        for (SatelliteData satellite : satelliteDataList) {
            System.out.println("\n>>	Satellite PRN: " + satellite.getPrn());
            System.out.println(">>>>	Elevation: " + satellite.getElevation());
            System.out.println(">>>>	Azimuth: " + satellite.getAzimuth());
            System.out.println(">>>>	SNR: " + satellite.getSnr());
        }
    }
}

