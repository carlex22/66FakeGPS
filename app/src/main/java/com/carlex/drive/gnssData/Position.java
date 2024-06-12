package com.carlex.drive.gnssData;

public class Position {
    private double latitude;
    private double longitude;

    public Position(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String[] getLatitudeValue() {
        return convertToDegMin(latitude, 'S', 'N');
    }

    public String[] getLongitudeValue() {
        return convertToDegMin(longitude, 'W', 'E');
    }

    private static String[] convertToDegMin(double coord, char negativo, char positivo) {
        int graus = (int) Math.abs(coord);
        double minutosDecimal = (Math.abs(coord) - graus) * 60;
        char direcao = coord >= 0 ? positivo : negativo;
        return new String[] {String.format("%02d%06.3f", graus, minutosDecimal), Character.toString(direcao)};
    }

    public String convertLongitudeToDegMin() {
        int graus = (int) Math.abs(longitude);
        double minutosDecimal = (Math.abs(longitude) - graus) * 60;
        char direcao = longitude >= 0 ? 'E' : 'W';
        return String.format("%03d° %06.3f' %c", graus, minutosDecimal, direcao);
    }
	
	
	public String convertLatitudeToDegMin() {
        int graus = (int) Math.abs(latitude);
        double minutosDecimal = (Math.abs(longitude) - graus) * 60;
        char direcao = latitude >= 0 ? 'N' : 'S';
        return String.format("%03d° %06.3f' %c", graus, minutosDecimal, direcao);
    }

    @Override
    public String toString() {
        return "Position{" +
			"latitude=" + latitude +
			", longitude=" + longitude +
			'}';
    }
}

