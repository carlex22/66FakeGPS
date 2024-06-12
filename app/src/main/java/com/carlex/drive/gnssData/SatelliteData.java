package com.carlex.drive.gnssData;

public class SatelliteData {
    private String prn;
    private String elevation;
    private String azimuth;
    private String snr;

    public SatelliteData(String prn, String elevation, String azimuth, String snr) {
        this.prn = prn;
        this.elevation = elevation;
        this.azimuth = azimuth;
        this.snr = snr;
    }

    public String getPrn() {
        return prn;
    }

    public String getElevation() {
        return elevation;
    }

    public String getAzimuth() {
        return azimuth;
    }

    public String getSnr() {
        return snr;
    }
}

