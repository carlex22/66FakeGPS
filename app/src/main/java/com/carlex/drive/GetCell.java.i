package com.carlex.drive;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class GetCell {

    private static final String PREFS_NAME = "CellPrefs";
    private static final String KEY_CELL_DATA = "CellData";
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private Context context;

    public GetCell(Context context, double lat, double lon) {
        this.context = context;
        CellInfoData cellInfoData = getCellInfo(lat, lon);
        if (cellInfoData != null) {
            saveCellData(cellInfoData);
        }
    }

    public static class CellInfoData {
        private String cellId;
        private String lac;
        private String mcc;
        private String mnc;
        private double lat;
        private double lon;
        private String date;

        public CellInfoData(String cellId, String lac, String mcc, String mnc, double lat, double lon, String date) {
            this.cellId = cellId;
            this.lac = lac;
            this.mcc = mcc;
            this.mnc = mnc;
            this.lat = lat;
            this.lon = lon;
            this.date = date;
        }

        public String getCellId() {
            return cellId;
        }

        public String getLac() {
            return lac;
        }

        public String getMcc() {
            return mcc;
        }

        public String getMnc() {
            return mnc;
        }

        public double getLat() {
            return lat;
        }

        public double getLon() {
            return lon;
        }

        public String getDate() {
            return date;
        }
    }

    private static class GetCellInfoTask extends AsyncTask<Void, Void, CellInfoData> {
        private double lat;
        private double lon;

        public GetCellInfoTask(double lat, double lon) {
            this.lat = lat;
            this.lon = lon;
        }

        @Override
        protected CellInfoData doInBackground(Void... voids) {
            try {
                String apiKey = "pk.28294d46ef4d93a0404c0fbb562264f6";
                double centerLat = lat;
                double centerLon = lon;
                double radius = 0.009;

                double minLat = centerLat - radius;
                double maxLat = centerLat + radius;
                double minLon = centerLon - radius;
                double maxLon = centerLon + radius;

                String url = "https://opencellid.org/cell/getInArea?key=" + apiKey +
                        "&BBOX=" + minLat + "," + minLon + "," + maxLat + "," + maxLon +
                        "&format=json&mcc=724&limit=1";

                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                StringBuilder response = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                connection.disconnect();

                JSONObject jsonResponse = new JSONObject(response.toString());
                if (jsonResponse.has("cells")) {
                    JSONArray cells = jsonResponse.getJSONArray("cells");
                    if (cells.length() > 0) {
                        JSONObject cell = cells.getJSONObject(0);
                        String cellId = cell.getString("cellid");
                        String lac = cell.getString("lac");
                        String mcc = cell.getString("mcc");
                        String mnc = cell.getString("mnc");
                        String date = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(new Date());
                        return new CellInfoData(cellId, lac, mcc, mnc, lat, lon, date);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private static Location getLastKnownLocation(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Location location = null;
        if (locationManager != null) {
            List<String> providers = locationManager.getProviders(true);
            for (String provider : providers) {
                Location l = locationManager.getLastKnownLocation(provider);
                if (l != null) {
                    if (location == null || l.getAccuracy() < location.getAccuracy()) {
                        location = l;
                    }
                }
            }
        }
        return location;
    }

    private CellInfoData getCellInfo(double lat, double lon) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Location currentLocation = getLastKnownLocation(context);

        if (currentLocation == null) {
            Log.d("cell", "Não foi possível obter a localização.");
            return null;
        }

        try {
            return new GetCellInfoTask(currentLocation.getLatitude(), currentLocation.getLongitude()).execute().get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void saveCellData(CellInfoData cellInfoData) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putString(KEY_CELL_DATA, cellInfoData.getCellId() + ","
                        + cellInfoData.getLac() + ","
                        + cellInfoData.getMcc() + ","
                        + cellInfoData.getMnc() + ","
                        + cellInfoData.getLat() + ","
                        + cellInfoData.getLon() + ","
                        + cellInfoData.getDate())
                .apply();
    }

    public static CellInfoData getSavedCellData(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedData = prefs.getString(KEY_CELL_DATA, null);
        if (savedData != null) {
            String[] dataParts = savedData.split(",");
            if (dataParts.length == 7) {
                return new CellInfoData(dataParts[0], dataParts[1], dataParts[2], dataParts[3],
                        Double.parseDouble(dataParts[4]), Double.parseDouble(dataParts[5]), dataParts[6]);
            }
        }
        return null;
    }
}

