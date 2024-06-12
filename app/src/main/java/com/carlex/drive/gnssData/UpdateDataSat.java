package com.carlex.drive.gnssData;
public class UpdateDataSat {
    private Position position;
    private double altitude;

    public UpdateDataSat(Position position, double altitude) {
        this.position = position;
        this.altitude = altitude;
    }

    public SatelliteDataList updateSatelliteData() {
        SatelliteDataList satelliteDataList = new SatelliteDataList();

        // Adicionando dados dos satélites com valores fixos
        satelliteDataList.addSatelliteData(new SatelliteData("01", "40", "083", "41"));
        satelliteDataList.addSatelliteData(new SatelliteData("02", "17", "278", "43"));
        satelliteDataList.addSatelliteData(new SatelliteData("03", "22", "128", "46"));
        satelliteDataList.addSatelliteData(new SatelliteData("04", "62", "197", "50"));
        satelliteDataList.addSatelliteData(new SatelliteData("05", "12", "234", "48"));
        satelliteDataList.addSatelliteData(new SatelliteData("06", "20", "187", "42"));
        satelliteDataList.addSatelliteData(new SatelliteData("07", "25", "267", "45"));
        satelliteDataList.addSatelliteData(new SatelliteData("08", "12", "156", "37"));
        satelliteDataList.addSatelliteData(new SatelliteData("09", "34", "267", "40"));
        satelliteDataList.addSatelliteData(new SatelliteData("10", "45", "301", "50"));
        satelliteDataList.addSatelliteData(new SatelliteData("11", "32", "145", "39"));
        satelliteDataList.addSatelliteData(new SatelliteData("12", "28", "112", "35"));

        return satelliteDataList;
    }
}

