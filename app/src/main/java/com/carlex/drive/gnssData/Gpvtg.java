package com.carlex.drive.gnssData;

import java.util.StringJoiner;

public class Gpvtg implements NmeaSentence {
    private static final String SENTENCE_ID = "GPVTG";
    private double headingTrue;
    private double sogKnots;

    public Gpvtg(double headingTrue, double sogKnots) {
        this.headingTrue = headingTrue;
        this.sogKnots = sogKnots;
    }

    private double getSogKmhr() {
        return Math.round(sogKnots * 1.852 * 10.0) / 10.0;
    }

    @Override
    public String toString() {
        String nmeaOutput = new StringJoiner(",", SENTENCE_ID + ",", "")
			.add(String.format("%.2f,T", headingTrue))
			.add("")
			.add(String.format("%.2f,N", sogKnots))
			.add(String.format("%.2f,K", getSogKmhr()))
			.toString();
        return String.format("$%s*%s", nmeaOutput, NmeaMsg.checkSum(nmeaOutput));
    }
}

