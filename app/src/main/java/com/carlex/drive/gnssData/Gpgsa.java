package com.carlex.drive.gnssData;

import java.util.List;
import java.util.Random;

public class Gpgsa implements NmeaSentence {
    private static final String SENTENCE_ID = "GPGSA";
    private String selectMode;
    private int mode;
    private List<String> satsIds;
    private double pdop;
    private double hdop;
    private double vdop;

    public Gpgsa(GpgsvGroup gpgsvGroup) {
        this.selectMode = "A";
        this.mode = 3;
        this.satsIds = gpgsvGroup.getSatsIds();
        this.pdop = 1.56;
        this.hdop = 0.92;
        this.vdop = 1.25;
    }

    public int getSatsCount() {
        return satsIds.size();
    }

    @Override
    public String toString() {
        // Fill in the satellite IDs to ensure there are 12 fields
        String[] satsIdsOutput = new String[12];
        for (int i = 0; i < 12; i++) {
            if (i < satsIds.size()) {
                satsIdsOutput[i] = satsIds.get(i);
            } else {
                satsIdsOutput[i] = "";
            }
        }
        String nmeaOutput = String.format("%s,%s,%d,%s,%.2f,%.2f,%.2f",
										  SENTENCE_ID, selectMode, mode, String.join(",", satsIdsOutput), pdop, hdop, vdop);
        return String.format("$%s*%s", nmeaOutput, NmeaMsg.checkSum(nmeaOutput));
    }
}

