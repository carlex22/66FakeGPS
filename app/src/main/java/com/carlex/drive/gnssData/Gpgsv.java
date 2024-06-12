package com.carlex.drive.gnssData;
import java.util.List;

public class Gpgsv implements NmeaSentence {
    private static final String SENTENCE_ID = "GPGSV";
    private int numOfGsvInGroup;
    private int sentenceNum;
    private int satsTotal;
    private int satsInSentence;
    private List<SatelliteData> satsData;
    private String satsDetails;

    public Gpgsv(int numOfGsvInGroup, int sentenceNum, int satsTotal, int satsInSentence, List<SatelliteData> satsData) {
        this.numOfGsvInGroup = numOfGsvInGroup;
        this.sentenceNum = sentenceNum;
        this.satsTotal = satsTotal;
        this.satsInSentence = satsInSentence;
        this.satsData = satsData;
        this.satsDetails = "";

        for (SatelliteData sat : satsData) {
            this.satsDetails += String.format(",%s,%s,%s,%s", sat.getPrn(), sat.getElevation(), sat.getAzimuth(), sat.getSnr());
        }
    }

    @Override
    public String toString() {
        String nmeaOutput = String.format("%s,%d,%d,%d%s",
                                          SENTENCE_ID, numOfGsvInGroup, sentenceNum, satsTotal, satsDetails);
        return String.format("$%s*%s", nmeaOutput, NmeaMsg.checkSum(nmeaOutput));
    }
}

