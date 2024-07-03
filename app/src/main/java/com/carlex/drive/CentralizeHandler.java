package com.carlex.drive;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import com.google.android.gms.maps.model.LatLng;
import android.os.Looper;

public class CentralizeHandler {
    private final Handler handler;
    private final Context context;
    private final MainActivity mainActivity;
    private static final String PREFS_NAME = "LocationPreferences";

    public CentralizeHandler(MainActivity mainActivity, Context context) {
        this.mainActivity = mainActivity;
        this.context = context;
        this.handler = new Handler(Looper.getMainLooper());
    }

    public void startCentralizing(long intervaloMillisegundos) {
        CentralizeRunnable centralizeRunnable = new CentralizeRunnable(mainActivity, handler, intervaloMillisegundos, context);
        handler.postDelayed(centralizeRunnable, intervaloMillisegundos);
    }

    public void stopCentralizing() {
        handler.removeCallbacksAndMessages(null);
    }

    private static class CentralizeRunnable implements Runnable {
        private final Handler handler;
        private final long intervaloMillisegundos;
        private final Context context;
        private final MainActivity mainActivity;

        public CentralizeRunnable(MainActivity mainActivity, Handler handler, long intervaloMillisegundos, Context context) {
            this.mainActivity = mainActivity;
            this.handler = handler;
            this.intervaloMillisegundos = intervaloMillisegundos;
            this.context = context;
        }

        @Override
        public void run() {
            // Atualizar mapa
            mainActivity.runOnUiThread(() -> {
                mainActivity.centralizar();
            });

            // Carregar dados de localização das preferências
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            double latitude = Double.parseDouble(prefs.getString("latitude", "0.0"));
            double longitude = Double.parseDouble(prefs.getString("longitude", "0.0"));
            float bearing = Float.parseFloat(prefs.getString("bearing", "0.0"));
            float speed = Float.parseFloat(prefs.getString("speed", "0.0"));
            speed /= 3.6f;
            double altitude = Double.parseDouble(prefs.getString("altitude", "0.0"));

            // Atualizar com dados carregados
            mainActivity.latLng = new LatLng(latitude, longitude);
            mainActivity.currentSpeed = speed;
            mainActivity.currentBearing = bearing;
            mainActivity.currentAlt = altitude;

            // Agendar próxima execução
            handler.postDelayed(this, intervaloMillisegundos);
        }
    }
}
