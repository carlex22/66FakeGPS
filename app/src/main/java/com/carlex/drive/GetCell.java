package com.carlex.drive;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class GetCell {

    private static final String TAG = "xCell";
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

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

        @Override
        public String toString() {
            return "CellInfoData{" +
                    "cellId='" + cellId + '\'' +
                    ", lac='" + lac + '\'' +
                    ", mcc='" + mcc + '\'' +
                    ", mnc='" + mnc + '\'' +
                    ", lat=" + lat +
                    ", lon=" + lon +
                    ", date='" + date + '\'' +
                    '}';
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
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d(TAG, "Iniciando tarefa para obter informações da célula.");
        }

        @Override
        protected CellInfoData doInBackground(Void... voids) {
            Log.d(TAG, "doInBackground: Iniciando solicitação para obter informações da célula.");
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

                Log.d(TAG, "doInBackground: URL para solicitação: " + url);

                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setRequestMethod("GET");

                Log.d(TAG, "doInBackground: Conexão estabelecida, enviando solicitação...");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                StringBuilder response = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                Log.d(TAG, "doInBackground: Resposta recebida: " + response.toString());

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
                        Log.d(TAG, "doInBackground: Informações da célula obtidas - Cell ID: " + cellId + ", LAC: " + lac + ", MCC: " + mcc + ", MNC: " + mnc);
                        return new CellInfoData(cellId, lac, mcc, mnc, lat, lon, date);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "doInBackground: Erro ao obter informações da célula", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(CellInfoData cellInfoData) {
            super.onPostExecute(cellInfoData);
            if (cellInfoData != null) {
                Log.d(TAG, "onPostExecute: Informações da célula obtidas com sucesso.");
                Log.d(TAG, "onPostExecute: " + cellInfoData.toString());
            } else {
                Log.d(TAG, "onPostExecute: Falha ao obter informações da célula.");
            }
        }
    }

    public static CellInfoData getCellInfo(double lat, double lon) {
        Log.d(TAG, "getCellInfo: Iniciando obtenção de informações da célula para latitude: " + lat + ", longitude: " + lon);
        try {
            CellInfoData cellInfoData = new GetCellInfoTask(lat, lon).execute().get();
            if (cellInfoData != null) {
                Log.d(TAG, "getCellInfo: Informações da célula: " + cellInfoData.toString());
            } else {
                Log.d(TAG, "getCellInfo: Não foi possível obter informações da célula.");
            }
            return cellInfoData;
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "getCellInfo: Erro ao obter informações da célula", e);
            return null;
        }
    }
}
