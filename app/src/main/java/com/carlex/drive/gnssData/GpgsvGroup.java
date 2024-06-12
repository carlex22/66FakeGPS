package com.carlex.drive.gnssData;
import java.util.ArrayList;
import java.util.List;

public class GpgsvGroup implements NmeaSentence {
    private static final int SATS_IN_SENTENCE = 4;
    private int satsTotal;
    private List<Gpgsv> gpgsvInstances;
    private List<String> satsIds;

    public GpgsvGroup(Position position, double altitude) {
        this.satsTotal = 12; // Definindo um total fixo de 12 satélites
        this.gpgsvInstances = new ArrayList<>();
        this.satsIds = new ArrayList<>();
        for (int i = 1; i <= this.satsTotal; i++) {
            this.satsIds.add(String.format("%02d", i));
        }

        // Atualizando dados dos satélites usando a classe UpdateDataSat
        UpdateDataSat updateDataSat = new UpdateDataSat(position, altitude);
        SatelliteDataList satelliteDataList = updateDataSat.updateSatelliteData();

        int numOfGsvInGroup = (int) Math.ceil((double) this.satsTotal / SATS_IN_SENTENCE);
        int index = 0;
        for (int i = 0; i < numOfGsvInGroup; i++) {
            int satsInThisSentence = (i == numOfGsvInGroup - 1) ? this.satsTotal % SATS_IN_SENTENCE : SATS_IN_SENTENCE;
            if (satsInThisSentence == 0) {
                satsInThisSentence = SATS_IN_SENTENCE;
            }
            List<SatelliteData> satsDataSentence = new ArrayList<>();
            for (int j = 0; j < satsInThisSentence; j++) {
                if (index < satelliteDataList.getSatelliteDataList().size()) {
                    satsDataSentence.add(satelliteDataList.getSatelliteDataList().get(index++));
                } else {
                    satsDataSentence.add(new SatelliteData("", "", "", ""));
                }
            }
            this.gpgsvInstances.add(new Gpgsv(numOfGsvInGroup, i + 1, this.satsTotal, satsInThisSentence, satsDataSentence));
        }
    }

    public List<String> getSatsIds() {
        return satsIds;
    }

    public List<Gpgsv> getGpgsvInstances() {
        return gpgsvInstances;
    }

    @Override
    public String toString() {
        StringBuilder gpgsvGroupStr = new StringBuilder();
        for (Gpgsv gpgsv : gpgsvInstances) {
            gpgsvGroupStr.append(gpgsv.toString());
        }
        return gpgsvGroupStr.toString();
    }
}

