package com.carlex.drive;


import java.util.ArrayList;
import java.util.List;

public class CustomGnssStatus {
    private List<Satellite> satellites;

    public CustomGnssStatus(List<Satellite> satellites) {
        this.satellites = satellites;
    }

    public int getSatelliteCount() {
        return satellites.size();
    }

    public int getConstellationType(int index) {
        return satellites.get(index).constellationType;
    }

    public int getSvid(int index) {
        return satellites.get(index).svid;
    }

    public float getCn0DbHz(int index) {
        return satellites.get(index).cn0DbHz;
    }

    public float getElevationDegrees(int index) {
        return satellites.get(index).elevationDegrees;
    }

    public float getAzimuthDegrees(int index) {
        return satellites.get(index).azimuthDegrees;
    }

    public boolean hasEphemerisData(int index) {
        return satellites.get(index).hasEphemerisData;
    }

    public boolean hasAlmanacData(int index) {
        return satellites.get(index).hasAlmanacData;
    }

    public boolean usedInFix(int index) {
        return satellites.get(index).usedInFix;
    }

    public static class Satellite {
        int constellationType;
        int svid;
        float cn0DbHz;
        float elevationDegrees;
        float azimuthDegrees;
        boolean hasEphemerisData;
        boolean hasAlmanacData;
        boolean usedInFix;

        public Satellite(int constellationType, int svid, float cn0DbHz, float elevationDegrees, float azimuthDegrees,
                         boolean hasEphemerisData, boolean hasAlmanacData, boolean usedInFix) {
            this.constellationType = constellationType;
            this.svid = svid;
            this.cn0DbHz = cn0DbHz;
            this.elevationDegrees = elevationDegrees;
            this.azimuthDegrees = azimuthDegrees;
            this.hasEphemerisData = hasEphemerisData;
            this.hasAlmanacData = hasAlmanacData;
            this.usedInFix = usedInFix;
        }
    }
}
