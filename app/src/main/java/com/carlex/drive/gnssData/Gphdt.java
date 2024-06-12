package com.carlex.drive.gnssData;

public class Gphdt implements NmeaSentence {
    private static final String SENTENCE_ID = "GPHDT";
    private double heading;

    public Gphdt(double heading) {
        this.heading = heading;
    }

    @Override
    public String toString() {
        String nmeaOutput = String.format("%s,%.2f,T", SENTENCE_ID, heading);
        return String.format("$%s*%s", nmeaOutput, NmeaMsg.checkSum(nmeaOutput));
    }
}

