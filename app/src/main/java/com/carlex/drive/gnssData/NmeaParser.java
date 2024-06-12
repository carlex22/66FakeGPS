package com.carlex.drive.gnssData;
import java.util.ArrayList;
import java.util.List;

public class NmeaParser {
    private String latitude;
    private String longitude;
    private String altitude;
    private String hdop;
    private String vdop;
    private boolean validFix;
    private int numSatellites;
    private double speed; // velocidade em nós
    private double course;
    private SatelliteDataList satelliteDataList;

    public NmeaParser() {
        satelliteDataList = new SatelliteDataList();
    }

    public void parseNmeaSentence(String nmeaSentence) {
        if (nmeaSentence.startsWith("$GPGGA")) {
            parseGPGGA(nmeaSentence);
        } else if (nmeaSentence.startsWith("$GPRMC")) {
            parseGPRMC(nmeaSentence);
        } else if (nmeaSentence.startsWith("$GPVTG")) {
            parseGPVTG(nmeaSentence);
        } else if (nmeaSentence.startsWith("$GPGSA")) {
            parseGPGSA(nmeaSentence);
        } else if (nmeaSentence.startsWith("$GPGSV")) {
            parseGPGSV(nmeaSentence);
        }
    }

    private void parseGPGGA(String sentence) {
        String[] parts = sentence.split(",");
        latitude = parts[2] + " " + parts[3];
        longitude = parts[4] + " " + parts[5];
        altitude = parts[9] + "." + parts[10];
        validFix = !"0".equals(parts[6]);
    }

    private void parseGPRMC(String sentence) {
        String[] parts = sentence.split(",");
        validFix = "A".equals(parts[2]);
        latitude = parts[3] + " " + parts[4];
        longitude = parts[5] + " " + parts[6];
        if (isNumeric(parts[7])) {
            speed = Double.parseDouble(parts[7]);
        }
        if (isNumeric(parts[8])) {
            course = Double.parseDouble(parts[8]);
        }
    }

    private void parseGPVTG(String sentence) {
        String[] parts = sentence.split(",");
        if (isNumeric(parts[1])) {
            course = Double.parseDouble(parts[1]);
        }
        if (isNumeric(parts[5])) { // Ensure the correct field is used for speed in knots
            speed = Double.parseDouble(parts[5]);
        }
    }

    private void parseGPGSA(String sentence) {
        String[] parts = sentence.split(",");
        hdop = parts[16];
        vdop = parts[17];
    }

    private void parseGPGSV(String sentence) {
        String[] parts = sentence.split(",");
        numSatellites = Integer.parseInt(parts[3]);
        for (int i = 4; i < parts.length; i += 4) {
            if (i + 3 < parts.length) {
                String prn = parts[i];
                String elevation = parts[i + 1];
                String azimuth = parts[i + 2];
                String snr = parts[i + 3];
                if (snr.contains("*")) {
                    snr = snr.split("\\*")[0];
                }
                SatelliteData data = new SatelliteData(prn, elevation, azimuth, snr);
                satelliteDataList.addSatelliteData(data);
            }
        }
    }

    private boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getAltitude() {
        return altitude;
    }

    public String getHdop() {
        return hdop;
    }

    public String getVdop() {
        return vdop;
    }

    public boolean isValidFix() {
        return validFix;
    }

    public int getNumSatellites() {
        return numSatellites;
    }

    public double getSpeed() {
        return speed;
    }

    public double getCourse() {
        return course;
    }

    public List<SatelliteData> getSatelliteDataList() {
        return satelliteDataList.getSatelliteDataList();
    }
}

