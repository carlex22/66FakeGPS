package com.carlex.drive.gnssData;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Gpgll implements NmeaSentence {
    private static final String SENTENCE_ID = "GPGLL";
    private String utcTime;
    private String[] latitude;
    private String[] longitude;
    private String dataStatus;
    private String faaMode;

    public Gpgll(LocalDateTime utcDateTime, Position position) {
        this.utcTime = utcDateTime.format(DateTimeFormatter.ofPattern("HHmmss"));
        this.latitude = position.getLatitudeValue();
        this.longitude = position.getLongitudeValue();
        this.dataStatus = "A";
        this.faaMode = "A";
    }

    @Override
    public String toString() {
        String nmeaOutput = String.format("%s,%s,%s,%s,%s,%s.000,%s,%s",
										  SENTENCE_ID, latitude[0], latitude[1], longitude[0], longitude[1], utcTime, dataStatus, faaMode);
        return String.format("$%s*%s", nmeaOutput, NmeaMsg.checkSum(nmeaOutput));
    }
}

