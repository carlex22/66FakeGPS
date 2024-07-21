package com.carlex.drive;

import android.util.Log;
 

import java.io.IOException;
import java.time.Year;

public class fLocation {
    private final String TAG = "DataLocation";
    double lat = 0.0;
    double lon = 0.0;
    double bear = 0.0;
    float speed = 0.0f;
    double alt = 0.0;
    long time = 0L;

    public fLocation(double lat, double lon, double bear, float speed, double alt, long time) {
        this.lat = lat;
        this.lon = lon;
        this.alt = alt;
        this.speed = speed;
        this.time = time;
        this.bear = bear;
    }

    public double getLatitude() {
        return lat;
    }

    public double getLongitude() {
        return lon;
    }

    public float getSpeed() {
        return speed;
    }

    public double getBearing() {
        return bear;
    }

    public double getAltitude() {
        return alt;
    }

    public long getTime() {
        return time;
    }

    public String toString() {
        return "fLocation:[Latitude:" + lat + ", Longitude:" + lon + ", Altitude:" + alt + ", Speed:" + speed + ", Bearing:" + bear + ", Time:" + time + "]";
    }

    // Método para retornar a diferença em metros entre as localizações
    public double getDeltaDistance(fLocation floc) {
        try {
            double lat1 = (lat);
            double lat2 = (floc.getLatitude());
            double lon1 = (lon);
            double lon2 = (floc.getLongitude());
            double distance = computeDistanceAndBearing(lat1, lon1, lat2, lon2)[0];
            //Log.i(TAG, "getDeltaDistance: valor -> " + distance);
            return distance;
        } catch (Exception e) {
            //Log.e(TAG, "Error calculating distance", e);
            return 0.0; // Retorna 0 em caso de erro
        }
    }

    // Método para retornar a diferença em altitude entre as localizações
    public double getDeltaAltitude(fLocation floc) {
        if (floc != null) {
            double deltaAltitude = getAltitude() - floc.getAltitude();
            //Log.i(TAG, "getDeltaAltitude: valor -> " + deltaAltitude);
            return deltaAltitude;
        }
        return 0.001d;
    }

    // Método para retornar a diferença em radianos da bearing entre as localizações
    public double getThetaBearing(fLocation floc) {
        if (floc != null) {
            double deltaBearing = dBearing(getBearing(), floc.getBearing());
            double thetaBearing = Math.toRadians(deltaBearing);
            //Log.i(TAG, "getThetaBearing: valor -> " + thetaBearing);
            return thetaBearing;
        }
        return 0.001d;
    }

    // Método para retornar o ângulo em radianos
    public double getThetaAltitude(fLocation floc) {
        double deltaAltitude = getDeltaAltitude(floc);
        double deltaDistance = getDeltaDistance(floc);
        if (deltaDistance != 0) {
            double thetaAltitude = Math.atan(deltaAltitude / deltaDistance);
            //Log.i(TAG, "getThetaAltitude: valor -> " + thetaAltitude);
            return thetaAltitude;
        }
        return 0.001d;
    }

    // Método para retornar a diferença em milissegundos entre as localizações
    public long getDeltaTime(fLocation floc) {
        if (floc != null) {
            long deltaTime = getTime() - floc.getTime();
            //Log.i(TAG, "getDeltaTime: valor -> " + deltaTime);
            return deltaTime;
        }
        return 100l;
    }

    // Método para retornar a velocidade em m/s entre as localizações
    public double getDeltaSpeedXY(fLocation floc) {
        double deltaDistance = getDeltaDistance(floc);
        long deltaTime = getDeltaTime(floc);
        if (deltaTime != 0) {
            double deltaSpeedXY = deltaDistance / (deltaTime / 1000.0);
            //Log.i(TAG, "getDeltaSpeedXY: valor -> " + deltaSpeedXY);
            return deltaSpeedXY;
        }
        return 0.001d;
    }

    // Método para retornar a velocidade em m/s entre as localizações (altitude)
    public double getDeltaSpeedZ(fLocation floc) {
        double deltaAltitude = getDeltaAltitude(floc);
        long deltaTime = getDeltaTime(floc);
        if (deltaTime != 0) {
            double deltaSpeedZ = deltaAltitude / (deltaTime / 1000.0);
            //Log.i(TAG, "getDeltaSpeedZ: valor -> " + deltaSpeedZ);
            return deltaSpeedZ;
        }
        return 0.001d;
    }

    // Método para retornar a velocidade em radianos/s entre as localizações (bearing)
    public double getThetaSpeedXY(fLocation floc) {
        double thetaBearing = getThetaBearing(floc);
        long deltaTime = getDeltaTime(floc);
        if (deltaTime != 0) {
            double thetaSpeedXY = thetaBearing / (deltaTime / 1000.0);
            //Log.i(TAG, "getThetaSpeedXY: valor -> " + thetaSpeedXY);
            return thetaSpeedXY;
        }
        return 0.001d;
    }

