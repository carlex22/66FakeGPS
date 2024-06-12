package com.carlex.drive.gnssData;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Gpgga implements NmeaSentence {
    private static final String SENTENCE_ID = "GPGGA";
    private int satsCount;
    private String utcTime;
    private String[] latitude;
    private String[] longitude;
    private int fixQuality;
    private double hdop;
    private double altitude;
    private double antennaAltitudeAboveMsl;
    private String dgpsLastUpdate;
    private String dgpsRefStationId;

    public Gpgga(int satsCount, LocalDateTime utcDateTime, Position position, double altitude) {
        this.satsCount = satsCount;
        this.utcTime = utcDateTime.format(DateTimeFormatter.ofPattern("HHmmss"));
        this.latitude = position.getLatitudeValue();
        this.longitude = position.getLongitudeValue();
        this.fixQuality = 1;
        this.hdop = 0.92;
        this.altitude = altitude;
        this.antennaAltitudeAboveMsl = 32.5;
        this.dgpsLastUpdate = "";
        this.dgpsRefStationId = "";
    }

    @Override
    public String toString() {
        String nmeaOutput = String.format("%s,%s.00,%s,%s,%s,%s,%d,%02d,%.2f,%.1f,M,%.1f,M,%s,%s",
										  SENTENCE_ID, utcTime, latitude[0], latitude[1], longitude[0], longitude[1], fixQuality,
										  satsCount, hdop, altitude, antennaAltitudeAboveMsl, dgpsLastUpdate, dgpsRefStationId);
        return String.format("$%s*%s", nmeaOutput, NmeaMsg.checkSum(nmeaOutput));
    }
}

