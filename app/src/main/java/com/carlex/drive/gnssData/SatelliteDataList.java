package com.carlex.drive.gnssData;
import java.util.ArrayList;
import java.util.List;

public class SatelliteDataList {
    private List<SatelliteData> satelliteDataList;

    public SatelliteDataList() {
        this.satelliteDataList = new ArrayList<>();
    }

    public void addSatelliteData(SatelliteData satelliteData) {
        this.satelliteDataList.add(satelliteData);
    }

    public List<SatelliteData> getSatelliteDataList() {
        return satelliteDataList;
    }

    public void clear() {
        this.satelliteDataList.clear();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (SatelliteData data : satelliteDataList) {
            builder.append("PRN: ").append(data.getPrn())
				.append(", Elevation: ").append(data.getElevation())
				.append(", Azimuth: ").append(data.getAzimuth())
				.append(", SNR: ").append(data.getSnr()).append("\n");
        }
        return builder.toString();
    }
}