    // Método para retornar a velocidade em radianos/s entre as localizações (altitude)
    public double getThetaSpeedZ(fLocation floc) {
        double thetaAltitude = getThetaAltitude(floc);
        long deltaTime = getDeltaTime(floc);
        if (deltaTime != 0) {
            double thetaSpeedZ = thetaAltitude / (deltaTime / 1000.0);
            //Log.i(TAG, "getThetaSpeedZ: valor -> " + thetaSpeedZ);
            return thetaSpeedZ;
        }
        return 0.001d;
    }

    public double dBearing(double bearing1, double bearing2) {
        double diff1 = (bearing1 - bearing2);
        double diff2 = 360 - (bearing1 - bearing2);
        double diffBearing = Math.min(diff1, diff2);
        diffBearing = (diffBearing);
        if (diff1 < diff2) diffBearing = -diffBearing;
        return diffBearing;
    }

    private static float[] computeDistanceAndBearing(double lat1, double lon1, double lat2, double lon2) {
        int MAXITERS = 20;
        lat1 *= Math.PI / 180.0;
        lat2 *= Math.PI / 180.0;
        lon1 *= Math.PI / 180.0;
        lon2 *= Math.PI / 180.0;

        double a = 6378137.0;
        double b = 6356752.3142;
        double f = (a - b) / a;
        double aSqMinusBSqOverBSq = (a * a - b * b) / (b * b);

        double L = lon2 - lon1;
        double A = 0.0;
        double U1 = Math.atan((1.0 - f) * Math.tan(lat1));
        double U2 = Math.atan((1.0 - f) * Math.tan(lat2));

        double cosU1 = Math.cos(U1);
        double cosU2 = Math.cos(U2);
        double sinU1 = Math.sin(U1);
        double sinU2 = Math.sin(U2);
        double cosU1cosU2 = cosU1 * cosU2;
        double sinU1sinU2 = sinU1 * sinU2;

        double sigma = 0.0;
        double deltaSigma = 0.0;
        double cosSqAlpha = 0.0;
        double cos2SM = 0.0;
        double cosSigma = 0.0;
        double sinSigma = 0.0;
        double cosLambda = 0.0;
        double sinLambda = 0.0;

        double lambda = L;
        for (int iter = 0; iter < MAXITERS; iter++) {
            double lambdaOrig = lambda;
            cosLambda = Math.cos(lambda);
            sinLambda = Math.sin(lambda);
            double t1 = cosU2 * sinLambda;
            double t2 = cosU1 * sinU2 - sinU1 * cosU2 * cosLambda;
            double sinSqSigma = t1 * t1 + t2 * t2;
            sinSigma = Math.sqrt(sinSqSigma);
            cosSigma = sinU1sinU2 + cosU1cosU2 * cosLambda;
            sigma = Math.atan2(sinSigma, cosSigma);
            double sinAlpha = (sinSigma == 0) ? 0.0 :
                    cosU1cosU2 * sinLambda / sinSigma;
            cosSqAlpha = 1.0 - sinAlpha * sinAlpha;
            cos2SM = (cosSqAlpha == 0) ? 0.0 :
                    cosSigma - 2.0 * sinU1sinU2 / cosSqAlpha;

            double uSquared = cosSqAlpha * aSqMinusBSqOverBSq;
            A = 1 + (uSquared / 16384.0) *
                    (4096.0 + uSquared *
                            (-768 + uSquared * (320.0 - 175.0 * uSquared)));
            double B = (uSquared / 1024.0) *
                    (256.0 + uSquared *
                            (-128.0 + uSquared * (74.0 - 47.0 * uSquared)));
            double C = (f / 16.0) *
                    cosSqAlpha *
                    (4.0 + f * (4.0 - 3.0 * cosSqAlpha));
            double cos2SMSq = cos2SM * cos2SM;
            deltaSigma = B * sinSigma *
                    (cos2SM + (B / 4.0) *
                            (cosSigma * (-1.0 + 2.0 * cos2SMSq) -
                                    (B / 6.0) * cos2SM *
                                            (-3.0 + 4.0 * sinSigma * sinSigma) *
                                            (-3.0 + 4.0 * cos2SMSq)));

            lambda = L +
                    (1.0 - C) * f * sinAlpha *
                            (sigma + C * sinSigma *
                                    (cos2SM + C * cosSigma *
                                            (-1.0 + 2.0 * cos2SM * cos2SM)));

            double delta = (lambda - lambdaOrig) / lambda;
            if (Math.abs(delta) < 1.0e-12) {
                break;
            }
        }

        float distance = (float) (b * A * (sigma - deltaSigma));
        float initialBearing = (float) Math.atan2(cosU2 * sinLambda,
                cosU1 * sinU2 - sinU1 * cosU2 * cosLambda);
        initialBearing *= 180.0 / Math.PI;
        float finalBearing = (float) Math.atan2(cosU1 * sinLambda,
                -sinU1 * cosU2 + cosU1 * sinU2 * cosLambda);
        finalBearing *= 180.0 / Math.PI;

        return new float[]{distance, initialBearing, finalBearing};
    }

    
 }
