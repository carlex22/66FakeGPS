package com.carlex.drive.gnssData;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Gprmc implements NmeaSentence {
    private static final String SENTENCE_ID = "GPRMC";
    private String utcTime;
    private String utcDate;
    private String dataStatus;
    private String[] latitude;
    private String[] longitude;
    private double sog;
    private double cmg;
    private String magneticVarValue;
    private String magneticVarDirect;
    private String faaMode;

    public Gprmc(LocalDateTime utcDateTime, Position position, double sog, double cmg) {
        this.utcTime = utcDateTime.format(DateTimeFormatter.ofPattern("HHmmss"));
        this.utcDate = utcDateTime.format(DateTimeFormatter.ofPattern("ddMMyy"));
        this.dataStatus = "A";
        this.latitude = position.getLatitudeValue();
        this.longitude = position.getLongitudeValue();
        this.sog = sog;
        this.cmg = cmg;
        this.magneticVarValue = "";
        this.magneticVarDirect = "";
        this.faaMode = "A";
    }

    @Override
    public String toString() {
        String nmeaOutput = String.format("%s,%s.000,%s,%s,%s,%s,%s,%.3f,%.2f,%s,%s,%s,%s",
										  SENTENCE_ID, utcTime, dataStatus, latitude[0], latitude[1], longitude[0], longitude[1], sog, cmg, utcDate, magneticVarValue, magneticVarDirect, faaMode);
        return String.format("$%s*%s", nmeaOutput, NmeaMsg.checkSum(nmeaOutput));
    }
}

