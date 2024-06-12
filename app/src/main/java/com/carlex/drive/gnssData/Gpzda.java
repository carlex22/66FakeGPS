package com.carlex.drive.gnssData;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Gpzda implements NmeaSentence {
    private static final String SENTENCE_ID = "GPZDA";
    private String utcTime;
    private String utcDate;

    public Gpzda(LocalDateTime utcDateTime) {
        this.utcTime = utcDateTime.format(DateTimeFormatter.ofPattern("HHmmss"));
        this.utcDate = utcDateTime.format(DateTimeFormatter.ofPattern("dd,MM,yyyy"));
    }

    @Override
    public String toString() {
        String nmeaOutput = String.format("%s,%s.000,%s,0,0", SENTENCE_ID, utcTime, utcDate);
        return String.format("$%s*%s", nmeaOutput, NmeaMsg.checkSum(nmeaOutput));
    }
}

